/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2010 - 2016 the original author or authors.
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
 * Basic test for versioning.
 *
 * @author Slavomir Krupa (slavomir.krupa@gmail.com)
 */
public class CdiMicroserviceProviderVersionsTest {

   private static final Semaphore semaphore = new Semaphore(0);
   public static final String VERSION_1 = "1.1.1";
   public static final String VERSION_2 = "2.2.2";
   private static String result = "";

   @Test
   public void testBasicVersionResolution() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName());
      platform.start();

      CdiMicroserviceProviderTestUtil.waitForBeanManager(bootUtil);

      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES), "Timed-out while waiting for platform startup.");
      Assert.assertEquals(result, "hello2bye1");

      platform.interrupt();
      platform.join(1);
   }

   @Microservice("basic")
   public static class TestVersionMicroservice {

      @Inject
      @MicroserviceReference
      @MicroserviceVersion(api = VERSION_2)
      private HelloVersion2 micro1;

      @Inject
      @MicroserviceReference
      @MicroserviceVersion(implementation = VERSION_1)
      private ByeVersion1 micro2;

      public void eventObserver(@Observes MicroservicesStartedEvent event) {
         result += micro1.hello();
         result += micro2.bye();
         semaphore.release();
      }
   }

   public interface HelloVersion2 {
      String hello();
   }

   public interface ByeVersion1 {
      String bye();
   }

   @Microservice
   @MicroserviceVersion(implementation = VERSION_1, api = VERSION_1)
   public static class Version1MicroBean implements HelloVersion2, ByeVersion1 {

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
   @MicroserviceVersion(implementation = VERSION_2, api = VERSION_2)
   public static class Version2MicroBean implements HelloVersion2, ByeVersion1 {

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