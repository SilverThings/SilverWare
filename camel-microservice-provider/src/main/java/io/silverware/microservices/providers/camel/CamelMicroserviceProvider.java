/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2015 the original author or authors.
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
package io.silverware.microservices.providers.camel;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.SilverWareException;
import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.silver.CamelSilverService;
import io.silverware.microservices.util.DeployStats;
import io.silverware.microservices.util.DeploymentScanner;
import io.silverware.microservices.util.Utils;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Endpoint;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.Route;
import org.apache.camel.TypeConverter;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RoutesDefinition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class CamelMicroserviceProvider implements MicroserviceProvider, CamelSilverService {

   private static final Logger log = LogManager.getLogger(CamelMicroserviceProvider.class);

   private Context context;
   private CamelContext camelContext;
   private final List<RouteBuilder> routes = new ArrayList<>();
   private Set<String> routeResources;
   private final DeployStats stats = new DeployStats();

   private void createCamelContext() throws SilverWareException {
      Set<Class<? extends CamelContextFactory>> camelContextFactories = DeploymentScanner.getContextInstance(context).lookupSubtypes(CamelContextFactory.class);

      if (camelContextFactories.size() >= 2) {
         throw new SilverWareException("More than one CamelContextFactories found.");
      } else if (camelContextFactories.size() == 1) {
         Class<? extends CamelContextFactory> clazz = camelContextFactories.iterator().next();
         try {
            CamelContextFactory camelContextFactory = clazz.newInstance();
            camelContext = camelContextFactory.createCamelContext(context);
         } catch (InstantiationException | IllegalAccessException e) {
            throw new SilverWareException(String.format("Cannot instantiate Camel context factory %s: ", clazz.getName()), e);
         }
      } else {
         camelContext = new DefaultCamelContext();
      }

      context.getProperties().put(CAMEL_CONTEXT, camelContext);
   }

   private void loadRoutesFromClasses() {
      final Set<Class<? extends RouteBuilder>> routeBuilders = DeploymentScanner.getContextInstance(context).lookupSubtypes(RouteBuilder.class);
      if (log.isDebugEnabled()) {
         log.debug("Initializing Camel route resources...");
      }
      stats.setFound(routeBuilders.size());

      for (Class<? extends RouteBuilder> clazz : routeBuilders) {
         if (log.isDebugEnabled()) {
            log.debug("Creating Camel route builder: " + clazz.getName());
         }

         if (clazz.getName().equals(AdviceWithRouteBuilder.class.getName())) {
            if (log.isDebugEnabled()) {
               log.debug("Skipping " + clazz.getName() + ". This is an internal Camel route builder.");
            }
            stats.incSkipped();
         } else {
            try {
               if (!Modifier.isAbstract(clazz.getModifiers())) {
                  final Constructor c = clazz.getConstructor();
                  final RouteBuilder r = (RouteBuilder) c.newInstance();
                  routes.add(r);
               } else {
                  stats.incSkipped();
               }
            } catch (Error | Exception e) {
               log.error("Cannot initialize Camel route builder: " + clazz.getName(), e);
            }
         }
      }
   }

   private void loadRoutesFromXml() {
      routeResources = DeploymentScanner.getContextInstance(context).lookupResources(".*camel-.*\\.xml");
   }

   @Override
   public void initialize(final Context context) {
      this.context = context;

      loadRoutesFromClasses();
      loadRoutesFromXml();
   }

   @Override
   public Context getContext() {
      return context;
   }

   @Override
   public void run() {
      if (routes.size() > 0 || routeResources.size() > 0) {
         try {
            log.info("Hello from Camel microservice provider!");

            createCamelContext();

            for (final RouteBuilder builder : routes) {
               try {
                  camelContext.addRoutes(builder);
                  stats.incDeployed();
               } catch (Exception e) {
                  log.warn("Unable to start Camel route " + builder.getClass().getName(), e);
                  stats.incSkipped();
               }
            }

            final ModelCamelContext model = (ModelCamelContext) camelContext;
            for (final String routeResource : routeResources) {
               try {
                  final RoutesDefinition definition = model.loadRoutesDefinition(this.getClass().getResourceAsStream("/" + routeResource));
                  model.addRouteDefinitions(definition.getRoutes());
                  stats.incDeployed();
               } catch (Exception e) {
                  log.warn(String.format("Cannot initialize routes in %s: ", routeResource), e);
                  stats.incSkipped();
               }
            }

            log.info("Total Camel route resources " + stats.toString() + ".");

            camelContext.start();

            try {
               while (!Thread.currentThread().isInterrupted()) {
                  Thread.sleep(1000);
               }
            } catch (InterruptedException ie) {
               Utils.shutdownLog(log, ie);
            } finally {
               try {
                  camelContext.stop();
               } catch (Exception e) {
                  log.trace("Weld was shut down before Camel and destroyed the context: ", e);
               }
            }
         } catch (Exception e) {
            log.error("Camel microservice provider failed: ", e);
         }
      } else {
         log.warn("No route resources to start. Camel microservice provider is terminated.");
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
