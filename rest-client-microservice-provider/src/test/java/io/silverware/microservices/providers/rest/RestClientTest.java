package io.silverware.microservices.providers.rest;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.util.Map;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.providers.http.HttpServerMicroserviceProvider;
import io.silverware.microservices.providers.http.SilverWareURI;
import io.silverware.microservices.providers.rest.annotation.ServiceConfiguration;
import io.silverware.microservices.providers.rest.api.RestService;
import io.silverware.microservices.providers.rest.internal.DefaultRestService;
import io.silverware.microservices.silver.HttpServerSilverService;
import io.silverware.microservices.util.BootUtil;

import javax.ws.rs.client.Client;

/**
 * Created by rkoubsky on 10/5/16.
 */
public class RestClientTest {
   private static final Logger log = LogManager.getLogger(RestClientTest.class);

   @Test
   public void testClientService() throws InterruptedException {
      final BootUtil bootUtil = new BootUtil();
      final Map<String, Object> platformProperties = bootUtil.getContext().getProperties();
      platformProperties.put(HttpServerSilverService.HTTP_SERVER_PORT, 8282);
      platformProperties.put(HttpServerSilverService.HTTP_SERVER_SSL_ENABLED, "true");

      final Thread platform = bootUtil.getMicroservicePlatform(
            this.getClass().getPackage().getName(),
            CdiMicroserviceProvider.class.getPackage().getName(),
            HttpServerMicroserviceProvider.class.getPackage().getName());
      platform.start();

      while (bootUtil.getContext().getProperties().get(CdiMicroserviceProvider.BEAN_MANAGER) == null) {
         Thread.sleep(200);
      }
      Client client = ClientBuilder.newClient();

      log.info("Result of client service call:" + client
            .target(new SilverWareURI(platformProperties).httpREST() + "/client_service/hello").request()
            .get(String.class));
   }

   @Path("backend_service")
   @Microservice
   public static class BackendService {
      @Path("hello")
      @GET
      public String hello() {
         return "Hello from backend service!";
      }

   }

   @Path("client_service")
   @Microservice
   public static class ClientService {
      @Inject
      @MicroserviceReference
      @ServiceConfiguration(endpoint = "backend_service")
      RestService backendService;

      @Path("hello")
      @GET
      public String hello() {
         return backendService.target().path("hello").request().get(String.class);
      }

      public String hello2() {
         return backendService.target().path("hello").request().get(String.class);
      }
   }
}
