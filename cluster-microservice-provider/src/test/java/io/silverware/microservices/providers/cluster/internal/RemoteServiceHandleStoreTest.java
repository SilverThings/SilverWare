/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2010 - 2016 the original author or authors.
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
package io.silverware.microservices.providers.cluster.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.providers.cluster.Util;
import io.silverware.microservices.silver.cluster.LocalServiceHandle;
import io.silverware.microservices.silver.cluster.RemoteServiceHandlesStore;
import io.silverware.microservices.silver.cluster.ServiceHandle;

import org.testng.annotations.Test;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Basic Unit tests for remote handles store
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class RemoteServiceHandleStoreTest {
   private static final String VERSION = "1.0.0";

   private static final Set<Annotation> ANNOTATIONS = new HashSet<>(Arrays.asList(RemoteServiceHandleStoreTest.class.getClass().getAnnotations()));

   public static final MicroserviceMetaData META_DATA = new MicroserviceMetaData(RemoteServiceHandleStoreTest.class.getName(), RemoteServiceHandleStoreTest.class, ANNOTATIONS, ANNOTATIONS, VERSION, VERSION);
   public static final LocalServiceHandle SERVICE_HANDLE = Util.createHandle("host");

   @Test
   public void addSingleHandle() {
      RemoteServiceHandlesStore store = new RemoteServiceHandlesStore();
      store.addHandle(META_DATA, SERVICE_HANDLE);
      Set<Object> services = store.getServices(META_DATA);
      assertThat(services).containsOnly(SERVICE_HANDLE.getProxy());
   }

   @Test
   public void testRemoveHandlesByHost() {
      RemoteServiceHandlesStore store = new RemoteServiceHandlesStore();
      Set<ServiceHandle> handles = Util.createSetFrom(Util.createHandle("1"), Util.createHandle("2"), SERVICE_HANDLE);
      store.addHandles(META_DATA, handles);
      store.keepHandlesFor(Util.createSetFrom("host"));

      Set<Object> services = store.getServices(META_DATA);
      assertThat(services).containsOnly(SERVICE_HANDLE.getProxy());
   }

   @Test
   public void testAddHandlesCollectionAfterOneHandle() {
      RemoteServiceHandlesStore store = new RemoteServiceHandlesStore();
      Set<ServiceHandle> handles = Util.createSetFrom(Util.createHandle("1"), Util.createHandle("2"));
      store.addHandle(META_DATA, SERVICE_HANDLE);
      store.addHandles(META_DATA, handles);
      Set<Object> services = store.getServices(META_DATA);
      assertThat(services).hasSize(3);
   }

   @Test
   public void testAddOneHandleAfterHandlesCollection() {
      RemoteServiceHandlesStore store = new RemoteServiceHandlesStore();
      Set<ServiceHandle> handles = Util.createSetFrom(Util.createHandle("1"), Util.createHandle("2"));
      store.addHandles(META_DATA, handles);
      store.addHandle(META_DATA, SERVICE_HANDLE);
      Set<Object> services = store.getServices(META_DATA);
      assertThat(services).hasSize(3);
   }

}
