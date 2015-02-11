package org.silverware.microservices.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.silverware.microservices.util.BootUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CamelMicroserviceTest {

   private static final Semaphore semaphore = new Semaphore(0);

   @Test
   public void camelMicroserviceTest() throws Exception {
      final String message = "But all the clocks in the city\n"
            + "   Began to whir and chime:\n"
            + "‘O let not Time deceive you,\n"
            + "   You cannot conquer Time.";
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName());
      platform.start();

      Assert.assertTrue(semaphore.tryAcquire(5, TimeUnit.SECONDS), "Timed-out while waiting for the camel route deployment."); // wait for the route to be deployed

      CamelContext context = (CamelContext) bootUtil.getContext().getProperties().get(CamelMicroservice.CAMEL_CONTEXT);
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

   public static class CamelRoute extends RouteBuilder {

      @Override
      public void configure() throws Exception {
         from("direct:test").to("log:test").to("direct:response");

         semaphore.release();
      }
   }
}