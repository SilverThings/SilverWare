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
package org.silverware.microservices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silverware.microservices.providers.MicroserviceProvider;
import org.silverware.microservices.silver.HttpServerSilverService;
import org.silverware.microservices.silver.ProvidingSilverService;
import org.silverware.microservices.silver.SilverService;
import org.silverware.microservices.silver.cluster.Invocation;
import org.silverware.microservices.silver.cluster.ServiceHandle;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class Context {

   private static final Logger log = LogManager.getLogger(Context.class);

   public static final String MICROSERVICE_PROVIDERS_REGISTRY = "silverware.providers.registry";
   public static final String DEPLOYMENT_PACKAGES = "silverware.deploy.packages";
   public static final String MICROSERVICES = "silverware.microservices";

   private final Map<String, Object> properties = new HashMap<>();
   private final Map<String, MicroserviceProvider> providers = new HashMap<>();
   private final Set<MicroserviceMetaData> microservices = new HashSet<>();
   private List<ServiceHandle> inboundHandles = new ArrayList<>();
   private List<ServiceHandle> outboundHandles = new ArrayList<>();

   public Context() {
      properties.put(MICROSERVICE_PROVIDERS_REGISTRY, providers);
      properties.put(MICROSERVICES, microservices);
   }

   public Map<String, Object> getProperties() {
      return properties;
   }

   @SuppressWarnings("unchecked")
   public Map<String, MicroserviceProvider> getProvidersRegistry() {
      return (Map<String, MicroserviceProvider>) properties.get(MICROSERVICE_PROVIDERS_REGISTRY);
   }

   public void registerMicroservice(final MicroserviceMetaData metaData) {
      microservices.add(metaData);
   }

   public Set<MicroserviceMetaData> getMicroservices() {
      return Collections.unmodifiableSet(microservices);
   }

   public Set<Object> lookupMicroservice(final MicroserviceMetaData metaData) {
      return getAllProviders(ProvidingSilverService.class).stream()
            .map(providingSilverService -> ((ProvidingSilverService) providingSilverService).lookupMicroservice(metaData))
            .collect(Collectors.toSet());
   }

   public Set<Object> lookupLocalMicroservice(final MicroserviceMetaData metaData) {
      return getAllProviders(ProvidingSilverService.class).stream()
            .map(providingSilverService -> ((ProvidingSilverService) providingSilverService).lookupLocalMicroservice(metaData))
            .collect(Collectors.toSet());
   }

   public SilverService getProvider(final Class<? extends SilverService> clazz) {
      for (Map.Entry<String, MicroserviceProvider> entry : providers.entrySet()) {
         if (clazz.isAssignableFrom(entry.getValue().getClass())) {
            return (SilverService) entry.getValue();
         }
      }

      return null;
   }

   public Set<SilverService> getAllProviders(final Class<? extends SilverService> clazz) {
      return providers.entrySet().stream().filter(entry -> clazz.isAssignableFrom(entry.getValue().getClass())).map(entry -> (SilverService) entry.getValue()).collect(Collectors.toSet());
   }

   public List<ServiceHandle> assureHandles(final MicroserviceMetaData metaData) {
      List<ServiceHandle> result = inboundHandles.stream().filter(serviceHandle -> serviceHandle.getQuery().equals(metaData)).collect(Collectors.toList());
      Set<Object> microservices = lookupLocalMicroservice(metaData);
      Set<Object> haveHandles = result.stream().map(ServiceHandle::getService).collect(Collectors.toSet());
      microservices.removeAll(haveHandles);

      microservices.forEach(microservice -> {
         final ServiceHandle handle = new ServiceHandle((String) properties.get(HttpServerSilverService.HTTP_SERVER_ADDRESS), metaData, microservice);
         result.add(handle);
         inboundHandles.add(handle);
      });

      return result;
   }

   public Object invoke(final Invocation invocation) throws Exception {
      if (log.isTraceEnabled()) {
         log.trace("Invoking Microservice with invocation {}.", invocation.toString());
      }

      final ServiceHandle handle = inboundHandles.stream().filter(serviceHandle -> serviceHandle.getHandle() == invocation.getHandle()).findFirst().get();

      if (handle == null) {
         throw new SilverWareException(String.format("Handle no. %d. No such handle found.", invocation.getHandle()));
      }

      final Class[] paramTypes = new Class[invocation.getParams().length];
      for (int i = 0; i < invocation.getParams().length; i++) {
         paramTypes[i] = invocation.getParams()[i].getClass();
      }

      final Method method = handle.getService().getClass().getDeclaredMethod(invocation.getMethod(), paramTypes);
      return method.invoke(handle.getService(), invocation.getParams());
   }

}
