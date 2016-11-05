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

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.util.BootUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class CdiMicroserviceProviderBasicTest {

   private static final Logger log = LogManager.getLogger(CdiMicroserviceProviderBasicTest.class);

   private static final Semaphore semaphore = new Semaphore(0);

   private TestMicroserviceB testMicroserviceB;
   private static String result = "";

   private static boolean postConstructCalled = false;

   @Test
   public void testCdi() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName());
      platform.start();

      CdiMicroserviceProviderTestUtil.waitForBeanManager(bootUtil);

      //testMicroserviceB = (TestMicroserviceB) CdiMicroserviceProvider.getMicroserviceProxy(bootUtil.getContext(), TestMicroserviceB.class);

      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES), "Timed-out while waiting for platform startup.");
      Assert.assertTrue(postConstructCalled);
      Assert.assertEquals(result, "micrononame");

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
      @MicroserviceReference("microBean")
      private TestMicro testMicroBean;

      @Inject
      @MicroserviceReference("noNameMicroBean")
      private TestMicro noNameMicroBean;

      public TestMicroserviceB() {
         log.info("MicroServiceB constructor");
      }

      @PostConstruct
      public void onInit() {
         log.info("MicroServiceB PostConstruct " + this.getClass().getName());
         postConstructCalled = true;
      }

      public void hello() {
         log.info("Hello from B (" + this.toString() + ") to A " + (testMicroserviceA != null ? testMicroserviceA.getClass().getName() : null));
         testMicroserviceA.hello();
         log.info("Hello from B to Micro " + testMicroBean.getClass().getName());
         result += testMicroBean.hello();
         result += noNameMicroBean.hello();
         semaphore.release();
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
         log.info("Hello from C (" + this.toString() + ") to B " + testMicroserviceB.getClass().getName());
         testMicroserviceB.hello();
      }
   }

   public interface TestMicro {
      String hello();
   }

   @Microservice("microBean")
   public static class TestMicroBean implements TestMicro {

      @Override
      public String hello() {
         log.info("micro hello");
         return "micro";
      }
   }

   @Microservice
   public static class NoNameMicroBean implements TestMicro {

      @Override
      public String hello() {
         log.info("noname hello");
         return "noname";
      }
   }

}