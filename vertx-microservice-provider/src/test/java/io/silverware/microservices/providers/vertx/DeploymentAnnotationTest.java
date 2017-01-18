/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2016 the original author or authors.
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

package io.silverware.microservices.providers.vertx;

import io.silverware.microservices.annotations.Deployment;
import io.silverware.microservices.enums.VerticleType;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.util.BootUtil;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public class DeploymentAnnotationTest {

   private static final Semaphore semaphore = new Semaphore(0);

   private static final Set<String> checksPassed = Collections.synchronizedSet(new HashSet<>());

   //test deployment false
   private static boolean deployedFalse = false;

   @Test
   public void deploymentAnnotationTest() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName(),
              CdiMicroserviceProvider.class.getPackage().getName(), "io.vertx.core");
      platform.start();

      VertxMicroserviceProviderTestUtil.waitFotPlatformStart(bootUtil);

      semaphore.acquireUninterruptibly();
      semaphore.acquireUninterruptibly();
      semaphore.acquireUninterruptibly();
      semaphore.acquireUninterruptibly();
      semaphore.acquireUninterruptibly();

      if (!deployedFalse) {
         checksPassed.add("Deployed false");
      }

      Assert.assertEquals(checksPassed.size(), 5);

      platform.interrupt();
      platform.join();
   }

   @Deployment()
   public static class DeploymentNoParams extends AbstractVerticle {

      @Override
      public void start() {
         checksPassed.add("No paramaters");
         semaphore.release();
      }
   }

   @Deployment(type = VerticleType.WORKER)
   public static class DeploymentType extends AbstractVerticle {

      @Override
      public void start() {
         Assert.assertTrue(vertx.getOrCreateContext().isWorkerContext());
         checksPassed.add("Verticle type");
         semaphore.release();
      }
   }

   @Deployment(instances = 2)
   public static class Instance extends AbstractVerticle {

      @Override
      public void start() {
         Assert.assertEquals(vertx.getOrCreateContext().getInstanceCount(), 2);
         checksPassed.add("Verticle instances");
         semaphore.release();
      }
   }

   @Deployment(config = "src/test/resources/example.json")
   public static class DeploymentConfig extends AbstractVerticle {

      @Override
      public void start() {
         Assert.assertNotNull(config());

         JsonObject expected = new JsonObject();
         expected.put("bar", "BaR");
         expected.put("foo", "FOO");


         Assert.assertEquals(config(), expected);

         checksPassed.add("Verticle config");
         semaphore.release();
      }
   }

   @Deployment(false)
   public static class DeploymentFalse extends AbstractVerticle {

      @Override
      public void start() {
         deployedFalse = true;
      }
   }
}
