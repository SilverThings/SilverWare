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
package io.silverware.microservices.providers.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.providers.http.HttpServerMicroserviceProvider;
import io.silverware.microservices.providers.http.SilverWareURI;
import io.silverware.microservices.providers.rest.annotation.ServiceConfiguration;
import io.silverware.microservices.silver.HttpServerSilverService;
import io.silverware.microservices.util.BootUtil;

/**
 * Created by rkoubsky on 10/5/16.
 */
public class RestClientMicroserviceProviderTest {
   private static final Logger log = LogManager.getLogger(RestClientMicroserviceProviderTest.class);
   private Map<String, Object> platformProperties;
   private Client client;
   private Thread platform;
   private SilverWareURI silverWareURI;

   @BeforeClass
   public void setUpPlatforn() throws InterruptedException {
      final BootUtil bootUtil = new BootUtil();
      this.platformProperties = bootUtil.getContext().getProperties();
      this.platformProperties.put(HttpServerSilverService.HTTP_SERVER_PORT, 8282);

      this.platform = bootUtil.getMicroservicePlatform(
            this.getClass().getPackage().getName(),
            CdiMicroserviceProvider.class.getPackage().getName(),
            HttpServerMicroserviceProvider.class.getPackage().getName());
      this.platform.start();
      this.silverWareURI = new SilverWareURI(this.platformProperties);

      while (bootUtil.getContext().getProperties().get(CdiMicroserviceProvider.BEAN_MANAGER) == null) {
         Thread.sleep(200);
      }
      log.info("Waiting for HTTP server provider.");
      TimeUnit.SECONDS.sleep(1);
      this.client = ClientBuilder.newClient();
   }

   @AfterClass
   public void tearDown() throws InterruptedException {
      this.client.close();
      this.platform.interrupt();
      this.platform.join();
   }

   @Test
   public void restServiceTest() throws InterruptedException {
      Assertions.assertThat(this.client
            .target(this.silverWareURI.httpREST() + "/client_service/hello").request()
            .get(String.class)).as("Rest service call should return 'Hello from backend service!'")
            .isEqualTo("Hello from backend service!");
   }

   @Test
   public void restServicePathParamTest() throws InterruptedException {
      Assertions.assertThat(this.client
            .target(this.silverWareURI.httpREST() + "/client_service/hello/5").request()
            .get(String.class)).as("Rest service call should return 'Hello from backend service with path param=5'")
            .isEqualTo("Hello from backend service with path param=5");
   }

   @Test
   public void restServiceReadEntityTest() throws InterruptedException {
      Assertions.assertThat(this.client
            .target(this.silverWareURI.httpREST() + "/client_service/book/title").request()
            .get(String.class)).as("Rest service call should return book title 'Java in Action'")
            .isEqualTo("Java in Action");
   }

   @Path("client_service")
   @Microservice
   public static class ClientService {
      @Inject
      @MicroserviceReference
      @ServiceConfiguration(endpoint = "http://localhost:8282/silverware/rest/backend_service")
      BackendServiceClient restService;

      @Path("hello")
      @GET
      public String hello() {
         return this.restService.hello();
      }

      @Path("hello/{id}")
      @GET
      public String helloWithParam(@PathParam("id") final int id) {
         return this.restService.getPathParam(id);
      }

      @Path("book/title")
      @GET
      public String getTitle() {
         return this.restService.getBook().getTitle();
      }
   }

   /**
    * This is an arbitrary REST service which can be implemented in any language and located anywhere in the Internet.
    * In this test, the Rest service is implemented in Java and is deployed in SilverWare.
    */
   @Microservice
   @Path("backend_service")
   public static class BackendService {
      @Path("hello")
      @GET
      @Produces("text/plain")
      public String hello() {
         return "Hello from backend service!";
      }

      @Path("hello/{id}")
      @GET
      @Produces("text/plain")
      public String getPathParam(@PathParam("id") final int id) {
         return "Hello from backend service with path param=" + id;
      }

      @Path("book")
      @GET
      @Produces(MediaType.APPLICATION_JSON)
      public Response getBook() {
         return Response.ok(new Book("Java in Action")).build();
      }
   }

   @Microservice
   public interface BackendServiceClient {
      @Path("hello")
      @GET
      @Produces("text/plain")
      public String hello();

      @Path("hello/{id}")
      @GET
      @Produces("text/plain")
      public String getPathParam(@PathParam("id") final int id);

      @Path("book")
      @GET
      @Produces(MediaType.APPLICATION_JSON)
      public Book getBook();
   }

   public static class Book {
      private final String title;

      public Book(@JsonProperty("title") final String title) {
         this.title = title;
      }

      public String getTitle() {
         return this.title;
      }

      @Override
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         }
         if (o == null || getClass() != o.getClass()) {
            return false;
         }

         final Book book = (Book) o;

         return this.title.equals(book.title);

      }

      @Override
      public int hashCode() {
         return this.title.hashCode();
      }
   }
}
