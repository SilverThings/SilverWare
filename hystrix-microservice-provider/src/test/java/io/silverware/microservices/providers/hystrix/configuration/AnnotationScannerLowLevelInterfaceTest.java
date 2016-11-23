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

import static org.assertj.core.api.Assertions.assertThat;

import io.silverware.microservices.annotations.hystrix.HystrixConfig;
import io.silverware.microservices.annotations.hystrix.advanced.CacheKey;
import io.silverware.microservices.annotations.hystrix.advanced.DefaultProperties;
import io.silverware.microservices.annotations.hystrix.advanced.HystrixCommand;
import io.silverware.microservices.annotations.hystrix.advanced.HystrixProperty;

import org.assertj.core.api.SoftAssertions;
import org.jboss.weld.exceptions.IllegalArgumentException;
import org.testng.annotations.Test;

import java.util.Map;

public class AnnotationScannerLowLevelInterfaceTest extends AnnotationScannerTestBase {

   private static final String GROUP_KEY = "TestingGroupKey";
   private static final String COMMAND_KEY = "TestingCommandKey";
   private static final String THREAD_POOL_KEY = "TestingThreadPoolKey";

   private static final String CIRCUIT_BREAKER_ENABLED = "true";
   private static final String CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE = "33";
   private static final String CIRCUIT_BREAKER_FORCE_CLOSED = "false";
   private static final String CIRCUIT_BREAKER_FORCE_OPEN = "false";
   private static final String CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD = "100";
   private static final String CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS = "10000";

   private static final String EXECUTION_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS = "10";
   private static final String EXECUTION_ISOLATION_STRATEGY = "THREAD";
   private static final String EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_TIMEOUT = "true";
   private static final String EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_CANCEL = "true";
   private static final String EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS = "15000";
   private static final String EXECUTION_TIMEOUT_ENABLED = "true";

   private static final String FALLBACK_ENABLED = "true";
   private static final String FALLBACK_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS = "5";

   private static final String METRICS_HEALTH_SNAPSHOT_INTERVAL_IN_MILLISECONDS = "30000";
   private static final String METRICS_ROLLING_PERCENTILE_BUCKET_SIZE = "20";
   private static final String METRICS_ROLLING_PERCENTILE_ENABLED = "true";
   private static final String METRICS_ROLLING_PERCENTILE_NUM_BUCKETS = "15";
   private static final String METRICS_ROLLING_PERCENTILE_TIME_IN_MILLISECONDS = "500";
   private static final String METRICS_ROLLING_STATS_NUM_BUCKETS = "13";
   private static final String METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS = "1200";

   private static final String REQUEST_CACHE_ENABLED = "true";
   private static final String REQUEST_LOG_ENABLED = "false";

   private static final String CORE_SIZE = "10";
   private static final String KEEP_ALIVE_TIME_MINUTES = "5";
   private static final String MAX_QUEUE_SIZE = "20";
   private static final String QUEUE_SIZE_REJECTION_THRESHOLD = "42";

   private static final String OVERRIDDEN_GROUP_KEY = "OverriddenTestingGroupKey";
   private static final String OVERRIDDEN_CIRCUIT_BREAKER_ENABLED = "false";
   private static final String OVERRIDDEN_CORE_SIZE = "15";

   @HystrixConfig(HystrixCommandRemoteMicroservice.class)
   private RemoteMicroservice hystrixCommandMicroservice;

   @Test
   public void testHystrixCommandNotPresent() {
      ServiceConfig serviceConfig = scanToServiceConfig("hystrixCommandMicroservice");

      MethodConfig methodConfig = getMethodConfig(serviceConfig, METHOD_1);
      assertThat(methodConfig).isNotNull();
      assertThat(methodConfig.isHystrixActive()).isFalse();
   }

