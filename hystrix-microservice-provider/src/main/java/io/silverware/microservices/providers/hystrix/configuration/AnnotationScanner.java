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

import io.silverware.microservices.annotations.hystrix.HystrixConfig;
import io.silverware.microservices.annotations.hystrix.advanced.CacheKey;
import io.silverware.microservices.annotations.hystrix.advanced.DefaultProperties;
import io.silverware.microservices.annotations.hystrix.advanced.HystrixCommand;
import io.silverware.microservices.annotations.hystrix.basic.Cached;
import io.silverware.microservices.annotations.hystrix.basic.CircuitBreaker;
import io.silverware.microservices.annotations.hystrix.basic.Fail;
import io.silverware.microservices.annotations.hystrix.basic.ThreadPool;
import io.silverware.microservices.annotations.hystrix.basic.Timeout;
import io.silverware.microservices.providers.hystrix.configuration.MethodConfig.Builder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Scans annotations and creates Hystrix service configuration.
 */
public class AnnotationScanner {

   private static final Set<Class<? extends Annotation>> HIGH_LEVEL_ANNOTATIONS = new HashSet<>();

   static {
      Collections.addAll(HIGH_LEVEL_ANNOTATIONS, Cached.class, CircuitBreaker.class, Fail.class, ThreadPool.class, Timeout.class);
   }

   private AnnotationScanner() {

   }

   /**
    * Scans given injection point for Hystrix annotations and creates Hystrix service configuration.
    *
    * @param injectionPoint
    *       injection point to be scanned
    * @param microserviceName
    *       the name of the microservice being injected
    * @return Hystrix service configuration
    */
   public static ServiceConfig scan(InjectionPoint injectionPoint, String microserviceName) {
      String commandKey = injectionPoint.getBean().getName() + ":" + injectionPoint.getMember().getName();
      MethodConfig basicConfig = MethodConfig.createBuilder(microserviceName, commandKey).build();

      Set<Annotation> annotations = injectionPoint.getAnnotated().getAnnotations();
      Optional<Annotation> hystrixAnnotation = annotations.stream().filter(annotation -> annotation.annotationType() == HystrixConfig.class).findAny();

      if (hystrixAnnotation.isPresent()) {
         HystrixConfig hystrixConfig = (HystrixConfig) hystrixAnnotation.get();
         return scanInterfaceAnnotations(hystrixConfig.value(), basicConfig);
      } else {
         Annotation[] annotationArray = new Annotation[annotations.size()];
         return scanFieldAnnotations(annotations.toArray(annotationArray), basicConfig);
      }
   }

   private static ServiceConfig scanFieldAnnotations(Annotation[] annotations, MethodConfig basicConfig) {
      final MethodConfig methodConfig = scanHighLevelAnnotations(annotations, basicConfig);
      return new ServiceConfig(methodConfig);
   }

   private static ServiceConfig scanInterfaceAnnotations(Class configClass, MethodConfig basicConfig) {
      final Map<String, MethodConfig> methods = new HashMap<>();

      MethodConfig defaultConfig = scanClassAnnotations(configClass.getAnnotations(), basicConfig);

      for (Method method : configClass.getDeclaredMethods()) {
         MethodConfig methodDefaultConfig = defaultConfig;
         if (defaultConfig.getCommandKey().equals(basicConfig.getCommandKey())) {
            String commandKey = basicConfig.getCommandKey() + ":" + method.getName();
            methodDefaultConfig = MethodConfig.createBuilder(defaultConfig).commandKey(commandKey).build();
         }
         MethodConfig methodConfig = scanMethodAnnotations(method, methodDefaultConfig);
         methods.put(ServiceConfig.getMethodSignature(method), methodConfig);
      }

      return new ServiceConfig(methods);
   }

   private static MethodConfig scanClassAnnotations(Annotation[] annotations, MethodConfig basicConfig) {
      MethodConfig methodConfig = scanHighLevelAnnotations(annotations, basicConfig);

      if (!methodConfig.isHystrixActive()) {
         methodConfig = scanClassLowLevelAnnotations(annotations, basicConfig);
      }

      return methodConfig;
   }

   private static MethodConfig scanMethodAnnotations(Method method, MethodConfig defaultConfig) {
      if (isHighLevelAnnotationPresent(method.getAnnotations())) {
         return scanHighLevelAnnotations(method.getAnnotations(), defaultConfig);
      } else {
         return scanMethodLowLevelAnnotations(method, defaultConfig);
      }
   }

   private static boolean isHighLevelAnnotationPresent(Annotation[] annotations) {
      return Arrays.stream(annotations)
                   .map(Annotation::annotationType)
                   .anyMatch(HIGH_LEVEL_ANNOTATIONS::contains);
   }

