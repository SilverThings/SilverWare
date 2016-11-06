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
package io.silverware.microservices;

import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.silver.HttpServerSilverService;
import io.silverware.microservices.silver.ProvidingSilverService;
import io.silverware.microservices.silver.SilverService;
import io.silverware.microservices.silver.cluster.LocalServiceHandle;
import io.silverware.microservices.silver.cluster.RemoteServiceHandlesStore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Microservices context carrying all necessary execution information.
 * The intention is to separate all information shared between providers
 * to be able to create multiple instances of the platform in the same JVM.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class Context {

   /**
    * Logger.
    */
   private static final Logger log = LogManager.getLogger(Context.class);

   /**
    * Property key where a registry with Microservice providers is.
    */
   public static final String MICROSERVICE_PROVIDERS_REGISTRY = "silverware.providers.registry";

   /**
    * Name of the system property that lists the packages to be searched for provider deployments.
    */
   public static final String DEPLOYMENT_PACKAGES = "silverware.deploy.packages";

   /**
    * Property key where a registry with local Microservices is.
    */
   public static final String MICROSERVICES = "silverware.microservices";

   /**
    * Global properties.
    */
   private final HashMap<String, Object> properties = new HashMap<>();

   /**
    * Providers registry.
    */
   private final Map<String, MicroserviceProvider> providers = new HashMap<>();

   /**
    * Local Microservices registry.
    */
   private final Set<MicroserviceMetaData> microservices = new HashSet<>();

   /**
    * Handles created for incoming service queries.
    */
   private final List<LocalServiceHandle> inboundHandles = new ArrayList<>();


   /**
    * store remote microservices
    */
   private final RemoteServiceHandlesStore remoteServiceHandlesStore;

   /**
    * Creates the context and binds the registries to global properties.
    */
   public Context() {
      properties.put(MICROSERVICE_PROVIDERS_REGISTRY, providers);
      properties.put(MICROSERVICES, microservices);
      remoteServiceHandlesStore = new RemoteServiceHandlesStore();

   }

   /**
    * Gets the global properties.
    *
    * @return The global properties.
    */
   public Map<String, Object> getProperties() {
      return properties;
   }

   /**
    * Gets the registry of Microservices providers.
    *
    * @return The registry of Microservices providers.
    */
   public Map<String, MicroserviceProvider> getProvidersRegistry() {
      return (Map<String, MicroserviceProvider>) properties.get(MICROSERVICE_PROVIDERS_REGISTRY);
   }

   /**
    * Adds a Microservice to the registry.
    *
    * @param metaData Description of the service to be registered.
    */
   public void registerMicroservice(final MicroserviceMetaData metaData) {
      microservices.add(metaData);
   }

   /**
    * Gets an unmodifiable copy of the current local Microservices registry.
    *
    * @return An unmodifiable copy of the current local Microservices registry.
    */
   public Set<MicroserviceMetaData> getMicroservices() {
      return Collections.unmodifiableSet(microservices);
   }

   /**
    * Looks up Microservices based on the provided meta-data query.
    * All providers are asked to look up all possible services including local and remote.
    *
    * @param metaData Meta-data query.
    * @return A set of Microservices instances that meets the query.
    */
   public Set<Object> lookupMicroservice(final MicroserviceMetaData metaData) {
      final Set<Object> microservices = new HashSet<>();
      getAllProviders(ProvidingSilverService.class).forEach(provider -> microservices.addAll(((ProvidingSilverService) provider).lookupMicroservice(metaData)));

      return microservices;
   }

   /**
    * Looks up Microservices based on the provided meta-data query.
    * Only local Microservices are searched.
    *
    * @param metaData Meta-data query.
    * @return A set of Microservices instances that meets the query.
    */
   public Set<Object> lookupLocalMicroservice(final MicroserviceMetaData metaData) {
      final Set<Object> microservices = new HashSet<>();
      getAllProviders(ProvidingSilverService.class).forEach(provider -> microservices.addAll(((ProvidingSilverService) provider).lookupLocalMicroservice(metaData)));

      return microservices;
   }

   /**
    * Gets a provider based on the interface or class specification.
    * Usually ancestors of {@link SilverService} are used for the query.
    * The first class from the registry that meets the query is returned.
    *
    * @param clazz An interface or class the implementation of which we are looking for.
    * @return The appropriate Microservice provider if it exists, null otherwise.
    */
   public SilverService getProvider(final Class<? extends SilverService> clazz) {
      for (Map.Entry<String, MicroserviceProvider> entry : providers.entrySet()) {
         if (clazz.isAssignableFrom(entry.getValue().getClass())) {
            return (SilverService) entry.getValue();
         }
      }

      return null;
   }

   /**
    * Gets all providers based on the interface or class specification.
    * Usually ancestors of {@link SilverService} are used for the query.
    *
    * @param clazz An interface or class the implementation of which we are looking for.
    * @return A set of appropriate Microservice providers if they exists, an empty set otherwise.
    */
   public Set<SilverService> getAllProviders(final Class<? extends SilverService> clazz) {
      return providers.entrySet().stream().filter(entry -> clazz.isAssignableFrom(entry.getValue().getClass())).map(entry -> (SilverService) entry.getValue()).collect(Collectors.toSet());
   }

   /**
    * Makes that there are handles created for all the local services that meet the given the query.
    * Only local Microservices are searched as we cannot created inbound handles for remote Microservices
    * (we do not want to just pass through calls).
    *
    * @param metaData The query for which to look up the service handles.
    * @return A list of {@link LocalServiceHandle Service Handles} that meet the specified query.
    */
   public List<LocalServiceHandle> assureHandles(final MicroserviceMetaData metaData) {
      List<LocalServiceHandle> result = inboundHandles.stream().filter(serviceHandle -> serviceHandle.getMetaData().equals(metaData)).collect(Collectors.toList());
      Set<Object> microservices = lookupLocalMicroservice(metaData);
      Set<Object> haveHandles = result.stream().map(LocalServiceHandle::getProxy).collect(Collectors.toSet());
      microservices.removeAll(haveHandles);

      microservices.forEach(microservice -> {
         final String host = properties.get(HttpServerSilverService.HTTP_SERVER_ADDRESS) + ":" + properties.get(HttpServerSilverService.HTTP_SERVER_PORT);
         final LocalServiceHandle handle = new LocalServiceHandle(host, metaData, microservice);
         result.add(handle);
         inboundHandles.add(handle);
      });

      return result;
   }

   /**
    * Gets the {@link LocalServiceHandle} for the given handle number.
    *
    * @param handle The handle number.
    * @return The {@link LocalServiceHandle} with the given handle number.
    */
   public LocalServiceHandle getInboundServiceHandle(final int handle) {
      return inboundHandles.stream().filter(serviceHandle -> serviceHandle.getHandle() == handle).findFirst().get();
   }

   /**
    * Gets store for remote handles
    *
    * @return instance of {@link RemoteServiceHandlesStore} which is used in this context
    */
   public RemoteServiceHandlesStore getRemoteServiceHandlesStore() {
      return remoteServiceHandlesStore;
   }

}