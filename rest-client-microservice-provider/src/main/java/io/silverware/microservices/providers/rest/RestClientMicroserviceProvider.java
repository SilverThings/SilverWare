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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.providers.http.SilverWareURI;
import io.silverware.microservices.providers.rest.annotation.ServiceConfiguration;
import io.silverware.microservices.providers.rest.api.RestService;
import io.silverware.microservices.providers.rest.internal.DefaultRestService;
import io.silverware.microservices.silver.RestClientSilverService;
import io.silverware.microservices.util.Utils;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class RestClientMicroserviceProvider implements MicroserviceProvider, RestClientSilverService {

   private static final Logger log = LogManager.getLogger(RestClientMicroserviceProvider.class);

   private Context context;
   private Client client;
   private SilverWareURI silverWareURI;

   @Override
   public void initialize(final Context context) {
      this.context = context;
      this.client = ClientBuilder.newClient();
      this.silverWareURI = new SilverWareURI(this.context.getProperties());
   }

   @Override
   public Context getContext() {
      return context;
   }

   @Override
   public void run() {
      log.info("Hello from REST client microservice provider!");

      try {
         while (!Thread.currentThread().isInterrupted()) {
            Thread.sleep(1000);
         }
      } catch (InterruptedException ie) {
         Utils.shutdownLog(log, ie);
      } finally {
         log.info("Closing Rest client...");
         this.client.close();
      }
   }

   private ServiceConfiguration getConfigurationAnnotation(final Set<Annotation> annotations) {
      for (Annotation a : annotations) {
         if (a instanceof ServiceConfiguration) {
            return (ServiceConfiguration) a;
         }
      }

      return null;
   }

   @Override
   public Set<Object> lookupMicroservice(final MicroserviceMetaData metaData) {
      ServiceConfiguration configuration = getConfigurationAnnotation(metaData.getAnnotations());
      if (RestService.class.isAssignableFrom(metaData.getType()) || metaData.getType().isInterface()) {
         if (configuration != null) {
            return Collections.singleton(initRestService(configuration));
         } else {
            log.warn("Attempt to inject RestService without ServiceConfiguration qualifier.");
         }

      }

      return new HashSet<>();
   }

   private RestService initRestService(final ServiceConfiguration configuration) {
      if (configuration.endpoint().isEmpty()) {
         log.warn(
               "Endpoint for the injected Rest service is not provided, provide endpoint or specify your custom path "
                     + "for the Rest service.");
      }
      if ("default".equals(configuration.type())) {
         return new DefaultRestService(this.client.target(silverWareURI.httpREST() + "/" + configuration.endpoint()));
      }
      throw new UnsupportedOperationException(String.format(
            "Requested Rest service implementation: %s is no provided. Currently, we support only default service "
                  + "implementation", configuration.type()));
   }

}
