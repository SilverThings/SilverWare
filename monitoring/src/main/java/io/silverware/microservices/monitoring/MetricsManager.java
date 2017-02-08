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
package io.silverware.microservices.monitoring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * Class responsible for managing metrics. Class works with {@link DefaultMetric} and stores recorded values for one minute. Percentiles are updated every second in separate thread.
 */
public class MetricsManager {

   private static final Logger log = LogManager.getLogger(MetricsManager.class);

   private final Metrics metrics = new DefaultMetric();

   private final LoadingCache<Long, BigDecimal> mapValues = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build(new CacheLoader<Long, BigDecimal>() {
      @Override
      public BigDecimal load(Long key) throws Exception {
         return mapValues.get(key);
      }
   });

   private LongAdder longAdder = new LongAdder();

   private final Thread updateThread = new Thread(this::updatePercentiles);

   /**
    * Default constructor, sets true for daemon attribute of percentile updating thread and starts it.
    */
   public MetricsManager() {
      updateThread.setDaemon(true);
      updateThread.start();
   }

   /**
    * Method responsible for adding new time to values collection and also updating min, max, avg and count metrics.
    *
    * @param elapsedTime
    *       runtime of microservice method.
    */
   public void addTime(BigDecimal elapsedTime) {

      if (elapsedTime.compareTo(BigDecimal.ZERO) <= 0) {
         throw new IllegalArgumentException("Elapsed time is negative or zero");
      }

      mapValues.put(longAdder.longValue(), elapsedTime);

      longAdder.increment();

      final BigDecimal count = new BigDecimal(metrics.getCount());
      final BigDecimal averageTime = metrics.getAverageTime();
      final BigDecimal minTime = metrics.getMinTime();
      final BigDecimal maxTime = metrics.getMaxTime();

      metrics.incrementCount();

      metrics.setAverageTime((averageTime.multiply(count).add(elapsedTime)).divide(count.add(BigDecimal.ONE), BigDecimal.ROUND_HALF_UP));

      if (elapsedTime.compareTo(maxTime) >= 1) {
         metrics.setMaxTime(elapsedTime);
      } else {
         metrics.setMinTime(elapsedTime);
      }
   }

   /**
    * Getter for count value stored in {@link DefaultMetric}.
    *
    * @return count attribute.
    */
   public long getCount() {
      return metrics.getCount();
   }

   /**
    * Getter for average time stored in {@link DefaultMetric}.
    *
    * @return average time attribute.
    */
   public BigDecimal getAverageTime() {
      return metrics.getAverageTime();
   }

   /**
    * Getter for minimal time stored in {@link DefaultMetric}.
    *
    * @return minimal time attribute.
    */
   public BigDecimal getMinTime() {
      return metrics.getMinTime();
   }

   /**
    * Getter for maximal time stored in {@link DefaultMetric}.
    *
    * @return maximal time attribute.
    */
   public BigDecimal getMaxTime() {
      return metrics.getMaxTime();
   }

   /**
    * Getter for 90 percentile stored in {@link DefaultMetric}.
    *
    * @return 90 percentile attribute.
    */
   public BigDecimal get90Percentile() {
      return metrics.get90Percentile();
   }

   /**
    * Getter for 95 percentile stored in {@link DefaultMetric}.
    *
    * @return 95 percentile attribute.
    */
   public BigDecimal get95Percentile() {
      return metrics.get95Percentile();
   }

   /**
    * Getter for 99 percentile stored in {@link DefaultMetric}.
    *
    * @return 99 percentile attribute.
    */
   public BigDecimal get99Percentile() {
      return metrics.get99Percentile();
   }

   /**
    * Returns unmodifiable {@link List} of values currently being stored. Note: values expire after one minute.
    *
    * @return unmodifiable {@link List}.
    */
   public List<BigDecimal> getValues() {
      List<BigDecimal> values = new ArrayList<>(mapValues.asMap().values());
      Collections.sort(values);
      return Collections.unmodifiableList(values);
   }

   /**
    * Returns JSON representation of recorded metrics.
    *
    * @return {@link String} holding JSON representation of metrics.
    */
   public String toJson() {
      ObjectMapper mapper = new ObjectMapper();
      String jsonString = null;
      try {
         jsonString = mapper.writeValueAsString(metrics);
      } catch (JsonProcessingException e) {
         log.warn("Could not return metrics: ", e);
      }
      return jsonString;
   }

   @Override
   public String toString() {
      return metrics.toString();
   }

   private void updatePercentiles() {

      while (!Thread.currentThread().isInterrupted()) {
         List<BigDecimal> values = getValues();

         if (!values.isEmpty()) {
            final BigDecimal percentile90 = values.get((int) (values.size() * 0.90));
            final BigDecimal percentile95 = values.get((int) (values.size() * 0.95));
            final BigDecimal percentile99 = values.get((int) (values.size() * 0.99));

            metrics.set90Percentile(percentile90);
            metrics.set95Percentile(percentile95);
            metrics.set99Percentile(percentile99);
         }

         try {
            TimeUnit.SECONDS.sleep(1);
         } catch (InterruptedException e) {
            // nps, we simply ignore
         }
      }
   }
}
