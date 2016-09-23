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
import io.silverware.microservices.annotations.ParamName;
import io.silverware.microservices.util.BootUtil;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
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

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class CdiMicroserviceProviderGatewayTest {

   private static final Logger log = LogManager.getLogger(CdiMicroserviceProviderGatewayTest.class);

   private static final Semaphore semaphore = new Semaphore(0);

   @Test
   public void testGateway() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName());
      platform.start();

      CdiMicroserviceProviderTestUtil.waitForBeanManager(bootUtil);
      Thread.sleep(100);
      final List<String> checksPassed = new ArrayList<>();

      Vertx vertx = Vertx.vertx();

      HttpClient client = vertx.createHttpClient();

      client.getNow(8081, "localhost", "/rest", response -> {
         response.bodyHandler(buffer -> {
            JsonArray services = new JsonArray(buffer.toString());

            Assert.assertEquals(services.size(), 1);
            Assert.assertTrue(services.contains("RestfulMicroservice"));
            checksPassed.add("Service list");
            semaphore.release();
         });
      });

      client.getNow(8081, "localhost", "/rest/RestfulMicroservice", response -> {
         response.bodyHandler(buffer -> {
            JsonArray methods = new JsonArray(buffer.toString());

            Assert.assertEquals(methods.size(), 2);

            JsonObject o1, o2;
            if (methods.getJsonObject(0).getString("methodName").equals("hello")) {
               o1 = methods.getJsonObject(0);
               o2 = methods.getJsonObject(1);
            } else {
               o1 = methods.getJsonObject(1);
               o2 = methods.getJsonObject(0);
            }

            Assert.assertEquals(o1.getString("methodName"), "hello");
            Assert.assertEquals(o1.getJsonArray("parameters").size(), 0);
            Assert.assertEquals(o1.getString("returns"), "void");
            Assert.assertEquals(o2.getString("methodName"), "multiHello");
            Assert.assertEquals(o2.getJsonArray("parameters").size(), 1);
            Assert.assertEquals(o2.getJsonArray("parameters").getString(0), "int");
            Assert.assertEquals(o2.getString("returns"), "java.lang.String");
            checksPassed.add("Methods list");
            semaphore.release();
         });
      });

      client.getNow(8081, "localhost", "/rest/RestfulMicroservice/hello", response -> {
         response.bodyHandler(buffer -> {
            JsonObject result = new JsonObject(buffer.toString());

            Assert.assertTrue(result.containsKey("result"));
            Assert.assertEquals(result.getString("result"), "null");
            checksPassed.add("Call to hello");
            semaphore.release();
         });
      });

      final String request = new JsonObject().put("count", 4).encodePrettily();
      client.post(8081, "localhost", "/rest/RestfulMicroservice/multiHello", response -> {
         response.bodyHandler(buffer -> {
            JsonObject result = new JsonObject(buffer.toString());

            Assert.assertTrue(result.containsKey("result"));
            Assert.assertEquals(result.getString("result"), "\"hello hello hello hello\"");
            checksPassed.add("Call to multiHello");
            semaphore.release();
         });
      }).putHeader("content-length", String.valueOf(request.length())).putHeader("content-type", "application/json").write(request).end();

      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES));
      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES));
      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES));
      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES));

      Assert.assertEquals(checksPassed.size(), 4);

      platform.interrupt();
      platform.join();
   }

   @Gateway
   @Microservice
   public static class RestfulMicroservice {

      public void hello() {
         log.info("This is the coolest method");
      }

      public String multiHello(@ParamName("count") int count) {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < count; i++) {
            sb.append("hello ");
         }
         sb.deleteCharAt(sb.length() - 1); // get rid of the last space

         return sb.toString();
      }
   }

}
