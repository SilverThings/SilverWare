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
package io.silverware.microservices.providers.hystrix.configuration;

/**
 * Keys to thread pool properties stored in {@link MethodConfig}.
 *
 * @see <a href="https://github.com/Netflix/Hystrix/wiki/Configuration#ThreadPool">https://github.com/Netflix/Hystrix/wiki/Configuration#ThreadPool</a>
 */
public interface ThreadPoolProperties {

   String CORE_SIZE = "coreSize";
   String MAXIMUM_SIZE = "maximumSize";
   String ALLOW_MAXIMUM_SIZE_TO_DIVERGE_FROM_CORE_SIZE = "allowMaximumSizeToDivergeFromCoreSize";
   String KEEP_ALIVE_TIME_MINUTES = "keepAliveTimeMinutes";
   String MAX_QUEUE_SIZE = "maxQueueSize";
   String METRICS_ROLLING_STATS_NUM_BUCKETS = "metrics.rollingStats.numBuckets";
   String METRICS_ROLLING_STATS_TIME_IN_MILLISECONDS = "metrics.rollingStats.timeInMilliseconds";
   String QUEUE_SIZE_REJECTION_THRESHOLD = "queueSizeRejectionThreshold";

}
