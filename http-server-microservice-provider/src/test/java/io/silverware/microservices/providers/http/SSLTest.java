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
package io.silverware.microservices.providers.http;

import static org.assertj.core.api.Assertions.assertThat;

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.silver.HttpServerSilverService;
import io.silverware.microservices.util.BootUtil;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
   private static Client client;

   @BeforeClass
   public static void setUp() throws IOException {
      if (client == null) {
         client = ClientBuilder
               .newBuilder()
               .sslContext(
                     new SSLContextFactory(CLIENT_KEY_STORE, STORE_PASSWORD, CLIENT_TRUST_STORE, STORE_PASSWORD)
                           .createSSLContext())
               .build();
      }
   }

   @AfterClass
   public void cleanUp() {
      if (client != null) {
         client.close();
      }
   }

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
      verifyResult(platformProperties, platform);
   }

   @Test
   public void sslConfigurationAbsolutePathTest() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Map<String, Object> platformProperties = bootUtil.getContext().getProperties();

      platformProperties.put(HttpServerSilverService.HTTP_SERVER_PORT, 8282);
      platformProperties.put(HttpServerSilverService.HTTP_SERVER_SSL_ENABLED, "true");
      platformProperties.put(
            HttpServerSilverService.HTTP_SERVER_KEY_STORE,
            getCorrectPath("/" + HttpServerSilverService.DEFAULT_SSL_KEYSTORE));
      platformProperties.put(
            HttpServerSilverService.HTTP_SERVER_KEY_STORE_PASSWORD,
            HttpServerSilverService.DEFAULT_SSL_STORE_PASSWORD);
      platformProperties.put(
            HttpServerSilverService.HTTP_SERVER_TRUST_STORE,
            getCorrectPath("/" + HttpServerSilverService.DEFAULT_SSL_TRUSTSTORE));
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
      verifyResult(platformProperties, platform);
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
      verifyResult(platformProperties, platform);
   }

   @Test
   public void httpToHttpsRedirectTest() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Map<String, Object> platformProperties = bootUtil.getContext().getProperties();
      platformProperties.put(HttpServerSilverService.HTTP_SERVER_SSL_ENABLED, "true");

      final Thread platform = bootUtil.getMicroservicePlatform(
            this.getClass().getPackage().getName(),
            CdiMicroserviceProvider.class.getPackage().getName());
      platform.start();

      Response response = null;
      try {
         while (bootUtil.getContext().getProperties().get(CdiMicroserviceProvider.BEAN_MANAGER) == null
               || !((HttpServerSilverService) bootUtil.getContext().getProvider(HttpServerSilverService.class))
               .isDeployed()) {
            Thread.sleep(200);
         }
         response = client
               .target(new SilverWareURI(platformProperties).httpREST() + "/sslservice/hello")
               .request(MediaType.TEXT_PLAIN).get();
         assertThat(response.getStatus())
               .as("Response should have status 302 Found as HTTP is redirected to HTTPS.")
               .isEqualTo(Response.Status.FOUND.getStatusCode());
      } finally {
         if (response != null) {
            response.close();
         }
         platform.interrupt();
         platform.join(0);
      }
   }

   private void verifyResult(final Map<String, Object> platformProperties, final Thread platform)
         throws InterruptedException {
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
         platform.interrupt();
         platform.join(0);
      }
      Thread.sleep(100);
   }

   String getCorrectPath(final String path) throws URISyntaxException {
      final URI uri = getClass().getResource(path).toURI();
      return new File(uri).getAbsolutePath();
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
