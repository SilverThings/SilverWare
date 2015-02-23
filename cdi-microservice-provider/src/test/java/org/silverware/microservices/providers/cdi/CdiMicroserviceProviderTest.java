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
import org.silverware.microservices.util.BootUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.ejb.PostActivate;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
@Microservice
public class CdiMicroserviceProviderTest {

   private static final Logger log = LogManager.getLogger(CdiMicroserviceProviderTest.class);

   private static final Semaphore semaphore = new Semaphore(0);

   @Inject
   private TestMicroserviceB microserviceB;

   @Test
   public void testCdi() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName());
      platform.start();

      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES), "Timed-out while waiting for platform startup.");

      microserviceB.hello();

      platform.interrupt();
      platform.join();
   }

   @Microservice
   public static class TestMicroserviceA {
      public void hello() {
         log.info("Hello from A");
      }
   }

   @Microservice
   public static class TestMicroserviceB {
      @Inject
      private TestMicroserviceA microserviceA;

      public TestMicroserviceB() {
         onInit();
      }

      @PostActivate
      @PostConstruct
      public void onInit() {
         log.error("initttttt");
         semaphore.release();
      }

      public void hello() {
         log.info("Hello from B");
         microserviceA.hello();
      }
   }
}