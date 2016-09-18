package io.silverware.microservices.providers.http;

import static org.assertj.core.api.Assertions.assertThat;

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.silver.HttpServerSilverService;
import io.silverware.microservices.util.BootUtil;

import org.testng.annotations.Test;

import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

      assertThat(client
            .target(new SilverWareURI(platformProperties).httpsREST() + "/sslservice/hello")
            .request(MediaType.TEXT_PLAIN)
            .get()
            .readEntity(String.class))
                  .as("Rest microservice should return 'Hello from SSL.")
                  .isEqualTo("Hello from SSL.");
      client.close();
      platform.interrupt();
      platform.join();
   }

   @Test
   public void sslConfigurationAbsolutePathTest() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Map<String, Object> platformProperties = bootUtil.getContext().getProperties();
      platformProperties.put(HttpServerSilverService.HTTP_SERVER_PORT, 8282);
      platformProperties.put(HttpServerSilverService.HTTP_SERVER_SSL_ENABLED, "true");
      platformProperties.put(
            HttpServerSilverService.HTTP_SERVER_KEY_STORE,
            getClass().getResource("/" + HttpServerSilverService.DEFAULT_SSL_KEYSTORE).getPath());
      platformProperties.put(
            HttpServerSilverService.HTTP_SERVER_KEY_STORE_PASSWORD,
            HttpServerSilverService.DEFAULT_SSL_STORE_PASSWORD);
      platformProperties.put(
            HttpServerSilverService.HTTP_SERVER_TRUST_STORE,
            getClass().getResource("/" + HttpServerSilverService.DEFAULT_SSL_TRUSTSTORE).getPath());
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

      assertThat(client
            .target(new SilverWareURI(platformProperties).httpsREST() + "/sslservice/hello")
            .request(MediaType.TEXT_PLAIN)
            .get()
            .readEntity(String.class))
                  .as("Rest microservice should return 'Hello from SSL.")
                  .isEqualTo("Hello from SSL.");
      client.close();
      platform.interrupt();
      platform.join();
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

      assertThat(client
            .target(new SilverWareURI(platformProperties).httpsREST() + "/sslservice/hello")
            .request(MediaType.TEXT_PLAIN)
            .get()
            .readEntity(String.class))
                  .as("Rest microservice should return 'Hello from SSL.")
                  .isEqualTo("Hello from SSL.");
      client.close();
      platform.interrupt();
      platform.join();
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
