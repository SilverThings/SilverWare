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
package org.silverware.microservices.providers.cdi.qualifiers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silverware.microservices.annotations.Microservice;
import org.silverware.microservices.annotations.MicroserviceReference;
import org.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import org.silverware.microservices.providers.cdi.MicroservicesStartedEvent;
import org.silverware.microservices.util.BootUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Qualifier;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class CdiMicroserviceProviderQualifiersTest {

   private static final Logger log = LogManager.getLogger(CdiMicroserviceProviderQualifiersTest.class);

   private static final Semaphore semaphore = new Semaphore(0);

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

      Assert.assertTrue(semaphore.tryAcquire(10, TimeUnit.MINUTES), "Timed-out while waiting for platform startup.");

      platform.interrupt();
      platform.join();
   }

   @Microservice
   public static class TestQualifierMicroservice {

      @MicroserviceReference
      private QualifierMicro micro1;

      @MicroserviceReference
      @Mock
      private QualifierMicro micro2;

      public void eventObserver(@Observes MicroservicesStartedEvent event) {
         String ret = micro1.hello();
         ret += micro2.hello();
         log.info(ret);

         semaphore.release();
      }
   }

   public interface QualifierMicro {
      String hello();
   }

   @Microservice
   public static class QualifierMicroBean implements QualifierMicro {

      @Override
      public String hello() {
         return "normal";
      }
   }

   @Microservice
   public static class MockQualifierMicroBean implements QualifierMicro {

      @Override
      public String hello() {
         return "mock";
      }
   }

   @Qualifier
   @Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
   @Retention(RetentionPolicy.RUNTIME)
   public @interface Mock {
   }

}