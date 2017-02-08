/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2016 the original author or authors.
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

package io.silverware.microservices.providers.vertx;

import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.util.BootUtil;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public class XMLConfigTest {

   private static final Semaphore semaphore = new Semaphore(0);

   private static final Set<String> checksPassed = Collections.synchronizedSet(new HashSet<>());

   private volatile static int serversEstablished = 0;

   @Test
   public void deploymentAnnotationTest() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName(),
              CdiMicroserviceProvider.class.getPackage().getName(), "io.vertx.core");
      platform.start();

      VertxMicroserviceProviderTestUtil.waitFotPlatformStart(bootUtil);

      while (serversEstablished < 2) {
         Thread.sleep(1000);
      }

      Vertx vertx = Vertx.vertx();
      vertx.createHttpClient().getNow(8084, "localhost", "/", response -> {
         Assert.assertEquals(response.statusCode(), 200);
         response.bodyHandler(buffer -> {
            Assert.assertEquals(buffer.toString(), "response");
         });

         checksPassed.add("basic deployment");
         semaphore.release();
      });

      vertx.createHttpClient().getNow(8085, "localhost", "/", response -> {
         Assert.assertEquals(response.statusCode(), 200);
         response.bodyHandler(buffer -> {
            Assert.assertEquals(buffer.toString(), "response");
         });

         Assert.assertEquals(response.getHeader("js2_worker"), "true");
         Assert.assertEquals(response.getHeader("js2_instances"), "2");

         JsonObject expected = new JsonObject();
         expected.put("bar", "BaR");
         expected.put("foo", "FOO");
         Assert.assertEquals(response.getHeader("js2_config_foo"), expected.getString("foo"));
         Assert.assertEquals(response.getHeader("js2_config_bar"), expected.getString("bar"));

         checksPassed.add("options deployment");
         semaphore.release();
      });

      semaphore.acquireUninterruptibly();
      semaphore.acquireUninterruptibly();

      Assert.assertEquals(checksPassed.size(), 2);

      platform.interrupt();
      platform.join();
   }

   public synchronized static void serversEstablished() {
      serversEstablished++;
   }
}
