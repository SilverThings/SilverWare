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
package org.silverware.microservices.providers.cdi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silverware.microservices.annotations.Microservice;
import org.silverware.microservices.annotations.MicroserviceReference;
import org.silverware.microservices.util.BootUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class CdiMicroserviceProviderTest {

   private static final Logger log = LogManager.getLogger(CdiMicroserviceProviderTest.class);

   private static final Semaphore semaphore = new Semaphore(0);

   private TestMicroserviceB testMicroserviceB;

   @Test
   public void testCdi() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName());
      platform.start();

      BeanManager beanManager = null;
      while (beanManager == null) {
         beanManager = (BeanManager) bootUtil.getContext().getProperties().get(CdiMicroserviceProvider.BEAN_MANAGER);
         Thread.sleep(200);
      }

      testMicroserviceB = (TestMicroserviceB) CdiMicroserviceProvider.getMicroservice(bootUtil.getContext(), TestMicroserviceB.class);

      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES), "Timed-out while waiting for platform startup.");

      testMicroserviceB.hello();

      platform.interrupt();
      platform.join();
   }

   @Microservice
   public static class TestMicroserviceA {
      public void hello() {
         log.info("Hello from A " + this);
      }
   }

   @Microservice
   public static class TestMicroserviceB {

      @Inject
      @MicroserviceReference
      private TestMicroserviceA testMicroserviceA;

      @Inject
      @MicroserviceReference
      private TestMicro testMicroBean;

      public TestMicroserviceB() {
         onInit();
      }

      //@PostActivate
      //@PostConstruct
      public void onInit() {
         //new Throwable().printStackTrace();
         log.error("initttttt " + this.getClass().getName());
         semaphore.release();
      }

      public void hello() {
         log.info("Hello from B to A " + (testMicroserviceA != null ? testMicroserviceA.getClass().getName() : null));
         //testMicroserviceA.hello();
         log.info("Hello from B to Micro ");// + testMicroBean.getClass().getName());
         //testMicroBean.hello();
      }
   }

   @Microservice
   public static class TestMicroserviceC {

      @Inject
      @MicroserviceReference
      private TestMicroserviceB testMicroserviceB;

      public TestMicroserviceC() {
         log.info("Instance of C");
      }

      public void eventObserver(@Observes MicroservicesStartedEvent event) {
         log.info("Hello from C to B " + testMicroserviceB.getClass().getName());
         testMicroserviceB.hello();
      }
   }

   public static interface TestMicro {
      public void hello();
   }

   @Microservice("microBean")
   public static class TestMicroBean implements TestMicro {

      @Override
      public void hello() {
         log.info("micro hello");
      }
   }
}