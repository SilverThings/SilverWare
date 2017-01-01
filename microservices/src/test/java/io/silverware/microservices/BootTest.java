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
package io.silverware.microservices;

import static io.silverware.microservices.Executor.SHUTDOWN_HOOK;

import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.util.BootUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class BootTest {

   private static final Logger log = LogManager.getLogger(BootTest.class);
   private static boolean wasInit = false;
   private static boolean wasRun = false;
   private static boolean wasInterrupted = false;
   private static final Semaphore semaphore = new Semaphore(0);

   @BeforeMethod
   public void beforeMethod() {
      wasInit = wasRun = wasInterrupted = false;
      semaphore.drainPermits();
   }

   @Test
   public void testLimitedBoot() throws InterruptedException {
      final Thread platform = (new BootUtil()).getMicroservicePlatform(BootTest.class.getPackage().getName());
      platform.start();
      platform.join();

      Assert.assertTrue(wasInit);
      Assert.assertTrue(wasRun);
      Assert.assertFalse(wasInterrupted);
   }

   @Test
   public void testFullBoot() throws InterruptedException {

      Boot.main("-D" + SHUTDOWN_HOOK + "=false");

      Assert.assertTrue(semaphore.tryAcquire(50, TimeUnit.SECONDS), "Timed-out while waiting for platform startup.");

      Assert.assertTrue(wasInit);
      Assert.assertTrue(wasRun);
      Assert.assertFalse(wasInterrupted);
   }

   public static class BootTestMicroservice implements MicroserviceProvider {

      @Override
      public void initialize(final Context context) {
         log.info("init");
         semaphore.release();
         wasInit = true;
      }

      @Override
      public void run() {
         log.info("run");
         try {
            Thread.sleep(100);
         } catch (InterruptedException e) {
            log.info("interrupted");
            wasInterrupted = true;
         }
         log.info("done");
         wasRun = true;
      }

   }
}