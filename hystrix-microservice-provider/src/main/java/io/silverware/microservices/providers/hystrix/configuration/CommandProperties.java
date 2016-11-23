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

/**
 * Keys to command properties stored in {@link MethodConfig}.
 *
 * @see <a href="https://github.com/Netflix/Hystrix/wiki/Configuration#CommandProperties">https://github.com/Netflix/Hystrix/wiki/Configuration#CommandProperties</a>
 */
public interface CommandProperties {

   String CIRCUIT_BREAKER_ENABLED = "circuitBreaker.enabled";
   String CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE = "circuitBreaker.errorThresholdPercentage";
   String CIRCUIT_BREAKER_FORCE_CLOSED = "circuitBreaker.forceClosed";
   String CIRCUIT_BREAKER_FORCE_OPEN = "circuitBreaker.forceOpen";
   String CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD = "circuitBreaker.requestVolumeThreshold";
   String CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS = "circuitBreaker.sleepWindowInMilliseconds";

   String EXECUTION_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS = "execution.isolation.semaphore.maxConcurrentRequests";
   String EXECUTION_ISOLATION_STRATEGY = "execution.isolation.strategy";
   String EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_TIMEOUT = "execution.isolation.thread.interruptOnTimeout";
   String EXECUTION_ISOLATION_THREAD_INTERRUPT_ON_CANCEL = "execution.isolation.thread.interruptOnCancel";
   String EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS = "execution.isolation.thread.timeoutInMilliseconds";
   String EXECUTION_TIMEOUT_ENABLED = "execution.timeout.enabled";

   String FALLBACK_ENABLED = "fallback.enabled";
   String FALLBACK_ISOLATION_SEMAPHORE_MAX_CONCURRENT_REQUESTS = "fallback.isolation.semaphore.maxConcurrentRequests";

   String METRICS_HEALTH_SNAPSHOT_INTERVAL_IN_MILLISECONDS = "metrics.healthSnapshot.intervalInMilliseconds";
   String METRICS_ROLLING_PERCENTILE_BUCKET_SIZE = "metrics.rollingPercentile.bucketSize";
   String METRICS_ROLLING_PERCENTILE_ENABLED = "metrics.rollingPercentile.enabled";
   String METRICS_ROLLING_PERCENTILE_NUM_BUCKETS = "metrics.rollingPercentile.numBuckets";
   String METRICS_ROLLING_PERCENTILE_TIME_IN_MILLISECONDS = "metrics.rollingPercentile.timeInMilliseconds";
   String METRICS_ROLLING_STATS_NUM_BUCKETS = "metrics.rollingStats.numBuckets";
   String METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS = "metrics.rollingStats.timeInMilliseconds";

   String REQUEST_CACHE_ENABLED = "requestCache.enabled";
   String REQUEST_LOG_ENABLED = "requestLog.enabled";

}
