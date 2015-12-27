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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.silverware.microservices.Context;
import io.silverware.microservices.SilverWareException;
import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.silver.HttpServerSilverService;
import io.silverware.microservices.silver.http.ServletDescriptor;
import io.silverware.microservices.util.Utils;

import java.util.List;
import javax.servlet.Servlet;
import javax.servlet.ServletException;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class HttpServerMicroserviceProvider implements MicroserviceProvider, HttpServerSilverService {

   private static final Logger log = LogManager.getLogger(HttpServerMicroserviceProvider.class);

   private Context context;
   private Undertow server;
   private PathHandler pathHandler;

   @Override
   public void initialize(final Context context) {
      this.context = context;

      context.getProperties().putIfAbsent(HTTP_SERVER_PORT, 8080);
      context.getProperties().putIfAbsent(HTTP_SERVER_ADDRESS, "localhost");

      pathHandler = Handlers.path();
      server = Undertow.builder()
                       .addHttpListener((int) context.getProperties().get(HTTP_SERVER_PORT), (String) context.getProperties().get(HTTP_SERVER_ADDRESS))
                       .setHandler(pathHandler)
                       .build();

      context.getProperties().put(HTTP_SERVER, server);
   }

   @Override
   public Context getContext() {
      return context;
   }

   @Override
   public void deployServlet(final String contextPath, final String deploymentName, final List<ServletDescriptor> servletDescriptors) throws SilverWareException {
      DeploymentInfo servletBuilder = Servlets.deployment()
                                              .setClassLoader(this.getClass().getClassLoader())
                                              .setContextPath(contextPath)
                                              .setDeploymentName(deploymentName);
      if (servletDescriptors != null) {
         servletDescriptors.forEach(servletDescriptor -> {
            ServletInfo servletInfo = Servlets.servlet(servletDescriptor.getName(), (Class<Servlet>) servletDescriptor.getServletClass());
            servletInfo.addMapping(servletDescriptor.getMapping());
            servletDescriptor.getProperties().forEach((key, value) -> servletInfo.addInitParam((String) key, (String) value));

            servletBuilder.addServlet(servletInfo);
         });
      }

      DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
      manager.deploy();

      try {
         pathHandler.addPrefixPath(contextPath, manager.start());
      } catch (ServletException se) {
         throw new SilverWareException(String.format("Unable to deploy '%s' at context path '%s':", deploymentName, contextPath), se);
      }
   }

   @Override
   public void run() {
      try {
         log.info("Hello from Http Server microservice provider!");

         try {
            server.start();
            log.info("Started Http Server.");

            while (!Thread.currentThread().isInterrupted()) {
               Thread.sleep(1000);
            }
         } catch (InterruptedException ie) {
            Utils.shutdownLog(log, ie);
         } finally {
            server.stop();
         }
      } catch (Exception e) {
         log.error("Http Server microservice provider failed: ", e);
      }
   }
}
