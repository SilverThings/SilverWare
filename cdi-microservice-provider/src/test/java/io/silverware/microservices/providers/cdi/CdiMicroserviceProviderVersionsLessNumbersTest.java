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
import io.silverware.microservices.annotations.MicroserviceVersion;
import io.silverware.microservices.util.BootUtil;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * Test for versions with missing minor/patch versions.
 *
 * @author Slavomir Krupa (slavomir.krupa@gmail.com)
 */
public class CdiMicroserviceProviderVersionsLessNumbersTest {

   private static final Semaphore semaphore = new Semaphore(0);
   private static String result = "";

   @Test
   public void testVersionsResolutionAdvanced() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName());
      platform.start();

      CdiMicroserviceProviderTestUtil.waitForBeanManager(bootUtil);

      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES), "Timed-out while waiting for platform startup.");
      Assert.assertEquals(result, "hello2bye1");

      platform.interrupt();
      platform.join(0);
   }

   @Microservice("advanced")
   public static class TestVersionMicroserviceLess {

      @Inject
      @MicroserviceReference
      @MicroserviceVersion(api = "^2")
      private HelloVersion2Less micro1;

      @Inject
      @MicroserviceReference
      @MicroserviceVersion(implementation = "~1")
      private ByeVersion1Less micro2;

      public void eventObserver(@Observes MicroservicesStartedEvent event) {
         result += micro1.hello();
         result += micro2.bye();
         semaphore.release();
      }
   }

   public interface HelloVersion2Less {
      String hello();
   }

   public interface ByeVersion1Less {
      String bye();
   }

   @Microservice
   @MicroserviceVersion(implementation = "1.6", api = "1.6")
   public static class Version1LessMicroBeanLess implements HelloVersion2Less, ByeVersion1Less {

      @Override
      public String hello() {
         return "hello1";
      }

      @Override
      public String bye() {
         return "bye1";
      }
   }

   @Microservice
   @MicroserviceVersion(implementation = "2.4-SNAPSHOT", api = "2.4-SNAPSHOT")
   public static class Version2LessLessMicroBean implements HelloVersion2Less, ByeVersion1Less {

      @Override
      public String hello() {
         return "hello2";
      }

      @Override
      public String bye() {
         return "bye2";
      }
   }
}