   private static MethodConfig scanHighLevelAnnotations(Annotation[] annotations, MethodConfig defaultConfig) {
      Builder builder = MethodConfig.createBuilder(defaultConfig);

      for (Annotation annotation : annotations) {
         if (annotation.annotationType() == Cached.class) {
            builder.hystrixActive(true)
                   .commandProperty(CommandProperties.REQUEST_CACHE_ENABLED, Boolean.TRUE.toString());
         }

         if (annotation.annotationType() == CircuitBreaker.class) {
            CircuitBreaker circuitBreaker = (CircuitBreaker) annotation;
            builder.hystrixActive(true)
                   .commandProperty(CommandProperties.CIRCUIT_BREAKER_ENABLED, Boolean.TRUE.toString())
                   .commandProperty(CommandProperties.CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE, String.valueOf(circuitBreaker.errorPercentage()))
                   .commandProperty(CommandProperties.CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD, String.valueOf(circuitBreaker.requestVolume()))
                   .commandProperty(CommandProperties.CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS, String.valueOf(circuitBreaker.sleepWindow()));
         }

         if (annotation.annotationType() == Fail.class) {
            Fail fail = (Fail) annotation;
            builder.hystrixActive(true)
                   .ignoredExceptions(new HashSet<>(Arrays.asList(fail.value())));
         }

         if (annotation.annotationType() == ThreadPool.class) {
            ThreadPool threadPool = (ThreadPool) annotation;
            builder.hystrixActive(true)
                   .threadPoolKey(threadPool.name())
                   .threadPoolProperty(ThreadPoolProperties.CORE_SIZE, String.valueOf(threadPool.size()))
                   .threadPoolProperty(ThreadPoolProperties.MAXIMUM_SIZE, String.valueOf(threadPool.size()));
         }

         if (annotation.annotationType() == Timeout.class) {
            Timeout timeout = (Timeout) annotation;
            builder.hystrixActive(true)
                   .commandProperty(CommandProperties.EXECUTION_TIMEOUT_ENABLED, Boolean.TRUE.toString())
                   .commandProperty(CommandProperties.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, String.valueOf(timeout.value()));
         }
      }

      return builder.build();
   }

   private static MethodConfig scanClassLowLevelAnnotations(Annotation[] annotations, MethodConfig basicConfig) {
      Builder builder = MethodConfig.createBuilder(basicConfig);

      for (Annotation annotation : annotations) {
         if (annotation.annotationType() == DefaultProperties.class) {
            DefaultProperties defaultProperties = (DefaultProperties) annotation;
            builder.hystrixActive(true)
                   .groupKey(defaultProperties.groupKey())
                   .threadPoolKey(defaultProperties.threadPoolKey());
            Arrays.stream(defaultProperties.commandProperties())
                  .forEach(p -> builder.commandProperty(p.name(), p.value()));
            Arrays.stream(defaultProperties.threadPoolProperties())
                  .forEach(p -> builder.threadPoolProperty(p.name(), p.value()));
            Arrays.stream(defaultProperties.ignoredExceptions())
                  .forEach(builder::ignoredException);
         }
      }

      return builder.build();
   }

   private static MethodConfig scanMethodLowLevelAnnotations(Method method, MethodConfig defaultConfig) {
      Builder builder = MethodConfig.createBuilder(defaultConfig);

      for (Annotation annotation : method.getAnnotations()) {
         if (annotation.annotationType() == HystrixCommand.class) {
            HystrixCommand hystrixCommand = (HystrixCommand) annotation;
            String groupKey = !hystrixCommand.groupKey().isEmpty() ? hystrixCommand.groupKey() : defaultConfig.getGroupKey();
            String commandKey = !hystrixCommand.commandKey().isEmpty() ? hystrixCommand.commandKey() : defaultConfig.getCommandKey();
            String threadPoolKey = !hystrixCommand.threadPoolKey().isEmpty() ? hystrixCommand.threadPoolKey() : defaultConfig.getThreadPoolKey();
            builder.hystrixActive(true)
                   .groupKey(groupKey)
                   .commandKey(commandKey)
                   .threadPoolKey(threadPoolKey);
            Arrays.stream(hystrixCommand.commandProperties())
                  .forEach(p -> builder.commandProperty(p.name(), p.value()));
            Arrays.stream(hystrixCommand.threadPoolProperties())
                  .forEach(p -> builder.threadPoolProperty(p.name(), p.value()));
            Arrays.stream(hystrixCommand.ignoredExceptions())
                  .forEach(builder::ignoredException);
         }
      }

      Annotation[][] parameterAnnotations = method.getParameterAnnotations();
      for (int i = 0; i < parameterAnnotations.length; i++) {
         for (Annotation annotation : parameterAnnotations[i]) {
            if (annotation.annotationType() == CacheKey.class) {
               builder.cacheKeyParameterIndex(i);
            }
         }
      }

      return builder.build();
   }

}
