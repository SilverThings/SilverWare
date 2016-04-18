/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2014 - 2016 the original author or authors.
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
package io.silverware.microservices.providers.rest;

import io.silverware.microservices.annotations.Gateway;
import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.providers.rest.annotation.JsonService;
import io.silverware.microservices.util.BootUtil;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.weld.environment.se.WeldContainer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class JsonRestServiceProxyTest {
   private static final Logger log = LogManager.getLogger(JsonRestServiceProxyTest.class);

   private static final Semaphore semaphore = new Semaphore(0);
   private static volatile String result;

   @Test
   public void restClientMicroserviceProviderTest() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName(), CdiMicroserviceProvider.class.getPackage().getName());
      platform.start();

      RestClientMicroserviceProviderTest.waitForBeanManager(bootUtil);
      final WeldContainer container = (WeldContainer) bootUtil.getContext().getProperties().get(CdiMicroserviceProvider.CDI_CONTAINER);
      container.event().select(StartTestEvent.class).fire(new StartTestEvent());

      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES), "Timed-out while waiting for the camel route deployment."); // wait for the route to be deployed
      Assert.assertEquals(result, "Hello Pepa");

      platform.interrupt();
      platform.join();
   }

   public static class StartTestEvent {

   }

   public interface HelloInterface {
      String hello(final String name);
   }

   @Microservice
   public static class ClientMicroservice {

      @Inject
      @MicroserviceReference
      @JsonService(endpoint = "http://localhost:8081/rest/HelloMicroservice")
      private HelloInterface helloService;

      public void eventObserver(@Observes StartTestEvent event) {
         log.info("Invoking injected service using REST and JSON...");

         try {
            Thread.sleep(1000); // wait for the REST interface to start
            result = helloService.hello("Pepa");
         } catch (Exception e) {
            log.error("Unable to call REST service: ", e);
         }

         semaphore.release();
      }

   }

   @Microservice
   @Gateway
   public static class HelloMicroservice implements HelloInterface {

      public String hello(final String name) {
         return "Hello " + name;
      }
   }
}