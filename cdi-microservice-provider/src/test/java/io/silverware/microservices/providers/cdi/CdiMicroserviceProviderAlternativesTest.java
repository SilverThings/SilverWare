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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.util.BootUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class CdiMicroserviceProviderAlternativesTest {

   private static final Logger log = LogManager.getLogger(CdiMicroserviceProviderAlternativesTest.class);

   private static final Semaphore semaphore = new Semaphore(0);
   private static String result = "";

   @Test
   public void testQualifiers() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName());
      platform.start();

      BeanManager beanManager = null;
      while (beanManager == null) {
         beanManager = (BeanManager) bootUtil.getContext().getProperties().get(CdiMicroserviceProvider.BEAN_MANAGER);
         Thread.sleep(200);
      }

      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES), "Timed-out while waiting for platform startup.");
      Assert.assertEquals(result, "alternatealternate");

      platform.interrupt();
      platform.join();
   }

   @Microservice
   public static class TestAlternativesMicroservice {

      @Inject
      @MicroserviceReference
      private AlternativesMicro micro1;

      @Inject
      @MicroserviceReference
      private AlternativesMicro micro2;

      public void eventObserver(@Observes MicroservicesStartedEvent event) {
         result += micro1.hello();
         result += micro2.hello();

         semaphore.release();
      }
   }

   public interface AlternativesMicro {
      String hello();
   }

   @Microservice
   public static class AlternativesMicroBean implements AlternativesMicro {

      @Override
      public String hello() {
         return "normal";
      }
   }

   @Alternative
   @Microservice
   public static class AlternateAlternativesMicroBean extends AlternativesMicroBean {

      @Override
      public String hello() {
         return "alternate";
      }
   }
}