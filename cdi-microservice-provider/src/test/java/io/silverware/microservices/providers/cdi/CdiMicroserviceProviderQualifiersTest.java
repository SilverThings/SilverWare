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
package io.silverware.microservices.providers.cdi;

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.util.BootUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class CdiMicroserviceProviderQualifiersTest {

   private static final Logger log = LogManager.getLogger(CdiMicroserviceProviderQualifiersTest.class);

   private static final Semaphore semaphore = new Semaphore(0);
   private static String result = "";

   @Test
   public void testQualifiers() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName());
      platform.start();

      CdiMicroserviceProviderTestUtil.waitForBeanManager(bootUtil);

      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES), "Timed-out while waiting for platform startup.");
      Assert.assertEquals(result, "normalmockbez");

      platform.interrupt();
      platform.join();
   }

   @Microservice
   public static class TestQualifierMicroservice {

      @Inject
      @MicroserviceReference
      @Sharp
      private QualifierMicro micro1;

      @Inject
      @MicroserviceReference
      @Mock
      private QualifierMicro micro2;

      @Inject
      @MicroserviceReference
      @Default
      private QualifierMicro micro3;

      public void eventObserver(@Observes MicroservicesStartedEvent event) {
         result += micro1.hello();
         result += micro2.hello();
         result += micro3.hello();

         semaphore.release();
      }
   }

   public interface QualifierMicro {
      String hello();
   }

   @Sharp
   @Microservice
   public static class QualifierMicroBean implements QualifierMicro {

      @Override
      public String hello() {
         return "normal";
      }
   }

   @Mock
   @Microservice
   public static class MockQualifierMicroBean implements QualifierMicro {

      @Override
      public String hello() {
         return "mock";
      }
   }

   @Microservice
   public static class BezMockQualifierMicroBean implements QualifierMicro {

      @Override
      public String hello() {
         return "bez";
      }
   }

   @Qualifier
   @Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
   @Retention(RetentionPolicy.RUNTIME)
   public @interface Mock {
   }

   @Qualifier
   @Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
   @Retention(RetentionPolicy.RUNTIME)
   public @interface Sharp {
   }
}