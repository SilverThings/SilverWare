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
import io.silverware.microservices.annotations.hystrix.basic.Cached;
import io.silverware.microservices.annotations.hystrix.basic.CircuitBreaker;
import io.silverware.microservices.annotations.hystrix.basic.Fail;
import io.silverware.microservices.annotations.hystrix.basic.ThreadPool;
import io.silverware.microservices.annotations.hystrix.basic.Timeout;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import java.util.Map;

public class AnnotationScannerHighLevelInterfaceTest extends AnnotationScannerTestBase {

   private static final String THREAD_POOL_NAME = "TestingThreadPool";
   private static final int TIMEOUT_VALUE = 2000;

   @HystrixConfig(MethodsRemoteMicroservice.class)
   private RemoteMicroservice methodsMicroservice;

   @Test
   public void testMethodsConfiguration() {
      ServiceConfig serviceConfig = scanToServiceConfig("methodsMicroservice");

      MethodConfig methodConfig = getMethodConfig(serviceConfig, METHOD_1);
      Assertions.assertThat(methodConfig).isNotNull();
      Assertions.assertThat(methodConfig.isHystrixActive()).isFalse();

      Map<String, String> commandProperties = methodConfig.getCommandProperties();
      Assertions.assertThat(commandProperties).isNotNull().isEmpty();

      methodConfig = getMethodConfig(serviceConfig, METHOD_2);
      Assertions.assertThat(methodConfig).isNotNull();
      Assertions.assertThat(methodConfig.isHystrixActive()).isTrue();

      commandProperties = methodConfig.getCommandProperties();
      Assertions.assertThat(commandProperties).isNotNull().isNotEmpty();

      SoftAssertions assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.REQUEST_CACHE_ENABLED, Boolean.TRUE.toString());
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ENABLED, Boolean.TRUE.toString());
      assertions.assertThat(methodConfig.getIgnoredExceptions()).containsOnly(NullPointerException.class);
      assertions.assertThat(methodConfig.getThreadPoolKey()).isNull();
      assertions.assertThat(commandProperties).doesNotContainKey(CommandProperties.EXECUTION_TIMEOUT_ENABLED);
      assertions.assertAll();

      methodConfig = getMethodConfig(serviceConfig, METHOD_3);
      Assertions.assertThat(methodConfig).isNotNull();
      Assertions.assertThat(methodConfig.isHystrixActive()).isTrue();

      commandProperties = methodConfig.getCommandProperties();
      Assertions.assertThat(commandProperties).isNotNull().isNotEmpty();

      assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).doesNotContainKey(CommandProperties.REQUEST_CACHE_ENABLED);
      assertions.assertThat(commandProperties).doesNotContainKey(CommandProperties.CIRCUIT_BREAKER_ENABLED);
      assertions.assertThat(methodConfig.getIgnoredExceptions()).isEmpty();
      assertions.assertThat(methodConfig.getThreadPoolKey()).isEqualTo(THREAD_POOL_NAME);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_TIMEOUT_ENABLED, Boolean.TRUE.toString());
      assertions.assertAll();
   }

   @HystrixConfig(ClassRemoteMicroservice.class)
   private RemoteMicroservice classMicroservice;

   @Test
   public void testClassConfiguration() {
      ServiceConfig serviceConfig = scanToServiceConfig("classMicroservice");

      MethodConfig methodConfig = getMethodConfig(serviceConfig, METHOD_1);
      Assertions.assertThat(methodConfig).isNotNull();
      Assertions.assertThat(methodConfig.isHystrixActive()).isTrue();

      Map<String, String> commandProperties = methodConfig.getCommandProperties();
      Assertions.assertThat(commandProperties).isNotNull().isNotEmpty();

      SoftAssertions assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).doesNotContainKey(CommandProperties.REQUEST_CACHE_ENABLED);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ENABLED, Boolean.TRUE.toString());
      assertions.assertThat(methodConfig.getIgnoredExceptions()).isEmpty();
      assertions.assertThat(methodConfig.getThreadPoolKey()).isNull();
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_TIMEOUT_ENABLED, Boolean.TRUE.toString());
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, String.valueOf(Timeout.DEFAULT_TIMEOUT));
      assertions.assertAll();

      methodConfig = getMethodConfig(serviceConfig, METHOD_2);
      Assertions.assertThat(methodConfig).isNotNull();
      Assertions.assertThat(methodConfig.isHystrixActive()).isTrue();

      commandProperties = methodConfig.getCommandProperties();
      Assertions.assertThat(commandProperties).isNotNull().isNotEmpty();

      assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.REQUEST_CACHE_ENABLED, Boolean.TRUE.toString());
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ENABLED, Boolean.TRUE.toString());
      assertions.assertThat(methodConfig.getIgnoredExceptions()).containsOnly(NullPointerException.class);
      assertions.assertThat(methodConfig.getThreadPoolKey()).isNull();
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_TIMEOUT_ENABLED, Boolean.TRUE.toString());
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, String.valueOf(Timeout.DEFAULT_TIMEOUT));
      assertions.assertAll();

      methodConfig = getMethodConfig(serviceConfig, METHOD_3);
      Assertions.assertThat(methodConfig).isNotNull();
      Assertions.assertThat(methodConfig.isHystrixActive()).isTrue();

      commandProperties = methodConfig.getCommandProperties();
      Assertions.assertThat(commandProperties).isNotNull().isNotEmpty();

      assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).doesNotContainKey(CommandProperties.REQUEST_CACHE_ENABLED);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ENABLED, Boolean.TRUE.toString());
      assertions.assertThat(methodConfig.getIgnoredExceptions()).isEmpty();
      assertions.assertThat(methodConfig.getThreadPoolKey()).isEqualTo(THREAD_POOL_NAME);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_TIMEOUT_ENABLED, Boolean.TRUE.toString());
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, String.valueOf(TIMEOUT_VALUE));
      assertions.assertAll();
   }

   private interface MethodsRemoteMicroservice extends RemoteMicroservice {

      void method1(Object param);

      @Cached
      @CircuitBreaker
      @Fail(NullPointerException.class)
      String method2();

      @ThreadPool(THREAD_POOL_NAME)
      @Timeout
      int method3(long param);

   }

   @CircuitBreaker
   @Timeout
   private interface ClassRemoteMicroservice extends RemoteMicroservice {

      void method1(Object param);

      @Cached
      @Fail(NullPointerException.class)
      String method2();

      @ThreadPool(THREAD_POOL_NAME)
      @Timeout(TIMEOUT_VALUE)
      int method3(long param);

   }

}