   @Test
   public void testHystrixCommandSomeProperties() {
      ServiceConfig serviceConfig = scanToServiceConfig("hystrixCommandMicroservice");

      MethodConfig methodConfig = getMethodConfig(serviceConfig, METHOD_2);
      assertThat(methodConfig).isNotNull();
      assertThat(methodConfig.isHystrixActive()).isTrue();

      assertThat(methodConfig.getGroupKey()).isEqualTo(GROUP_KEY);
      assertThat(methodConfig.getCommandKey()).isEqualTo(COMMAND_KEY);
      assertThat(methodConfig.getThreadPoolKey()).isNullOrEmpty();

      Map<String, String> commandProperties = methodConfig.getCommandProperties();
      assertThat(commandProperties).isNotNull().isNotEmpty();

      SoftAssertions assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ENABLED, CIRCUIT_BREAKER_ENABLED);
      assertions.assertThat(commandProperties).doesNotContainKey(CommandProperties.CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE);
      assertions.assertThat(commandProperties).doesNotContainKey(CommandProperties.CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD);
      assertions.assertThat(commandProperties).doesNotContainKey(CommandProperties.CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_TIMEOUT_ENABLED, EXECUTION_TIMEOUT_ENABLED);
      assertions.assertThat(commandProperties).doesNotContainKey(CommandProperties.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS);
      assertions.assertAll();

      Map<String, String> threadPoolProperties = methodConfig.getThreadPoolProperties();
      assertThat(threadPoolProperties).isNotNull().isNotEmpty();

      assertions = new SoftAssertions();
      assertions.assertThat(threadPoolProperties).containsEntry(ThreadPoolProperties.CORE_SIZE, CORE_SIZE);
      assertions.assertThat(threadPoolProperties).doesNotContainKey(ThreadPoolProperties.KEEP_ALIVE_TIME_MINUTES);
      assertions.assertThat(threadPoolProperties).containsEntry(ThreadPoolProperties.MAX_QUEUE_SIZE, MAX_QUEUE_SIZE);
      assertions.assertThat(threadPoolProperties).doesNotContainKey(ThreadPoolProperties.QUEUE_SIZE_REJECTION_THRESHOLD);
      assertions.assertAll();

