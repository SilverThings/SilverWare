/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2015 the original author or authors.
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
package io.silverware.providers.camel;

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.providers.camel.CamelMicroserviceProvider;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.providers.cdi.MicroservicesStartedEvent;
import io.silverware.microservices.silver.CdiSilverService;
import io.silverware.microservices.util.BootUtil;
import io.silverware.microservices.util.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class CamelCdiIntegrationTest {

   private static final Semaphore semaphore = new Semaphore(0);

   @Test
   public void testHello() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName(), CdiMicroserviceProvider.class.getPackage().getName(), CamelMicroserviceProvider.class.getPackage().getName());
      platform.start();

      BeanManager beanManager = null;
      while (beanManager == null) {
         beanManager = (BeanManager) bootUtil.getContext().getProperties().get(CdiSilverService.BEAN_MANAGER);
         Thread.sleep(200);
      }

      Assert.assertTrue(semaphore.tryAcquire(10, TimeUnit.MINUTES), "Timed-out while waiting for platform startup.");

      Thread.sleep(500);

      platform.interrupt();
      platform.join();
   }

   @Microservice("camelCdiMicroservice")
   public static class CamelCdiMicroservice {

      private final Logger log = LogManager.getLogger(CamelCdiMicroservice.class);

      public CamelCdiMicroservice() {
         log.info("CamelCdiMicroservice constructor");
      }

      @PostConstruct
      public void onInit() {
         log.info("CamelCdiMicroservice PostConstruct " + this.getClass().getName());
      }

      public void hello() {
         log.info("CamelCdiMicroservice Hello");
      }

      public String sayHello(final String msg) {
         semaphore.release();
         return "Answering '" + msg + "' with 'How do you do!'";
      }

      public void eventObserver(@Observes MicroservicesStartedEvent event) {
         log.info("CamelCdiMicroservice MicroservicesStartedEvent");
      }
   }
}