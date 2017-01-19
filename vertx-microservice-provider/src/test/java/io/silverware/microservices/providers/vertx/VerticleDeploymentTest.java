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
import io.silverware.microservices.providers.vertx.verticle.TestVerticle;
import io.silverware.microservices.util.BootUtil;

import io.vertx.core.Vertx;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.Semaphore;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public class VerticleDeploymentTest {

   private static final Semaphore semaphore = new Semaphore(0);

   private Vertx vertx;

   private volatile static int serversEstablished = 0;

   @Test
   public void verticleDeplyomentTest() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName(),
              CdiMicroserviceProvider.class.getPackage().getName(), TestVerticle.class.getPackage().getName(), "io.vertx.core");
      platform.start();

      VertxMicroserviceProviderTestUtil.waitFotPlatformStart(bootUtil);

      vertx = Vertx.vertx();

      //wait for the server to start
      while (serversEstablished < 2) {
         Thread.sleep(1000);
      }

      //java server
      assertResponse(8082, "localhost", "/");

      //groovy server
      assertResponse(8083, "localhost", "/");

      semaphore.acquireUninterruptibly();
      semaphore.acquireUninterruptibly();

      platform.interrupt();
      platform.join();
   }

   public synchronized static void serverEstablished() {
      serversEstablished++;
   }

   private void assertResponse(int port, String host, String path) {
      vertx.createHttpClient().getNow(port, host, path, response -> {
         Assert.assertEquals(response.statusCode(), 200);
         Assert.assertNotNull(response.getHeader("semaphore"));
         Assert.assertEquals(response.getHeader("semaphore"), "release");
         response.bodyHandler(buffer -> {
            Assert.assertEquals(buffer.toString(), "response");
         });
         semaphore.release();
      });
   }
}
