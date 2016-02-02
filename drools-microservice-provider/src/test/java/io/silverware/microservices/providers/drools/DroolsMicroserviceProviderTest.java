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
package io.silverware.microservices.providers.drools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.api.cdi.KSession;
import org.kie.api.runtime.KieSession;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.providers.cdi.MicroservicesStartedEvent;
import io.silverware.microservices.silver.CdiSilverService;
import io.silverware.microservices.util.BootUtil;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class DroolsMicroserviceProviderTest {

   private static final Semaphore semaphore = new Semaphore(0);

   @Test(enabled = false)
   public void droolsMicroserviceProviderTest() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName(), CdiMicroserviceProvider.class.getPackage().getName());
      platform.start();

      BeanManager beanManager = null;
      while (beanManager == null) {
         beanManager = (BeanManager) bootUtil.getContext().getProperties().get(CdiSilverService.BEAN_MANAGER);
         Thread.sleep(200);
      }

      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES), "Timed-out while waiting for ..."); 

      platform.interrupt();
      platform.join();
   }

   @Microservice
   public static class DroolsConsumeMicroservice {
      private final Logger log = LogManager.getLogger(DroolsConsumeMicroservice.class);

      @Inject
      @KSession
      private KieSession session;

      public DroolsConsumeMicroservice() {

      }

      public void eventObserver(@Observes MicroservicesStartedEvent event) {

         log.info("Session: {}", session);

         log.info("DroolsConsumeMicroservice MicroservicesStartedEvent");

         semaphore.release();
      }

   }
}