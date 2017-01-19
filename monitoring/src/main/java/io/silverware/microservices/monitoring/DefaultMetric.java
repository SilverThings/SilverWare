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

import java.math.BigDecimal;

/**
 * Default metric implemenatation for microservice monitoring.
 */
public class DefaultMetric implements Metrics {

   /**
    * Average time attribute. Default value is BigDecimal.ZERO.
    */
   private BigDecimal averageTime = BigDecimal.ZERO;

   /**
    * Minimal time attribute. Default value is BigDecimal.ZERO.
    */
   private BigDecimal minTime = BigDecimal.ZERO;

   /**
    * Maximal time attribute. Default value is BigDecimal.ZERO.
    */
   private BigDecimal maxTime = BigDecimal.ZERO;

   /**
    * 90 percentile attribute. Default value is BigDecimal.ZERO.
    */
   private BigDecimal percentile90 = BigDecimal.ZERO;

   /**
    * 95 percentile attribute. Default value is BigDecimal.ZERO.
    */
   private BigDecimal percentile95 = BigDecimal.ZERO;

   /**
    * 99 percentile attribute. Default value is BigDecimal.ZERO.
    */
   private BigDecimal percentile99 = BigDecimal.ZERO;

   /**
    * Counter attribute for keeping track of how many values have been recorded so far.
    */
   private long count = 0L;

   public BigDecimal getAverageTime() {
      return averageTime;
   }

   public void setAverageTime(BigDecimal averageTime) {
      this.averageTime = averageTime;
   }

   public BigDecimal getMinTime() {
      return minTime;
   }

   public void setMinTime(BigDecimal minTime) {
      this.minTime = minTime;
   }

   public BigDecimal getMaxTime() {
      return maxTime;
   }

   public void setMaxTime(BigDecimal maxTime) {
      this.maxTime = maxTime;
   }

   public BigDecimal get90Percentile() {
      return percentile90;
   }

   public void set90Percentile(BigDecimal percentile90) {
      this.percentile90 = percentile90;
   }

   public BigDecimal get95Percentile() {
      return percentile95;
   }

   public void set95Percentile(BigDecimal percentile95) {
      this.percentile95 = percentile95;
   }

   public BigDecimal get99Percentile() {
      return percentile99;
   }

   public void set99Percentile(BigDecimal percentile99) {
      this.percentile99 = percentile99;
   }

   public long getCount() {
      return count;
   }

   public void incrementCount() {
      count++;
   }

   @Override
   public String toString() {
      return "Entity{" +
            "averageTime=" + averageTime +
            ", minTime=" + minTime +
            ", maxTime=" + maxTime +
            ", count=" + count +
            '}';
   }
}
