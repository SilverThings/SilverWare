package io.silverware.microservices.providers.http;

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.silver.HttpServerSilverService;
import io.silverware.microservices.util.BootUtil;
import org.testng.annotations.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SSLTest {
   private static final String CLIENT_KEY_STORE = "silverware-client.keystore";
   private static final String CLIENT_TRUST_STORE = "silverware-client.truststore";
   private static final String STORE_PASSWORD = "silverware";

   @Test
   public void defaultSslConfigurationTest() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Map<String, Object> platformProperties = bootUtil.getContext().getProperties();
      platformProperties.put(HttpServerSilverService.HTTP_SERVER_PORT, 8282);
      platformProperties.put(HttpServerSilverService.HTTP_SERVER_SSL_ENABLED, "true");

      final Thread platform = bootUtil.getMicroservicePlatform(
              this.getClass().getPackage().getName(),
              CdiMicroserviceProvider.class.getPackage().getName());
      platform.start();

      while (bootUtil.getContext().getProperties().get(CdiMicroserviceProvider.BEAN_MANAGER) == null) {
         Thread.sleep(200);
      }

      final Client client = ClientBuilder
              .newBuilder()
              .sslContext(
                      new SSLContextFactory(CLIENT_KEY_STORE, STORE_PASSWORD, CLIENT_TRUST_STORE, STORE_PASSWORD)
                              .createSSLContext())
              .build();
      verifyResult(platformProperties, platform, client);
   }

   @Test
   public void sslConfigurationAbsolutePathTest() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Map<String, Object> platformProperties = bootUtil.getContext().getProperties();

      platformProperties.put(HttpServerSilverService.HTTP_SERVER_PORT, 8282);
      platformProperties.put(HttpServerSilverService.HTTP_SERVER_SSL_ENABLED, "true");
      platformProperties.put(
              HttpServerSilverService.HTTP_SERVER_KEY_STORE,
              getCorrectPaht("/" + HttpServerSilverService.DEFAULT_SSL_KEYSTORE));
      platformProperties.put(
              HttpServerSilverService.HTTP_SERVER_KEY_STORE_PASSWORD,
              HttpServerSilverService.DEFAULT_SSL_STORE_PASSWORD);
      platformProperties.put(
              HttpServerSilverService.HTTP_SERVER_TRUST_STORE,
              getCorrectPaht("/" + HttpServerSilverService.DEFAULT_SSL_TRUSTSTORE));
      platformProperties.put(
              HttpServerSilverService.HTTP_SERVER_TRUST_STORE_PASSWORD,
              HttpServerSilverService.DEFAULT_SSL_STORE_PASSWORD);

      final Thread platform = bootUtil.getMicroservicePlatform(
              this.getClass().getPackage().getName(),
              CdiMicroserviceProvider.class.getPackage().getName());
      platform.start();

      while (bootUtil.getContext().getProperties().get(CdiMicroserviceProvider.BEAN_MANAGER) == null) {
         Thread.sleep(200);
      }

      final Client client = ClientBuilder
              .newBuilder()
              .sslContext(
                      new SSLContextFactory(CLIENT_KEY_STORE, STORE_PASSWORD, CLIENT_TRUST_STORE, STORE_PASSWORD)
                              .createSSLContext())
              .build();
      verifyResult(platformProperties, platform, client);
   }


   String getCorrectPaht(String path) throws URISyntaxException {
      URI uri = getClass().getResource(path).toURI();
      return new File(uri).getAbsolutePath();
   }

   @Test
   public void sslConfigurationResourcePathTest() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Map<String, Object> platformProperties = bootUtil.getContext().getProperties();
      platformProperties.put(HttpServerSilverService.HTTP_SERVER_PORT, 8282);
      platformProperties.put(HttpServerSilverService.HTTP_SERVER_SSL_ENABLED, "true");
      platformProperties
              .put(HttpServerSilverService.HTTP_SERVER_KEY_STORE, HttpServerSilverService.DEFAULT_SSL_KEYSTORE);
      platformProperties.put(
              HttpServerSilverService.HTTP_SERVER_KEY_STORE_PASSWORD,
              HttpServerSilverService.DEFAULT_SSL_STORE_PASSWORD);
      platformProperties
              .put(HttpServerSilverService.HTTP_SERVER_TRUST_STORE, HttpServerSilverService.DEFAULT_SSL_TRUSTSTORE);
      platformProperties.put(
              HttpServerSilverService.HTTP_SERVER_TRUST_STORE_PASSWORD,
              HttpServerSilverService.DEFAULT_SSL_STORE_PASSWORD);

      final Thread platform = bootUtil.getMicroservicePlatform(
              this.getClass().getPackage().getName(),
              CdiMicroserviceProvider.class.getPackage().getName());
      platform.start();

      while (bootUtil.getContext().getProperties().get(CdiMicroserviceProvider.BEAN_MANAGER) == null) {
         Thread.sleep(200);
      }

      final Client client = ClientBuilder
              .newBuilder()
              .sslContext(
                      new SSLContextFactory(CLIENT_KEY_STORE, STORE_PASSWORD, CLIENT_TRUST_STORE, STORE_PASSWORD)
                              .createSSLContext())
              .build();
      verifyResult(platformProperties, platform, client);
   }


   private void verifyResult(Map<String, Object> platformProperties, Thread platform, Client client) throws InterruptedException {
      try {
         Thread.sleep(100);
         assertThat(client
                 .target(new SilverWareURI(platformProperties).httpsREST() + "/sslservice/hello")
                 .request(MediaType.TEXT_PLAIN)
                 .get()
                 .readEntity(String.class))
                 .as("Rest microservice should return 'Hello from SSL.")
                 .isEqualTo("Hello from SSL.");
      } finally {
         client.close();
         platform.interrupt();
         platform.join(0);
      }
   }

   @Path("sslservice")
   @Microservice
   public static class SSLService {
      @GET
      @Produces(MediaType.TEXT_PLAIN)
      @Path("hello")
      public Response sayHello() {
         return Response.ok("Hello from SSL.").build();
      }
   }
}
