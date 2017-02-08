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

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.providers.cdi.MicroservicesStartedEvent;
import io.silverware.microservices.util.BootUtil;

import io.vertx.core.Vertx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public class VertxMicroserviceCDITest {

   private static final Logger log = LogManager.getLogger(VertxMicroserviceCDITest.class);

   private static final Semaphore semaphore = new Semaphore(0);

   @Test
   public void vertxCDITest() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName(),
              CdiMicroserviceProvider.class.getPackage().getName(), "io.vertx.core");
      platform.start();

      VertxMicroserviceProviderTestUtil.waitFotPlatformStart(bootUtil);

      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES));

      platform.interrupt();
      platform.join();
   }

   @Microservice
   public static class MicrosetviceA {

      @Inject
      @MicroserviceReference
      private Vertx vertx;

      @Inject
      @MicroserviceReference
      private MicroserviceB microserviceB;

      public MicrosetviceA() {
         log.info("Instance of " + this.getClass().getSimpleName());
      }

      public void eventObserver(@Observes MicroservicesStartedEvent event) {
         log.info("Hello from observer " + this.getClass().getSimpleName() + " eventObserver()");

         Assert.assertNotNull(vertx);

         vertx.eventBus().consumer("topic", message -> {
            Assert.assertEquals((String) message.body(), "message");
            message.reply("acknowledge");
         });

         microserviceB.sendMessage();
      }
   }

   @Microservice
   public static class MicroserviceB {

      @Inject
      @MicroserviceReference
      private Vertx vertx;

      public MicroserviceB() {
         log.info("Instance of " + this.getClass().getSimpleName());
      }

      public void sendMessage() {
         log.info("Hello from " + this.getClass().getSimpleName() + " sendMessage()");

         Assert.assertNotNull(vertx);

         vertx.eventBus().send("topic", "message", ack -> {
            if (ack.succeeded()) {
               Assert.assertEquals(ack.result().body(), "acknowledge");
               semaphore.release();
            }
         });
      }
   }
}
