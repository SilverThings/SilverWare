/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2015 - 2017 the original author or authors.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.util.BootUtil;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class CdiMicroserviceProviderProducesTest {

   private static final Logger log = LogManager.getLogger(CdiMicroserviceProviderProducesTest.class);
   private static final Semaphore semaphore = new Semaphore(0);

   @Test
   public void producesTest() throws InterruptedException {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName());
      platform.start();

      // wait for the CDI provider to start properly
      // early termination causes REST gateway to fail and log dummy errors
      CdiMicroserviceProviderTestUtil.waitForBeanManager(bootUtil);

      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES), "Timed-out while waiting for test completion.");

      platform.interrupt();
      platform.join();
   }

   @Microservice
   public static class ConsumingBean {

      @Inject
      @Named("myCoolPool")
      private ThreadPoolExecutor pool;

      public void eventObserver(@Observes MicroservicesStartedEvent event) {
         pool.submit(() -> {
            log.info("Working hard");
            semaphore.release();
         });
      }

   }

   @Microservice
   public static class ProducingBean {

      @Produces
      @Named("myCoolPool")
      public ThreadPoolExecutor getPool() {
         return (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
      }


   }
}
