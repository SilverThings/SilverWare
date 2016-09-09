package io.silverware.microservices.providers.http;

import static org.assertj.core.api.Assertions.assertThat;

import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.silver.HttpServerSilverService;
import io.silverware.microservices.util.BootUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

public class SSLTest {
   private static final Logger log = LogManager.getLogger(SSLTest.class);
   private static final String CLIENT_KEY_STORE = "silverware-client.keystore";
   private static final String CLIENT_TRUST_STORE = "silverware-client.truststore";
   private static final String STORE_PASSWORD = "silverware";
   private Map<String, Object> platformProperties;
   private Client client;
   private Thread platform;

   @BeforeClass
   public void setUpPlatforn() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      this.platformProperties = bootUtil.getContext().getProperties();
      this.platformProperties.put(HttpServerSilverService.HTTP_SERVER_PORT, 8282);
      this.platformProperties.put(HttpServerSilverService.HTTP_SERVER_TRUST_STORE, "silverware-server.truststore");
      this.platformProperties.put(HttpServerSilverService.HTTP_SERVER_TRUST_STORE_PASSWORD, STORE_PASSWORD);
      this.platformProperties.put(HttpServerSilverService.HTTP_SERVER_KEY_STORE, "silverware-server.keystore");
      this.platformProperties.put(HttpServerSilverService.HTTP_SERVER_KEY_STORE_PASSWORD, "silverware");

      this.platform = bootUtil.getMicroservicePlatform(
            this.getClass().getPackage().getName(),
            CdiMicroserviceProvider.class.getPackage().getName());
      this.platform.start();

      while (bootUtil.getContext().getProperties().get(CdiMicroserviceProvider.BEAN_MANAGER) == null) {
         Thread.sleep(200);
      }

      this.client = ClientBuilder
            .newBuilder()
            .sslContext(
                  new SSLContextFactory(CLIENT_KEY_STORE, STORE_PASSWORD, CLIENT_TRUST_STORE, STORE_PASSWORD)
                        .createSSLContext())
            .build();
   }

   @AfterClass
   public void tearDown() throws InterruptedException {
      this.client.close();
      this.platform.interrupt();
      this.platform.join();
   }

   @Test
   public void microserviceSslTest() throws Exception {
      assertThat(
            this.client
                  .target(
                        new SilverWareURI(this.platformProperties).httpsREST() + "/helloservice/test_query_params?name=Radek&age=25")
                  .request(MediaType.TEXT_PLAIN)
                  .get()
                  .readEntity(String.class)).as("Rest microservice should return 'Radek;25'").isEqualTo("Radek;25");

   }
}
