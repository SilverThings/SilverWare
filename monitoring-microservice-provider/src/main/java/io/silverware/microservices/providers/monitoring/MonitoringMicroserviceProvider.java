/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2015 - 2017 the original author or authors.
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
package io.silverware.microservices.providers.monitoring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.silverware.microservices.Context;
import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.silver.HttpServerSilverService;
import io.silverware.microservices.silver.MonitoringSilverService;
import io.silverware.microservices.silver.http.ServletDescriptor;
import io.silverware.microservices.util.Utils;

import java.util.Collections;
import java.util.Properties;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class MonitoringMicroserviceProvider implements MicroserviceProvider, MonitoringSilverService {

   private static final Logger log = LogManager.getLogger(MonitoringMicroserviceProvider.class);

   private Context context;
   private HttpServerSilverService http;

   @Override
   public void initialize(final Context context) {
      this.context = context;

      context.getProperties().putIfAbsent(MONITORING_URL, "monitor");
   }

   @Override
   public Context getContext() {
      return context;
   }

   @Override
   public void run() {
      try {
         log.info("Hello from Monitoring microservice provider!");

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

                     log.info("Deploying monitoring agent...");

                     http.deployServlet((String) context.getProperties().get(MONITORING_URL), "", Collections.singletonList(getServletDescriptor()));

                     final String monitorUrl = "http://" + context.getProperties().get(HttpServerSilverService.HTTP_SERVER_ADDRESS) + ":" +
                           context.getProperties().get(HttpServerSilverService.HTTP_SERVER_PORT) + "/" +
                           context.getProperties().get(MONITORING_URL) + "/";

                     if (log.isTraceEnabled()) {
                        log.trace("Waiting for monitor to appear at {}", monitorUrl);
                     }

                     if (!Utils.waitForHttp(monitorUrl, 200)) {
                        throw new InterruptedException("Unable to start monitoring agent.");
                     }
                  }
               }

               Thread.sleep(1000);
            }
         } catch (InterruptedException ie) {
            Utils.shutdownLog(log, ie);
         }
      } catch (Exception e) {
         log.error("Monitoring microservice provider failed: ", e);
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

      return new ServletDescriptor("jolokia-agent", org.jolokia.http.AgentServlet.class, "/", properties);
   }
}
