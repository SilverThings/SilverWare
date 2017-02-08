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
package io.silverware.microservices.providers.camel;

import io.silverware.microservices.Context;
import io.silverware.microservices.util.BootUtil;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class CamelMicroserviceProviderTest {

   static CamelContext camelContext = new DefaultCamelContext();

   private static final Semaphore semaphore = new Semaphore(0);

   @Test
   public void camelMicroserviceProviderTest() throws Exception {
      final String message = "But all the clocks in the city\n"
            + "   Began to whir and chime:\n"
            + "‘O let not Time deceive you,\n"
            + "   You cannot conquer Time.";
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName());
      platform.start();

      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES), "Timed-out while waiting for the camel route deployment."); // wait for the route to be deployed

      CamelContext context = (CamelContext) bootUtil.getContext().getProperties().get(CamelMicroserviceProvider.CAMEL_CONTEXT);
      Endpoint endpoint = context.getEndpoint("direct:response");
      final StringBuilder response = new StringBuilder();
      endpoint.createConsumer(new Processor() {
         @Override
         public void process(final Exchange exchange) throws Exception {
            response.append(exchange.getIn().getBody().toString());
         }
      }).start();

      ProducerTemplate template = context.createProducerTemplate();
      template.sendBody("direct:test", message);

      Assert.assertEquals(response.toString(), message);

      platform.interrupt();
      platform.join();
   }

   @Test
   public void shouldCreateCamelContextUsingTheFactory() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName());
      platform.start();

      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES), "Timed-out while waiting for the camel route deployment."); // wait for the route to be deployed

      CamelContext context = (CamelContext) bootUtil.getContext().getProperties().get(CamelMicroserviceProvider.CAMEL_CONTEXT);
      Assert.assertSame(context, camelContext);

      platform.interrupt();
      platform.join();
   }

   public static class CamelRoute extends RouteBuilder {

      @Override
      public void configure() throws Exception {
         from("direct:test").to("log:test").to("direct:response");

         semaphore.release();
      }
   }

   public static class TestCamelContextFactory implements CamelContextFactory {

      @Override
      public CamelContext createCamelContext(final Context context) {
         return camelContext;
      }

   }

}