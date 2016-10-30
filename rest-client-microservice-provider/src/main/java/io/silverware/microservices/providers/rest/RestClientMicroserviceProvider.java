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

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.providers.rest.annotation.ServiceConfiguration;
import io.silverware.microservices.silver.RestClientSilverService;
import io.silverware.microservices.util.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides a wrapper for calling arbitrary REST service that is deployed anywhere.
 * The REST service is injected into a SilverWare microservice as an interface that describes the REST
 * service itself.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 * @author Radek Koubsky (radekkoubsky@gmail.com)
 */
public class RestClientMicroserviceProvider implements MicroserviceProvider, RestClientSilverService {
   private static final Logger log = LogManager.getLogger(RestClientMicroserviceProvider.class);
   private Context context;
   private ResteasyClient client;

   @Override
   public void initialize(final Context context) {
      this.context = context;
      this.client = new ResteasyClientBuilder().build();
   }

   @Override
   public Context getContext() {
      return this.context;
   }

   @Override
   public void run() {
      log.info("Hello from REST client microservice provider!");

      try {
         while (!Thread.currentThread().isInterrupted()) {
            Thread.sleep(1000);
         }
      } catch (final InterruptedException ie) {
         Utils.shutdownLog(log, ie);
      } finally {
         log.info("Closing the Rest client...");
         this.client.close();
      }
   }

   private ServiceConfiguration getConfigurationAnnotation(final Set<Annotation> annotations) {
      for (final Annotation a : annotations) {
         if (a instanceof ServiceConfiguration) {
            return (ServiceConfiguration) a;
         }
      }

      return null;
   }

   @Override
   public Set<Object> lookupMicroservice(final MicroserviceMetaData metaData) {
      final ServiceConfiguration configuration = getConfigurationAnnotation(metaData.getAnnotations());
      if (configuration != null) {
         return Collections.singleton(initRestService(metaData, configuration));
      }

      return new HashSet<>();
   }

   private Object initRestService(final MicroserviceMetaData metaData, final ServiceConfiguration configuration) {
      if (configuration.endpoint().isEmpty()) {
         log.warn("The endpoint for the injected Rest service: {} is not provided. Specify the endpoint within the "
                     + "%s annotation.",
               metaData.getType(), configuration.getClass());
      }

      final Object restService = this.client.target(configuration.endpoint()).proxy(metaData.getType());
      log.debug("Proxy for the Rest service: {} successfully created.", metaData.getType());

      return restService;
   }

}
