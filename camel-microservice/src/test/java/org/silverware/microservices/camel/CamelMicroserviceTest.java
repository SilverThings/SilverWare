package org.silverware.microservices.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.silverware.microservices.Boot;
import org.testng.annotations.Test;

public class CamelMicroserviceTest {

   @Test
   public void camelMicroserviceTest() throws InterruptedException {
      Thread t = new Thread(new Runnable() {
         @Override
         public void run() {
            Boot.main();
         }
      });

      t.start();

      Thread.sleep(5000);

      CamelContext context = (CamelContext) System.getProperties().get(CamelMicroservice.CAMEL_CONTEXT);
      ProducerTemplate template = context.createProducerTemplate();
      template.sendBody("direct:test", "This is a test message");

      t.interrupt();
      t.join();
   }

   public static class CamelRoute extends RouteBuilder {

      @Override
      public void configure() throws Exception {
         from("direct:test").to("log:test");
      }
   }
}