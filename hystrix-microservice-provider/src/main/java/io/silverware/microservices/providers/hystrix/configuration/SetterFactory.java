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

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixCommandProperties.ExecutionIsolationStrategy;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;

import java.util.Map;

/**
 * Creates HystrixCommand.Setter instance from MethodConfig.
 */
public class SetterFactory {

   /**
    * Creates HystrixCommand.Setter instance from MethodConfig.
    *
    * @param methodConfig
    *       method configuration
    * @param groupName
    *       service name
    * @param commandName
    *       unique command name
    * @return hystrix command settter
    */
   public static HystrixCommand.Setter createHystrixCommandSetter(String groupName, String commandName, MethodConfig methodConfig) {
      if (!methodConfig.isHystrixActive()) {
         throw new IllegalStateException("Hystrix is not active");
      }

      groupName = getNonEmptyOrDefault(methodConfig.getGroupKey(), groupName);
      HystrixCommandGroupKey groupKey = HystrixCommandGroupKey.Factory.asKey(groupName);

      commandName = getNonEmptyOrDefault(methodConfig.getCommandKey(), commandName);
      HystrixCommandKey commandKey = HystrixCommandKey.Factory.asKey(commandName);

      String threadPoolName = getNonEmptyOrDefault(methodConfig.getThreadPoolKey(), groupName);
      HystrixThreadPoolKey threadPoolKey = HystrixThreadPoolKey.Factory.asKey(threadPoolName);

      Map<String, String> commandProperties = methodConfig.getCommandProperties();
      HystrixCommandProperties.Setter commandPropertiesSetter = createHystrixCommandPropertiesSetter(commandProperties);

      Map<String, String> threadPoolProperties = methodConfig.getThreadPoolProperties();
      HystrixThreadPoolProperties.Setter threadPoolPropertiesSetter = createHystrixThreadPoolProperties(threadPoolProperties);

      return Setter.withGroupKey(groupKey)
                   .andCommandKey(commandKey)
                   .andThreadPoolKey(threadPoolKey)
                   .andCommandPropertiesDefaults(commandPropertiesSetter)
                   .andThreadPoolPropertiesDefaults(threadPoolPropertiesSetter);
   }

   private static String getNonEmptyOrDefault(String value, String defaultValue) {
      return value != null && !value.isEmpty() ? value : defaultValue;
   }

