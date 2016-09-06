/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2010 - 2013 the original author or authors.
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

import io.silverware.microservices.Context;
import io.silverware.microservices.SilverWareException;
import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.providers.http.resteasy.SilverwareResourceFactory;
import io.silverware.microservices.silver.CdiSilverService;
import io.silverware.microservices.silver.HttpServerSilverService;
import io.silverware.microservices.silver.http.ServletDescriptor;
import io.silverware.microservices.util.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.resteasy.spi.ResteasyDeployment;

import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.servlet.Servlet;
import javax.ws.rs.Path;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class HttpServerMicroserviceProvider implements MicroserviceProvider, HttpServerSilverService {

   private static final Logger log = LogManager.getLogger(HttpServerMicroserviceProvider.class);

   private Context context;
   private UndertowJaxrsServer server;

   @Override
   public void initialize(final Context context) {
      this.context = context;

      context.getProperties().putIfAbsent(HTTP_SERVER_PORT, 8080);
      context.getProperties().putIfAbsent(HTTPS_SERVER_PORT, 10443);
      context.getProperties().putIfAbsent(HTTP_SERVER_ADDRESS, "localhost");
      context.getProperties().putIfAbsent(HTTP_SERVER_REST_CONTEXT_PATH, "/silverware");
      context.getProperties().putIfAbsent(HTTP_SERVER_REST_SERVLET_MAPPING_PREFIX, "rest");
      this.server = new UndertowJaxrsServer();
      context.getProperties().put(HTTP_SERVER, this.server);

   }

   @Override
   public Context getContext() {
      return this.context;
   }

   @Override
   @SuppressWarnings("unchecked")
   public void deployServlet(final String contextPath, final String deploymentName,
         final List<ServletDescriptor> servletDescriptors) throws SilverWareException {
      final DeploymentInfo servletBuilder = Servlets
            .deployment()
            .setClassLoader(this.getClass().getClassLoader())
            .setContextPath(contextPath)
            .setDeploymentName(deploymentName);
      if (servletDescriptors != null) {
         servletDescriptors.forEach(servletDescriptor -> {
            final ServletInfo servletInfo = Servlets
                  .servlet(servletDescriptor.getName(), (Class<Servlet>) servletDescriptor.getServletClass());
            servletInfo.addMapping(servletDescriptor.getMapping());
            servletDescriptor
                  .getProperties()
                  .forEach((key, value) -> servletInfo.addInitParam((String) key, (String) value));

            servletBuilder.addServlet(servletInfo);
         });
      }

      this.server.deploy(servletBuilder);
   }

   @Override
   public void run() {
      try {
         log.info("Hello from Http Server microservice provider!");
         try {
            this.server.start(
                  Undertow
                        .builder()
                        .addHttpListener(
                              (int) this.context.getProperties().get(HTTP_SERVER_PORT),
                              (String) this.context.getProperties().get(HTTP_SERVER_ADDRESS))
                        .addHttpsListener(
                              (int) this.context.getProperties().get(HTTPS_SERVER_PORT),
                              (String) this.context.getProperties().get(HTTP_SERVER_ADDRESS),
                              sslContext()));
            this.server.deploy(deploymentInfo());
            log.info("Started Http Server.");

            while (!Thread.currentThread().isInterrupted()) {
               Thread.sleep(1000);
            }
         } catch (final InterruptedException ie) {
            Utils.shutdownLog(log, ie);
         } catch (final Exception ie) {
            log.error("Error while initializing.", ie);
            ;
         } finally {
            this.server.stop();
         }
      } catch (final Exception e) {
         log.error("Http Server microservice provider failed: ", e);
      }
   }

   private DeploymentInfo deploymentInfo() throws InterruptedException {
      final ResteasyDeployment resteasyDeployment = new ResteasyDeployment();
      waitForCDIProvider();
      resteasyDeployment.setResourceFactories(resourceFactories());
      return this.server
            .undertowDeployment(
                  resteasyDeployment,
                  String.valueOf(this.context.getProperties().get(HTTP_SERVER_REST_SERVLET_MAPPING_PREFIX)))
            .setContextPath(String.valueOf(this.context.getProperties().get(HTTP_SERVER_REST_CONTEXT_PATH)))
            .setClassLoader(this.getClass().getClassLoader())
            .setDeploymentName("Silverware rest deployment");
   }

   /**
    * Waits until {@link CdiSilverService} is deployed, thus all Microservices have been registered to {@link Context}.
    */
   private void waitForCDIProvider() throws InterruptedException {
      while (this.context.getProvider(
            CdiSilverService.class) == null || !((CdiSilverService) this.context.getProvider(CdiSilverService.class))
                  .isDeployed()) {
         Thread.sleep(200);
      }
   }

   /**
    * Creates an instance of {@link SilverwareResourceFactory} for each Microservice
    * with the {@link Path} annotation.
    *
    * @return list of resource factories
    */
   private List<ResourceFactory> resourceFactories() {
      final List<ResourceFactory> factories = new ArrayList<>();
      this.context.getMicroservices().forEach(microservice -> {
         microservice.getAnnotations().forEach(annotation -> {
            if (annotation.annotationType().equals(Path.class)) {
               log.debug("Creating new SilverwareResourceFactory for the following microservice {}", microservice);
               factories.add(new SilverwareResourceFactory(this.context, microservice));
            }
         });
      });
      return factories;
   }

   private SSLContext sslContext() throws IOException {
      return new SSLContextFactory(
            (String) this.context.getProperties().get(HTTP_SERVER_KEY_STORE),
            (String) this.context.getProperties().get(HTTP_SERVER_KEY_STORE_PASSWORD),
            (String) this.context.getProperties().get(HTTP_SERVER_TRUST_STORE),
            (String) this.context.getProperties().get(HTTP_SERVER_TRUST_STORE_PASSWORD)).createSSLContext();
   }
}
