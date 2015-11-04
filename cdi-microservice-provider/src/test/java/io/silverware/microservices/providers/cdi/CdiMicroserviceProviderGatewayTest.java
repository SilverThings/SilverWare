/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2010 - 2013 the original author or authors.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------------/
 */
package io.silverware.microservices.providers.cdi;

import io.silverware.microservices.annotations.Gateway;
import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.util.BootUtil;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.enterprise.inject.spi.BeanManager;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class CdiMicroserviceProviderGatewayTest {

   private static final Logger log = LogManager.getLogger(CdiMicroserviceProviderGatewayTest.class);

   private static final Semaphore semaphore = new Semaphore(0);
   private static String result = "";

   @Test
   public void testGateway() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName());
      platform.start();

      BeanManager beanManager = null;
      while (beanManager == null) {
         beanManager = (BeanManager) bootUtil.getContext().getProperties().get(CdiMicroserviceProvider.BEAN_MANAGER);
         Thread.sleep(200);
      }

    //  Assert.assertTrue(semaphore.tryAcquire(10, TimeUnit.MINUTES), "Timed-out while waiting for platform startup.");

      final List<String> checksPassed = new ArrayList<>();

      Vertx vertx = Vertx.vertx();

      HttpClient client = vertx.createHttpClient();

      client.getNow(8081, "localhost", "/rest", response -> {
         response.bodyHandler(buffer -> {
            JsonArray services = new JsonArray(buffer.toString());

            Assert.assertEquals(services.size(), 1);
            Assert.assertTrue(services.contains("RestfulMicroservice"));
            checksPassed.add("Service list");
         });
      });

      client.getNow(8081, "localhost", "/rest/RestfulMicroservice", response -> {
         response.bodyHandler(buffer -> {
            JsonArray methods = new JsonArray(buffer.toString());
            log.info(methods);

            Assert.assertEquals(methods.size(), 2);
            Assert.assertEquals(methods.getJsonObject(0).getString("methodName"), "hello");
            Assert.assertEquals(methods.getJsonObject(0).getJsonArray("parameters").size(), 0);
            Assert.assertEquals(methods.getJsonObject(0).getString("returns"), "void");
            Assert.assertEquals(methods.getJsonObject(1).getString("methodName"), "multiHello");
            Assert.assertEquals(methods.getJsonObject(1).getJsonArray("parameters").size(), 1);
            Assert.assertEquals(methods.getJsonObject(1).getJsonArray("parameters").getString(0), "int");
            Assert.assertEquals(methods.getJsonObject(1).getString("returns"), "java.lang.String");
            checksPassed.add("Methods list");
         });
      });

      Thread.sleep(5000);

      //Assert.assertEquals(result, "normalmock");

      platform.interrupt();
      platform.join();
   }

   @Gateway
   @Microservice
   public static class RestfulMicroservice {

      public void hello() {
         log.info("This is the coolest method");
      }

      public String multiHello(int count) {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < count; i++) {
            sb.append("hello ");
         }
         sb.deleteCharAt(sb.length() - 1); // get rid of the last space

         return sb.toString();
      }
   }

}