   private static HystrixCommandProperties.Setter createHystrixCommandPropertiesSetter(Map<String, String> commandProperties) {
      HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter();

      String circuitBreakerEnabled = commandProperties.get(CommandProperties.CIRCUIT_BREAKER_ENABLED);
      if (circuitBreakerEnabled != null) {
         setter.withCircuitBreakerEnabled(Boolean.parseBoolean(circuitBreakerEnabled));
      }

      String circuitBreakerErrorThresholdPercentage = commandProperties.get(CommandProperties.CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE);
      if (circuitBreakerErrorThresholdPercentage != null) {
         setter.withCircuitBreakerErrorThresholdPercentage(Integer.parseInt(circuitBreakerErrorThresholdPercentage));
      }

      String circuitBreakerForceClosed = commandProperties.get(CommandProperties.CIRCUIT_BREAKER_FORCE_CLOSED);
      if (circuitBreakerForceClosed != null) {
         setter.withCircuitBreakerForceClosed(Boolean.parseBoolean(circuitBreakerForceClosed));
      }

      String circuitBreakerForceOpen = commandProperties.get(CommandProperties.CIRCUIT_BREAKER_FORCE_OPEN);
      if (circuitBreakerForceOpen != null) {
         setter.withCircuitBreakerForceOpen(Boolean.parseBoolean(circuitBreakerForceOpen));
      }

      String circuitBreakerRequestVolumeThreshold = commandProperties.get(CommandProperties.CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD);
      if (circuitBreakerRequestVolumeThreshold != null) {
         setter.withCircuitBreakerRequestVolumeThreshold(Integer.parseInt(circuitBreakerRequestVolumeThreshold));
      }

      String circuitBreakerSleepWindowInMilliseconds = commandProperties.get(CommandProperties.CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS);
      if (circuitBreakerSleepWindowInMilliseconds != null) {
         setter.withCircuitBreakerSleepWindowInMilliseconds(Integer.parseInt(circuitBreakerSleepWindowInMilliseconds));
      }

      String executionIsolationSemaphoreMaxConcurrentRequests = commandProperties.get(CommandProperties.EXECUTION_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS);
      if (executionIsolationSemaphoreMaxConcurrentRequests != null) {
         setter.withExecutionIsolationSemaphoreMaxConcurrentRequests(Integer.parseInt(executionIsolationSemaphoreMaxConcurrentRequests));
      }

      String executionIsolationStrategy = commandProperties.get(CommandProperties.EXECUTION_ISOLATION_STRATEGY);
      if (executionIsolationStrategy != null) {
         setter.withExecutionIsolationStrategy(ExecutionIsolationStrategy.valueOf(executionIsolationStrategy));
      }

      String executionIsolationThreadInterruptOnFutureCancel = commandProperties.get(CommandProperties.EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_CANCEL);
      if (executionIsolationThreadInterruptOnFutureCancel != null) {
         setter.withExecutionIsolationThreadInterruptOnFutureCancel(Boolean.valueOf(executionIsolationThreadInterruptOnFutureCancel));
      }

      String executionIsolationThreadInterruptOnTimeout = commandProperties.get(CommandProperties.EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_TIMEOUT);
      if (executionIsolationThreadInterruptOnTimeout != null) {
         setter.withExecutionIsolationThreadInterruptOnTimeout(Boolean.valueOf(executionIsolationThreadInterruptOnTimeout));
      }

      String executionTimeoutInMilliseconds = commandProperties.get(CommandProperties.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS);
      if (executionTimeoutInMilliseconds != null) {
         setter.withExecutionTimeoutInMilliseconds(Integer.parseInt(executionTimeoutInMilliseconds));
      }

      String executionTimeoutEnabled = commandProperties.get(CommandProperties.EXECUTION_TIMEOUT_ENABLED);
      if (executionTimeoutEnabled != null) {
         setter.withExecutionTimeoutEnabled(Boolean.parseBoolean(executionTimeoutEnabled));
      }

      String fallbackEnabled = commandProperties.get(CommandProperties.FALLBACK_ENABLED);
      if (fallbackEnabled != null) {
         setter.withFallbackEnabled(Boolean.parseBoolean(fallbackEnabled));
      }

      String fallbackIsolationSemaphoreMaxConcurrentRequests = commandProperties.get(CommandProperties.FALLBACK_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS);
      if (fallbackIsolationSemaphoreMaxConcurrentRequests != null) {
         setter.withFallbackIsolationSemaphoreMaxConcurrentRequests(Integer.parseInt(fallbackIsolationSemaphoreMaxConcurrentRequests));
      }

      String metricsHealthSnapshotIntervalInMilliseconds = commandProperties.get(CommandProperties.METRICS_HEALTH_SNAPSHOT_INTERVAL_IN_MILLISECONDS);
      if (metricsHealthSnapshotIntervalInMilliseconds != null) {
         setter.withMetricsHealthSnapshotIntervalInMilliseconds(Integer.parseInt(metricsHealthSnapshotIntervalInMilliseconds));
      }

      String metricsRollingPercentileBucketSize = commandProperties.get(CommandProperties.METRICS_ROLLING_PERCENTILE_BUCKET_SIZE);
      if (metricsRollingPercentileBucketSize != null) {
         setter.withMetricsRollingPercentileBucketSize(Integer.parseInt(metricsRollingPercentileBucketSize));
      }

      String metricsRollingPercentileEnabled = commandProperties.get(CommandProperties.METRICS_ROLLING_PERCENTILE_ENABLED);
      if (metricsRollingPercentileEnabled != null) {
         setter.withMetricsRollingPercentileEnabled(Boolean.parseBoolean(metricsRollingPercentileEnabled));
      }

      String metricsRollingPercentileWindowBuckets = commandProperties.get(CommandProperties.METRICS_ROLLING_PERCENTILE_NUM_BUCKETS);
      if (metricsRollingPercentileWindowBuckets != null) {
         setter.withMetricsRollingPercentileWindowBuckets(Integer.parseInt(metricsRollingPercentileWindowBuckets));
      }

      String metricsRollingPercentileWindowInMilliseconds = commandProperties.get(CommandProperties.METRICS_ROLLING_PERCENTILE_TIME_IN_MILLISECONDS);
      if (metricsRollingPercentileWindowInMilliseconds != null) {
         setter.withMetricsRollingPercentileWindowInMilliseconds(Integer.parseInt(metricsRollingPercentileWindowInMilliseconds));
      }

      String metricsRollingStatisticalWindowBuckets = commandProperties.get(CommandProperties.METRICS_ROLLING_STATS_NUM_BUCKETS);
      if (metricsRollingStatisticalWindowBuckets != null) {
         setter.withMetricsRollingStatisticalWindowBuckets(Integer.parseInt(metricsRollingStatisticalWindowBuckets));
      }

      String metricsRollingStatisticalWindowInMilliseconds = commandProperties.get(CommandProperties.METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS);
      if (metricsRollingStatisticalWindowInMilliseconds != null) {
         setter.withMetricsRollingStatisticalWindowInMilliseconds(Integer.parseInt(metricsRollingStatisticalWindowInMilliseconds));
      }

      String requestCacheEnabled = commandProperties.get(CommandProperties.REQUEST_CACHE_ENABLED);
      if (requestCacheEnabled != null) {
         setter.withRequestCacheEnabled(Boolean.parseBoolean(requestCacheEnabled));
      }

      String requestLogEnabled = commandProperties.get(CommandProperties.REQUEST_LOG_ENABLED);
      if (requestLogEnabled != null) {
         setter.withRequestLogEnabled(Boolean.parseBoolean(requestLogEnabled));
      }

      return setter;
   }

