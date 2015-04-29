package org.silverware.microservices.providers.http.invoker;

import org.codehaus.jackson.map.ObjectMapper;
import org.silverware.microservices.MicroserviceMetaData;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import javax.enterprise.inject.Alternative;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class HttpInvokerMicroserviceProviderTest {

   private HttpInvokerSilverService httpInvokerSilverService = null;
   private ObjectMapper mapper = new ObjectMapper();

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

      String urlBase = "http://" + platformProperties.get(HttpServerSilverService.HTTP_SERVER_ADDRESS) + ":" +
            platformProperties.get(HttpServerSilverService.HTTP_SERVER_PORT) + "/" +
            platformProperties.get(HttpInvokerSilverService.INVOKER_URL) + "/";

      Assert.assertTrue(Utils.waitForHttp(urlBase, 204));

      HttpURLConnection con = (HttpURLConnection) new URL(urlBase + "query").openConnection();
      con.setRequestMethod("POST");
      con.setDoInput(true);
      con.setDoOutput(true);
      con.connect();

      MicroserviceMetaData metaData = new MicroserviceMetaData("sumService", SumService.class, Collections.emptySet());
      mapper.writeValue(con.getOutputStream(), metaData);

      System.out.println(con.getResponseMessage());

      Thread.sleep(10000);

      platform.interrupt();
      platform.join();
   }

   @Microservice
   public static class SumService {

      public long sum(short a, int b) {
         return a + b;
      }
   }

}