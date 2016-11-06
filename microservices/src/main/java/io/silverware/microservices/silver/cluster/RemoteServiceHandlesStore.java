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
package io.silverware.microservices.silver.cluster;

import io.silverware.microservices.MicroserviceMetaData;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This class is used as concurrent safe store for all remote microservices handles
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class RemoteServiceHandlesStore {

   private final Map<MicroserviceMetaData, Set<ServiceHandle>> outboundHandles;

   public RemoteServiceHandlesStore() {
      this.outboundHandles = new ConcurrentHashMap<>();
   }

   /**
    * Adds provided handles to store with metadata used as key.
    * If the entry for given metadata does not exist new entry is created
    *
    * @param metaData - key for service handle
    * @param handles  - list of handles tied to metadata
    */
   public void addHandles(MicroserviceMetaData metaData, Set<ServiceHandle> handles) {
      Set<ServiceHandle> serviceHandles = outboundHandles.getOrDefault(metaData, new HashSet<>());
      serviceHandles.addAll(handles);
      outboundHandles.putIfAbsent(metaData, serviceHandles);
   }

   /**
    * Adds provided handle  to store with metadata used as key.
    * If the entry for given metadata does not exist new entry is created
    *
    * @param metaData - key for service handle
    * @param handle   - list of handles tied to metadata
    */
   public void addHandle(MicroserviceMetaData metaData, ServiceHandle handle) {
      Set<ServiceHandle> serviceHandles = outboundHandles.getOrDefault(metaData, new HashSet<>());
      serviceHandles.add(handle);
      outboundHandles.putIfAbsent(metaData, serviceHandles);
   }

   /**
    * Removes all handles which are not mentioned in available nodes
    *
    * @param availableNodes addresses of available nodes
    */
   public void keepHandlesFor(Set<String> availableNodes) {
      outboundHandles.forEach((metaData, serviceHandles) -> {
         serviceHandles = serviceHandles.stream().filter(serviceHandle -> availableNodes.contains(serviceHandle.getHost())).collect(Collectors.toSet());
         outboundHandles.put(metaData, serviceHandles);
      });
   }

   /**
    * Provides services available for given metadata
    *
    * @param metaData metadata of microservice which is required
    * @return collection of services which can be called for given metadata
    */
   public Set<Object> getServices(MicroserviceMetaData metaData) {
      return outboundHandles.getOrDefault(metaData, Collections.emptySet()).stream().map(ServiceHandle::getProxy).collect(Collectors.toSet());
   }
}
