package org.silverware.microservices.providers.http.invoker;

import org.silverware.microservices.annotations.Microservice;
import org.silverware.microservices.annotations.MicroserviceReference;
import org.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import org.silverware.microservices.providers.http.HttpServerMicroserviceProvider;
import org.silverware.microservices.silver.HttpInvokerSilverService;
import org.silverware.microservices.silver.HttpServerSilverService;
import org.silverware.microservices.util.BootUtil;
import org.silverware.microservices.util.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.util.Map;
import javax.enterprise.inject.Alternative;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class HttpInvokerMicroserviceProviderTest {

   private HttpInvokerSilverService httpInvokerSilverService = null;

   @Test
   public void testHttpInvoker() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Map<String, Object> platformProperties = bootUtil.getContext().getProperties();
      final Thread platform = bootUtil.getMicroservicePlatform(); //this.getClass().getPackage().getName(), HttpServerMicroserviceProvider.class.getPackage().getName(), CdiMicroserviceProvider.class.getPackage().getName());
      platform.start();

      while (httpInvokerSilverService == null) {
         httpInvokerSilverService = (HttpInvokerSilverService) bootUtil.getContext().getProvider(HttpInvokerSilverService.class);
         Thread.sleep(200);
      }

      Assert.assertTrue(Utils.waitForHttp("http://" + platformProperties.get(HttpServerSilverService.HTTP_SERVER_ADDRESS) + ":" +
            platformProperties.get(HttpServerSilverService.HTTP_SERVER_PORT) + "/" +
            platformProperties.get(HttpInvokerSilverService.INVOKER_URL) + "/", 204));

      Thread.sleep(10000);

      platform.interrupt();
      platform.join();
   }

   @Microservice
   public static class SumService {

      @MicroserviceReference
      private Serializable test;

      public long sum(short a, int b) {
         return a + b;
      }
   }

}