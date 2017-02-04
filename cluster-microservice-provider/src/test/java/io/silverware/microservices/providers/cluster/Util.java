/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2015 - 2017 the original author or authors.
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
package io.silverware.microservices.providers.cluster;

import io.silverware.microservices.providers.cluster.internal.RemoteServiceHandleStoreTest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Basic Utils used in tests
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class Util {
   public static RemoteServiceHandle createHandle(int handle) {
      return new RemoteServiceHandle(org.jgroups.util.UUID.randomUUID(), 1, null, RemoteServiceHandleStoreTest.META_DATA);
   }

   public static <T> Set<T> createSetFrom(T... components) {
      return new HashSet<>(Arrays.asList(components));
   }

}
