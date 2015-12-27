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
package io.silverware.microservices.providers.http.invoker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.silverware.microservices.Context;
import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.providers.http.invoker.internal.HttpInvokerServlet;
import io.silverware.microservices.silver.HttpInvokerSilverService;
import io.silverware.microservices.silver.HttpServerSilverService;
import io.silverware.microservices.silver.ProvidingSilverService;
import io.silverware.microservices.silver.http.ServletDescriptor;
import io.silverware.microservices.util.Utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class HttpInvokerMicroserviceProvider implements MicroserviceProvider, HttpInvokerSilverService {

   private static final Logger log = LogManager.getLogger(HttpInvokerMicroserviceProvider.class);

   private Context context;
   private HttpServerSilverService http;
   private Set<ProvidingSilverService> microserviceProviders = new HashSet<>();

   @Override
   public void initialize(final Context context) {
      this.context = context;
      HttpInvokerServlet.setContext(context);

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
         context.getAllProviders(ProvidingSilverService.class).stream().forEach(silverService -> microserviceProviders.add((ProvidingSilverService) silverService));

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
                        log.trace("Waiting for invoker to appear at {}", invokerUrl);
                     }

                     if (!Utils.waitForHttp(invokerUrl, 204)) {
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

      return new ServletDescriptor("http-invoker", HttpInvokerServlet.class, "/", properties);
   }

}