      assertThat(methodConfig.getIgnoredExceptions()).containsOnly(NullPointerException.class);
   }

   @Test
   public void testHystrixCommandAllProperties() {
      ServiceConfig serviceConfig = scanToServiceConfig("hystrixCommandMicroservice");

      MethodConfig methodConfig = getMethodConfig(serviceConfig, METHOD_3);
      assertThat(methodConfig).isNotNull();
      assertThat(methodConfig.isHystrixActive()).isTrue();

      assertThat(methodConfig.getGroupKey()).isEqualTo(GROUP_KEY);
      assertThat(methodConfig.getCommandKey()).isEqualTo(COMMAND_KEY);
      assertThat(methodConfig.getThreadPoolKey()).isEqualTo(THREAD_POOL_KEY);

      Map<String, String> commandProperties = methodConfig.getCommandProperties();
      assertThat(commandProperties).isNotNull().isNotEmpty();

      SoftAssertions assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ENABLED, CIRCUIT_BREAKER_ENABLED);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE, CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_FORCE_CLOSED, CIRCUIT_BREAKER_FORCE_CLOSED);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_FORCE_OPEN, CIRCUIT_BREAKER_FORCE_OPEN);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD, CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS, CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS, EXECUTION_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_ISOLATION_STRATEGY, EXECUTION_ISOLATION_STRATEGY);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_TIMEOUT, EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_TIMEOUT);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_CANCEL, EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_CANCEL);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_TIMEOUT_ENABLED, EXECUTION_TIMEOUT_ENABLED);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.FALLBACK_ENABLED, FALLBACK_ENABLED);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.FALLBACK_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS, FALLBACK_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.METRICS_HEALTH_SNAPSHOT_INTERVAL_IN_MILLISECONDS, METRICS_HEALTH_SNAPSHOT_INTERVAL_IN_MILLISECONDS);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.METRICS_ROLLING_PERCENTILE_BUCKET_SIZE, METRICS_ROLLING_PERCENTILE_BUCKET_SIZE);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.METRICS_ROLLING_PERCENTILE_ENABLED, METRICS_ROLLING_PERCENTILE_ENABLED);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.METRICS_ROLLING_PERCENTILE_NUM_BUCKETS, METRICS_ROLLING_PERCENTILE_NUM_BUCKETS);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.METRICS_ROLLING_PERCENTILE_TIME_IN_MILLISECONDS, METRICS_ROLLING_PERCENTILE_TIME_IN_MILLISECONDS);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.METRICS_ROLLING_STATS_NUM_BUCKETS, METRICS_ROLLING_STATS_NUM_BUCKETS);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS, METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.REQUEST_CACHE_ENABLED, REQUEST_CACHE_ENABLED);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.REQUEST_LOG_ENABLED, REQUEST_LOG_ENABLED);
      assertions.assertAll();

      Map<String, String> threadPoolProperties = methodConfig.getThreadPoolProperties();
      assertThat(threadPoolProperties).isNotNull().isNotEmpty();

      assertions = new SoftAssertions();
      assertions.assertThat(threadPoolProperties).containsEntry(ThreadPoolProperties.CORE_SIZE, CORE_SIZE);
      assertions.assertThat(threadPoolProperties).containsEntry(ThreadPoolProperties.KEEP_ALIVE_TIME_MINUTES, KEEP_ALIVE_TIME_MINUTES);
      assertions.assertThat(threadPoolProperties).containsEntry(ThreadPoolProperties.MAX_QUEUE_SIZE, MAX_QUEUE_SIZE);
      assertions.assertThat(threadPoolProperties).containsEntry(ThreadPoolProperties.METRICS_ROLLING_STATS_NUM_BUCKETS, METRICS_ROLLING_STATS_NUM_BUCKETS);
      assertions.assertThat(threadPoolProperties).containsEntry(ThreadPoolProperties.METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS, METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS);
      assertions.assertThat(threadPoolProperties).containsEntry(ThreadPoolProperties.QUEUE_SIZE_REJECTION_THRESHOLD, QUEUE_SIZE_REJECTION_THRESHOLD);
      assertions.assertAll();

      assertThat(methodConfig.getIgnoredExceptions()).containsOnly(NullPointerException.class, IllegalArgumentException.class);
   }

   @HystrixConfig(DefaultPropertiesRemoteMicroservice.class)
   private RemoteMicroservice defaultPropertiesMicroservice;

   @Test
   public void testDefaultPropertiesOnly() {
      ServiceConfig serviceConfig = scanToServiceConfig("defaultPropertiesMicroservice");

      MethodConfig methodConfig = getMethodConfig(serviceConfig, METHOD_1);
      assertThat(methodConfig).isNotNull();
      assertThat(methodConfig.isHystrixActive()).isTrue();

      assertThat(methodConfig.getGroupKey()).isEqualTo(GROUP_KEY);
      assertThat(methodConfig.getCommandKey()).isNullOrEmpty();
      assertThat(methodConfig.getThreadPoolKey()).isNullOrEmpty();

      Map<String, String> commandProperties = methodConfig.getCommandProperties();
      assertThat(commandProperties).isNotNull().isNotEmpty();

      SoftAssertions assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ENABLED, CIRCUIT_BREAKER_ENABLED);
      assertions.assertThat(commandProperties).doesNotContainKey(CommandProperties.EXECUTION_TIMEOUT_ENABLED);
      assertions.assertAll();

      Map<String, String> threadPoolProperties = methodConfig.getThreadPoolProperties();
      assertThat(threadPoolProperties).isNotNull().isNotEmpty();

      assertions = new SoftAssertions();
      assertions.assertThat(threadPoolProperties).containsEntry(ThreadPoolProperties.CORE_SIZE, CORE_SIZE);
      assertions.assertThat(threadPoolProperties).doesNotContainKey(ThreadPoolProperties.MAX_QUEUE_SIZE);
      assertions.assertAll();

      assertThat(methodConfig.getIgnoredExceptions()).containsOnly(NullPointerException.class);
   }

   @Test
   public void testDefaultPropertiesEnhanced() {
      ServiceConfig serviceConfig = scanToServiceConfig("defaultPropertiesMicroservice");

      MethodConfig methodConfig = getMethodConfig(serviceConfig, METHOD_2);
      assertThat(methodConfig).isNotNull();
      assertThat(methodConfig.isHystrixActive()).isTrue();

      assertThat(methodConfig.getGroupKey()).isEqualTo(GROUP_KEY);
      assertThat(methodConfig.getCommandKey()).isEqualTo(COMMAND_KEY);
      assertThat(methodConfig.getThreadPoolKey()).isEqualTo(THREAD_POOL_KEY);

      Map<String, String> commandProperties = methodConfig.getCommandProperties();
      assertThat(commandProperties).isNotNull().isNotEmpty();

      SoftAssertions assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ENABLED, CIRCUIT_BREAKER_ENABLED);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_TIMEOUT_ENABLED, EXECUTION_TIMEOUT_ENABLED);
      assertions.assertAll();

      Map<String, String> threadPoolProperties = methodConfig.getThreadPoolProperties();
      assertThat(threadPoolProperties).isNotNull().isNotEmpty();

      assertions = new SoftAssertions();
      assertions.assertThat(threadPoolProperties).containsEntry(ThreadPoolProperties.CORE_SIZE, CORE_SIZE);
      assertions.assertThat(threadPoolProperties).containsEntry(ThreadPoolProperties.MAX_QUEUE_SIZE, MAX_QUEUE_SIZE);
      assertions.assertAll();

      assertThat(methodConfig.getIgnoredExceptions()).containsOnly(NullPointerException.class, IllegalArgumentException.class);
   }

   @Test
   public void testDefaultPropertiesOverridden() {
      ServiceConfig serviceConfig = scanToServiceConfig("defaultPropertiesMicroservice");

      MethodConfig methodConfig = getMethodConfig(serviceConfig, METHOD_3);
      assertThat(methodConfig).isNotNull();
      assertThat(methodConfig.isHystrixActive()).isTrue();

      assertThat(methodConfig.getGroupKey()).isEqualTo(OVERRIDDEN_GROUP_KEY);
      assertThat(methodConfig.getCommandKey()).isNullOrEmpty();
      assertThat(methodConfig.getThreadPoolKey()).isNullOrEmpty();

      Map<String, String> commandProperties = methodConfig.getCommandProperties();
      assertThat(commandProperties).isNotNull().isNotEmpty();

      SoftAssertions assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ENABLED, OVERRIDDEN_CIRCUIT_BREAKER_ENABLED);
      assertions.assertThat(commandProperties).doesNotContainKey(CommandProperties.EXECUTION_TIMEOUT_ENABLED);
      assertions.assertAll();

      Map<String, String> threadPoolProperties = methodConfig.getThreadPoolProperties();
      assertThat(threadPoolProperties).isNotNull().isNotEmpty();

      assertions = new SoftAssertions();
      assertions.assertThat(threadPoolProperties).containsEntry(ThreadPoolProperties.CORE_SIZE, OVERRIDDEN_CORE_SIZE);
      assertions.assertThat(threadPoolProperties).doesNotContainKey(ThreadPoolProperties.MAX_QUEUE_SIZE);
      assertions.assertAll();
   }

   @HystrixConfig(CacheKeyRemoteMicroservice.class)
   private RemoteMicroservice cacheKeyMicroservice;

   @Test
   public void testCacheKey() {
      ServiceConfig serviceConfig = scanToServiceConfig("cacheKeyMicroservice");

      MethodConfig methodConfig = getMethodConfig(serviceConfig, METHOD_1);
      assertThat(methodConfig).isNotNull();
      assertThat(methodConfig.getCacheKeyParameterIndexes()).isEmpty();

      methodConfig = getMethodConfig(serviceConfig, METHOD_2);
      assertThat(methodConfig).isNotNull();
      assertThat(methodConfig.getCacheKeyParameterIndexes()).isEmpty();

      methodConfig = getMethodConfig(serviceConfig, METHOD_3);
      assertThat(methodConfig).isNotNull();
      assertThat(methodConfig.getCacheKeyParameterIndexes()).containsOnly(0);

      methodConfig = getMethodConfig(serviceConfig, METHOD_4);
      assertThat(methodConfig).isNotNull();
      assertThat(methodConfig.getCacheKeyParameterIndexes()).containsOnly(1);
   }

   private interface HystrixCommandRemoteMicroservice extends RemoteMicroservice {

      void method1(Object param);

      @HystrixCommand(
            groupKey = GROUP_KEY,
            commandKey = COMMAND_KEY,
            commandProperties = {
                  @HystrixProperty(name = CommandProperties.CIRCUIT_BREAKER_ENABLED, value = CIRCUIT_BREAKER_ENABLED),
                  @HystrixProperty(name = CommandProperties.EXECUTION_TIMEOUT_ENABLED, value = EXECUTION_TIMEOUT_ENABLED)
            },
            threadPoolProperties = {
                  @HystrixProperty(name = ThreadPoolProperties.CORE_SIZE, value = CORE_SIZE),
                  @HystrixProperty(name = ThreadPoolProperties.MAX_QUEUE_SIZE, value = MAX_QUEUE_SIZE)
            },
            ignoredExceptions = NullPointerException.class
      )
      String method2();

      @HystrixCommand(
            groupKey = GROUP_KEY,
            commandKey = COMMAND_KEY,
            threadPoolKey = THREAD_POOL_KEY,
            commandProperties = {
                  @HystrixProperty(name = CommandProperties.CIRCUIT_BREAKER_ENABLED, value = CIRCUIT_BREAKER_ENABLED),
                  @HystrixProperty(name = CommandProperties.CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE, value = CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE),
                  @HystrixProperty(name = CommandProperties.CIRCUIT_BREAKER_FORCE_CLOSED, value = CIRCUIT_BREAKER_FORCE_CLOSED),
                  @HystrixProperty(name = CommandProperties.CIRCUIT_BREAKER_FORCE_OPEN, value = CIRCUIT_BREAKER_FORCE_OPEN),
                  @HystrixProperty(name = CommandProperties.CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD, value = CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD),
                  @HystrixProperty(name = CommandProperties.CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS, value = CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS),
                  @HystrixProperty(name = CommandProperties.EXECUTION_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS, value = EXECUTION_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS),
                  @HystrixProperty(name = CommandProperties.EXECUTION_ISOLATION_STRATEGY, value = EXECUTION_ISOLATION_STRATEGY),
                  @HystrixProperty(name = CommandProperties.EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_TIMEOUT, value = EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_TIMEOUT),
                  @HystrixProperty(name = CommandProperties.EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_CANCEL, value = EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_CANCEL),
                  @HystrixProperty(name = CommandProperties.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, value = EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS),
                  @HystrixProperty(name = CommandProperties.EXECUTION_TIMEOUT_ENABLED, value = EXECUTION_TIMEOUT_ENABLED),
                  @HystrixProperty(name = CommandProperties.FALLBACK_ENABLED, value = FALLBACK_ENABLED),
                  @HystrixProperty(name = CommandProperties.FALLBACK_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS, value = FALLBACK_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS),
                  @HystrixProperty(name = CommandProperties.METRICS_HEALTH_SNAPSHOT_INTERVAL_IN_MILLISECONDS, value = METRICS_HEALTH_SNAPSHOT_INTERVAL_IN_MILLISECONDS),
                  @HystrixProperty(name = CommandProperties.METRICS_ROLLING_PERCENTILE_BUCKET_SIZE, value = METRICS_ROLLING_PERCENTILE_BUCKET_SIZE),
                  @HystrixProperty(name = CommandProperties.METRICS_ROLLING_PERCENTILE_ENABLED, value = METRICS_ROLLING_PERCENTILE_ENABLED),
                  @HystrixProperty(name = CommandProperties.METRICS_ROLLING_PERCENTILE_NUM_BUCKETS, value = METRICS_ROLLING_PERCENTILE_NUM_BUCKETS),
                  @HystrixProperty(name = CommandProperties.METRICS_ROLLING_PERCENTILE_TIME_IN_MILLISECONDS, value = METRICS_ROLLING_PERCENTILE_TIME_IN_MILLISECONDS),
                  @HystrixProperty(name = CommandProperties.METRICS_ROLLING_STATS_NUM_BUCKETS, value = METRICS_ROLLING_STATS_NUM_BUCKETS),
                  @HystrixProperty(name = CommandProperties.METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS, value = METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS),
                  @HystrixProperty(name = CommandProperties.REQUEST_CACHE_ENABLED, value = REQUEST_CACHE_ENABLED),
                  @HystrixProperty(name = CommandProperties.REQUEST_LOG_ENABLED, value = REQUEST_LOG_ENABLED)
            },
            threadPoolProperties = {
                  @HystrixProperty(name = ThreadPoolProperties.CORE_SIZE, value = CORE_SIZE),
                  @HystrixProperty(name = ThreadPoolProperties.KEEP_ALIVE_TIME_MINUTES, value = KEEP_ALIVE_TIME_MINUTES),
                  @HystrixProperty(name = ThreadPoolProperties.MAX_QUEUE_SIZE, value = MAX_QUEUE_SIZE),
                  @HystrixProperty(name = ThreadPoolProperties.METRICS_ROLLING_STATS_NUM_BUCKETS, value = METRICS_ROLLING_STATS_NUM_BUCKETS),
                  @HystrixProperty(name = ThreadPoolProperties.METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS, value = METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS),
                  @HystrixProperty(name = ThreadPoolProperties.QUEUE_SIZE_REJECTION_THRESHOLD, value = QUEUE_SIZE_REJECTION_THRESHOLD)
            },
            ignoredExceptions = { NullPointerException.class, IllegalArgumentException.class }
      )
      int method3(long param);

   }

   @DefaultProperties(
         groupKey = GROUP_KEY,
         commandProperties = {
               @HystrixProperty(name = CommandProperties.CIRCUIT_BREAKER_ENABLED, value = CIRCUIT_BREAKER_ENABLED)
         },
         threadPoolProperties = {
               @HystrixProperty(name = ThreadPoolProperties.CORE_SIZE, value = CORE_SIZE)
         },
         ignoredExceptions = NullPointerException.class
   )
   private interface DefaultPropertiesRemoteMicroservice extends RemoteMicroservice {

      void method1(Object param);

      @HystrixCommand(
            commandKey = COMMAND_KEY,
            threadPoolKey = THREAD_POOL_KEY,
            commandProperties = {
                  @HystrixProperty(name = CommandProperties.EXECUTION_TIMEOUT_ENABLED, value = EXECUTION_TIMEOUT_ENABLED)
            },
            threadPoolProperties = {
                  @HystrixProperty(name = ThreadPoolProperties.MAX_QUEUE_SIZE, value = MAX_QUEUE_SIZE)
            },
            ignoredExceptions = IllegalArgumentException.class
      )
      String method2();

      @HystrixCommand(
            groupKey = OVERRIDDEN_GROUP_KEY,
            commandProperties = {
                  @HystrixProperty(name = CommandProperties.CIRCUIT_BREAKER_ENABLED, value = OVERRIDDEN_CIRCUIT_BREAKER_ENABLED)
            },
            threadPoolProperties = {
                  @HystrixProperty(name = ThreadPoolProperties.CORE_SIZE, value = OVERRIDDEN_CORE_SIZE)
            }
      )
      int method3(long param);

   }

   private interface CacheKeyRemoteMicroservice extends RemoteMicroservice {

      void method1(Object param);

      String method2();

      int method3(@CacheKey long param);

      long method4(long param1, @CacheKey long param2);

   }

}
