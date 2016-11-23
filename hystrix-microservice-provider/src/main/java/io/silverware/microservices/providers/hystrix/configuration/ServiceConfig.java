/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2016 the original author or authors.
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
package io.silverware.microservices.providers.hystrix.configuration;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/**
 * Configuration of Hystrix commands for a given microservice. Either method-specific or default for all service methods.
 */
@Immutable
public class ServiceConfig {

   private static final String DEFAULT = "*";

   private final Map<String, MethodConfig> config;

   public ServiceConfig(MethodConfig defaultConfig) {
      this(Collections.singletonMap(DEFAULT, defaultConfig));
   }

   ServiceConfig(final Map<String, MethodConfig> config) {
      this.config = config;
   }

   public MethodConfig getMethodConfig(Method method) {
      String methodSignature = getMethodSignature(method);
      return config.get(methodSignature);
   }

   public MethodConfig getDefaultConfig() {
      return config.get(DEFAULT);
   }

   static String getMethodSignature(Method method) {
      return method.getName() + Arrays.toString(method.getParameterTypes());
   }

}
