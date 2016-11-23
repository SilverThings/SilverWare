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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.concurrent.Immutable;

/**
 * Configuration of Hystrix commands for a given method.
 */
@Immutable
public class MethodConfig {

   private final boolean hystrixActive;

   private final String groupKey;
   private final String commandKey;
   private final String threadPoolKey;

   private final Set<Integer> cacheKeyParameterIndexes;

   private final Map<String, String> commandProperties;
   private final Map<String, String> threadPoolProperties;
   private final Set<Class<? extends Throwable>> ignoredExceptions;

   public MethodConfig(Builder builder) {
      hystrixActive = builder.hystrixActive;
      groupKey = builder.groupKey;
      commandKey = builder.commandKey;
      cacheKeyParameterIndexes = builder.cacheKeyParameterIndexes;
      threadPoolKey = builder.threadPoolKey;
      commandProperties = builder.commandProperties;
      threadPoolProperties = builder.threadPoolProperties;
      ignoredExceptions = builder.ignoredExceptions;
   }

   public boolean isHystrixActive() {
      return hystrixActive;
   }

   public String getGroupKey() {
      return groupKey;
   }

   public String getCommandKey() {
      return commandKey;
   }

   public String getThreadPoolKey() {
      return threadPoolKey;
   }

   public Set<Integer> getCacheKeyParameterIndexes() {
      return Collections.unmodifiableSet(cacheKeyParameterIndexes);
   }

   public Map<String, String> getCommandProperties() {
      return Collections.unmodifiableMap(commandProperties);
   }

   public Map<String, String> getThreadPoolProperties() {
      return Collections.unmodifiableMap(threadPoolProperties);
   }

   public Set<Class<? extends Throwable>> getIgnoredExceptions() {
      return Collections.unmodifiableSet(ignoredExceptions);
   }

   public static Builder createBuilder() {
      return new Builder();
   }

   /**
    * Creates method configuration builder pre-configured with values from given default configuration.
    *
    * @param defaultConfig
    *       default configuration
    * @return method configuration builder
    */
   public static Builder createBuilder(MethodConfig defaultConfig) {
      Builder builder = createBuilder();

      if (defaultConfig != null) {
         builder.hystrixActive(defaultConfig.hystrixActive)
                .groupKey(defaultConfig.groupKey)
                .commandKey(defaultConfig.commandKey)
                .threadPoolKey(defaultConfig.threadPoolKey)
                .commandProperties(defaultConfig.commandProperties)
                .threadPoolProperties(defaultConfig.threadPoolProperties)
                .ignoredExceptions(defaultConfig.ignoredExceptions);
      }

      return builder;
   }

   /**
    * Method configuration builder.
    */
   public static class Builder {

      private boolean hystrixActive = false;

      private String groupKey;
      private String commandKey;
      private String threadPoolKey;

      private Set<Integer> cacheKeyParameterIndexes = new HashSet<>();

      private Map<String, String> commandProperties = new HashMap<>();
      private Map<String, String> threadPoolProperties = new HashMap<>();
      private Set<Class<? extends Throwable>> ignoredExceptions = new HashSet<>();

      private Builder() {
      }

      public Builder hystrixActive(boolean hystrixActive) {
         this.hystrixActive = hystrixActive;
         return this;
      }

      public Builder groupKey(String groupKey) {
         this.groupKey = groupKey;
         return this;
      }

      public Builder commandKey(String commandKey) {
         this.commandKey = commandKey;
         return this;
      }

      public Builder threadPoolKey(String threadPoolKey) {
         this.threadPoolKey = threadPoolKey;
         return this;
      }

      public Builder cacheKeyParameterIndex(Integer parameterIndex) {
         cacheKeyParameterIndexes.add(parameterIndex);
         return this;
      }

      public Builder commandProperties(Map<String, String> commandProperties) {
         this.commandProperties.putAll(commandProperties);
         return this;
      }

      public Builder commandProperty(String key, String value) {
         commandProperties.put(key, value);
         return this;
      }

      public Builder threadPoolProperties(Map<String, String> threadPoolProperties) {
         this.threadPoolProperties.putAll(threadPoolProperties);
         return this;
      }

      public Builder threadPoolProperty(String key, String value) {
         threadPoolProperties.put(key, value);
         return this;
      }

      public Builder ignoredExceptions(Set<Class<? extends Throwable>> ignoredExceptions) {
         this.ignoredExceptions.addAll(ignoredExceptions);
         return this;
      }

      public Builder ignoredException(Class<? extends Throwable> ignoredException) {
         this.ignoredExceptions.add(ignoredException);
         return this;
      }

      public MethodConfig build() {
         return new MethodConfig(this);
      }

   }

}
