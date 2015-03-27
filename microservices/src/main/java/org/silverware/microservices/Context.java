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

import org.silverware.microservices.providers.MicroserviceProvider;
import org.silverware.microservices.silver.SilverService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class Context {

   public static final String MICROSERVICE_PROVIDERS_REGISTRY = "silverware.providers.registry";
   public static final String DEPLOYMENT_PACKAGES = "silverware.deploy.packages";
   public static final String MICROSERVICES = "silverware.microservices";

   private final Map<String, Object> properties = new HashMap<>();
   private final Map<String, MicroserviceProvider> providers = new HashMap<>();
   private final Set<MicroserviceMetaData> microservices = new HashSet<>();

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

   public SilverService getProvider(Class<? extends SilverService> clazz) {
      for (Map.Entry<String, MicroserviceProvider> entry : providers.entrySet()) {
         if (clazz.isAssignableFrom(entry.getValue().getClass())) {
            return (SilverService) entry.getValue();
         }
      }

      return null;
   }
}
