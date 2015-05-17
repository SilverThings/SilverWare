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
package org.silverware.microservices.providers.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.apache.camel.TypeConverter;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.component.properties.PropertiesParser;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silverware.microservices.Context;
import org.silverware.microservices.MicroserviceMetaData;
import org.silverware.microservices.providers.MicroserviceProvider;
import org.silverware.microservices.silver.CamelSilverService;
import org.silverware.microservices.util.DeployStats;
import org.silverware.microservices.util.DeploymentScanner;
import org.silverware.microservices.util.Utils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class CamelMicroserviceProvider implements MicroserviceProvider, CamelSilverService {

   private static final Logger log = LogManager.getLogger(CamelMicroserviceProvider.class);

   private Context context;
   private final CamelContext camelContext = new DefaultCamelContext();
   private final List<RouteBuilder> routes = new ArrayList<>();
   private final DeployStats stats = new DeployStats();

   @Override
   public void initialize(final Context context) {
      this.context = context;
      context.getProperties().put(CAMEL_CONTEXT, camelContext);

      @SuppressWarnings("unchecked")
      final Set<Class<RouteBuilder>> routeBuilders = (Set<Class<RouteBuilder>>) DeploymentScanner.getContextInstance(context).lookupSubtypes(RouteBuilder.class);
      if (log.isDebugEnabled()) {
         log.debug("Initializing Camel routes...");
      }
      stats.setFound(routeBuilders.size());

      for (Class<RouteBuilder> clazz : routeBuilders) {
         if (log.isDebugEnabled()) {
            log.debug("Creating Camel route: " + clazz.getName());
         }

         if (clazz.getName().equals(AdviceWithRouteBuilder.class.getName())) {
            if (log.isDebugEnabled()) {
               log.debug("Skipping " + clazz.getName() + ". This is an internal Camel route.");
            }
            stats.incSkipped();
         } else {
            try {
               final Constructor c = clazz.getConstructor();
               final RouteBuilder r = (RouteBuilder) c.newInstance();

               routes.add(r);
            } catch (Error | Exception e) {
               log.error("Cannot initialize Camel route: " + clazz.getName(), e);
            }
         }
      }
   }

   @Override
   public Context getContext() {
      return context;
   }

   @Override
   public void run() {
      if (routes.size() > 0) {
         try {
            log.info("Hello from Camel microservice provider!");

            for (final RouteBuilder builder : routes) {
               try {
                  camelContext.addRoutes(builder);
                  stats.incDeployed();
               } catch (Exception e) {
                  log.warn("Unable to start Camel route " + builder.getClass().getName(), e);
                  stats.incSkipped();
               }
            }

            log.info("Total Camel routes " + stats.toString() + ".");

            camelContext.start();

            try {
               while (!Thread.currentThread().isInterrupted()) {
                  Thread.sleep(1000);
               }
            } catch (InterruptedException ie) {
               Utils.shutdownLog(log, ie);
            } finally {
               camelContext.stop();
            }
         } catch (Exception e) {
            log.error("Camel microservice provider failed: ", e);
         }
      } else {
         log.warn("No routes to start. Camel microservice provider is terminated.");
      }
   }

   @Override
   public Set<Object> lookupMicroservice(final MicroserviceMetaData metaData) {
      if (Route.class.isAssignableFrom(metaData.getType())) {
         return Collections.singleton(camelContext.getRoute(metaData.getName()));
      } else if (Endpoint.class.isAssignableFrom(metaData.getType())) {
         return Collections.singleton(camelContext.getEndpoint(metaData.getName()));
      } else if (TypeConverter.class.isAssignableFrom(metaData.getType())) {
         return Collections.singleton(camelContext.getTypeConverter());
      } else if (Component.class.isAssignableFrom(metaData.getType())) {
         return Collections.singleton(camelContext.getComponent(metaData.getName()));
      } else if (ConsumerTemplate.class.isAssignableFrom(metaData.getType())) {
         return Collections.singleton(camelContext.createConsumerTemplate());
      } else if (ProducerTemplate.class.isAssignableFrom(metaData.getType())) {
         return Collections.singleton(camelContext.createProducerTemplate());
      }

      return new HashSet<>();
   }

}
