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
 * Metrics interface specifying default metrics every Metrics implementation should consist of.
 */
public interface Metrics {

   /**
    * Getter returning average time.
    *
    * @return average time.
    */
   BigDecimal getAverageTime();

   /**
    * Setter for setting average time.
    *
    * @param averageTime average time.
    */
   void setAverageTime(BigDecimal averageTime);

   /**
    * Getter returning minimal time.
    *
    * @return minimal time.
    */
   BigDecimal getMinTime();

   /**
    * Setter for setting minimal time.
    *
    * @param minTime minimal time.
    */
   void setMinTime(BigDecimal minTime);

   /**
    * Getter returning maximal time.
    *
    * @return maximal time.
    */
   BigDecimal getMaxTime();

   /**
    * Setter for setting maximal time.
    *
    * @param maxTime maximal time.
    */
   void setMaxTime(BigDecimal maxTime);

   /**
    * Getter returning 90 percentile.
    *
    * @return 90 percentile.
    */
   BigDecimal get90Percentile();

   /**
    * Setter for setting 90 percentile.
    *
    * @param percentile90 percentile.
    */
   void set90Percentile(BigDecimal percentile90);

   /**
    * Getter returning 95 percentile.
    *
    * @return 95 percentile.
    */
   BigDecimal get95Percentile();

   /**
    * Setter for setting 95 percentile.
    *
    * @param percentile95 percentile.
    */
   void set95Percentile(BigDecimal percentile95);

   /**
    * Getter returning 99 percentile.
    *
    * @return 99 percentile.
    */
   BigDecimal get99Percentile();

   /**
    * Setter for setting 99 percentile.
    *
    * @param percentile99 percentile.
    */
   void set99Percentile(BigDecimal percentile99);

   /**
    * Getter returning count of recorded metrics.
    *
    * @return count.
    */
   long getCount();

   /**
    * Method for incrementing counter.
    */
   void incrementCount();
}