   private static HystrixThreadPoolProperties.Setter createHystrixThreadPoolProperties(Map<String, String> threadPoolProperties) {
      HystrixThreadPoolProperties.Setter setter = HystrixThreadPoolProperties.Setter();

      String coreSize = threadPoolProperties.get(ThreadPoolProperties.CORE_SIZE);
      if (coreSize != null) {
         setter.withCoreSize(Integer.parseInt(coreSize));
      }

      String maximumSize = threadPoolProperties.get(ThreadPoolProperties.MAXIMUM_SIZE);
      if (maximumSize != null) {
         setter.withMaximumSize(Integer.parseInt(maximumSize));
      }

      String keepAliveTimeMinutes = threadPoolProperties.get(ThreadPoolProperties.KEEP_ALIVE_TIME_MINUTES);
      if (keepAliveTimeMinutes != null) {
         setter.withKeepAliveTimeMinutes(Integer.parseInt(keepAliveTimeMinutes));
      }

      String maxQueueSize = threadPoolProperties.get(ThreadPoolProperties.MAX_QUEUE_SIZE);
      if (maxQueueSize != null) {
         setter.withMaxQueueSize(Integer.parseInt(maxQueueSize));
      }

      String metricsRollingStatisticalWindowBuckets = threadPoolProperties.get(ThreadPoolProperties.METRICS_ROLLING_STATS_NUM_BUCKETS);
      if (metricsRollingStatisticalWindowBuckets != null) {
         setter.withMetricsRollingStatisticalWindowBuckets(Integer.parseInt(metricsRollingStatisticalWindowBuckets));
      }

      String metricsRollingStatisticalWindowInMilliseconds = threadPoolProperties.get(ThreadPoolProperties.METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS);
      if (metricsRollingStatisticalWindowInMilliseconds != null) {
         setter.withMetricsRollingStatisticalWindowInMilliseconds(Integer.parseInt(metricsRollingStatisticalWindowInMilliseconds));
      }

      String queueSizeRejectionThreshold = threadPoolProperties.get(ThreadPoolProperties.QUEUE_SIZE_REJECTION_THRESHOLD);
      if (queueSizeRejectionThreshold != null) {
         setter.withQueueSizeRejectionThreshold(Integer.parseInt(queueSizeRejectionThreshold));
      }

      return setter;
   }

}
