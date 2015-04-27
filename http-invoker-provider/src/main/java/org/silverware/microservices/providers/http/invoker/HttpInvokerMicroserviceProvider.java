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
package org.silverware.microservices.providers.http.invoker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silverware.microservices.Context;
import org.silverware.microservices.providers.MicroserviceProvider;
import org.silverware.microservices.silver.HttpServerSilverService;
import org.silverware.microservices.silver.MonitoringSilverService;
import org.silverware.microservices.silver.http.ServletDescriptor;
import org.silverware.microservices.util.Utils;

import java.util.Collections;
import java.util.Properties;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class HttpInvokerMicroserviceProvider implements MicroserviceProvider, HttpInvokerSilverService {

   private static final Logger log = LogManager.getLogger(HttpInvokerMicroserviceProvider.class);

   private Context context;
   private HttpServerSilverService http;

   @Override
   public void initialize(final Context context) {
      this.context = context;

      context.getProperties().putIfAbsent(INVOKER_URL, "invoker");
   }

   @Override
   public Context getContext() {
      return context;
   }

   @Override
   public void run() {
      try {
         log.info("Hello from Http Invoker microservice provider!");

         try {
            if (log.isDebugEnabled()) {
               log.debug("Waiting for the Http Microservice provider.");
            }

            while (!Thread.currentThread().isInterrupted()) {

               if (http == null) {
                  http = (HttpServerSilverService) context.getProvider(HttpServerSilverService.class);

                  if (http != null) {
                     if (log.isDebugEnabled()) {
                        log.debug("Discovered Http Silverservice: " + http.getClass().getName());
                     }

                     log.info("Deploying Http Invoker...");

                     http.deployServlet((String) context.getProperties().get(INVOKER_URL), "", Collections.singletonList(getServletDescriptor()));

                     final String invokerUrl = "http://" + context.getProperties().get(HttpServerSilverService.HTTP_SERVER_ADDRESS) + ":" +
                           context.getProperties().get(HttpServerSilverService.HTTP_SERVER_PORT) + "/" +
                           context.getProperties().get(INVOKER_URL) + "/";

                     if (log.isTraceEnabled()) {
                        log.trace("Waiting for invoker to appear at {}", monitorUrl);
                     }

                     if (!Utils.waitForHttp(monitorUrl, 200)) {
                        throw new InterruptedException("Unable to start Http Invoker.");
                     }
                  }
               }

               Thread.sleep(1000);
            }
         } catch (InterruptedException ie) {
            Utils.shutdownLog(log, ie);
         } finally {
         }
      } catch (Exception e) {
         log.error("Http Invoker microservice provider failed: ", e);
      }
   }

   private ServletDescriptor getServletDescriptor() {
      final Properties properties = new Properties();
      properties.setProperty("dispatcherClasses", org.jolokia.jsr160.Jsr160RequestDispatcher.class.getName());
      properties.setProperty("debug", "false");
      properties.setProperty("historyMaxEntries", "10");
      properties.setProperty("debugMaxEntries", "100");
      properties.setProperty("maxDepth", "15");
      properties.setProperty("maxCollectionSize", "1000");
      properties.setProperty("maxObjects", "0");
      properties.setProperty("detectorOptions", "{}");
      properties.setProperty("canonicalNaming", "true");
      properties.setProperty("includeStackTrace", "true");
      properties.setProperty("serializeException", "false");
      properties.setProperty("discoveryEnabled", "true");

      return null; //new ServletDescriptor("jolokia-agent", org.jolokia.http.AgentServlet.class, "/", properties);
   }
}
