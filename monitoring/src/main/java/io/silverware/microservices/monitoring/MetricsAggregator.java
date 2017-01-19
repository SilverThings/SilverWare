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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.monitoring.annotations.NotMonitored;

/**
 * Static method for storage and exposure of all metrics recorded.
 */
@Microservice
public class MetricsAggregator {

   /**
    * Register storing {@link MetricsManager} for every method being called.
    */
   private static final Map<Method, MetricsManager> microserviceRegister = new HashMap<>();

   private MetricsAggregator() {
   }

   /**
    * Method for adding new microservice method into MetricsAggregator. Method is not monitored by {@link MonitoringMethodHandler}.
    *
    * @param method method to be added.
    * @param time runtime of method to be added.
    */
   @NotMonitored
   public static void addMicroserviceMethod(Method method, BigDecimal time) {
      MetricsManager metricsManager = microserviceRegister.get(method);
      if (metricsManager == null) {
         metricsManager = new MetricsManager();
         metricsManager.addTime(time);
         microserviceRegister.put(method, metricsManager);
      } else {
         metricsManager.addTime(time);
      }
   }

   /**
    * Method for exposing recorded metrics. Method is not monitored by {@link MonitoringMethodHandler}.
    *
    * @return metrics in JSON format.
    */
   @NotMonitored
   public static String getMetrics() {
      if (microserviceRegister.isEmpty()) {
         return "";
      }
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      for (Map.Entry<Method, MetricsManager> e : microserviceRegister.entrySet()) {
         sb.append("\"").append(e.getKey().getDeclaringClass().getSimpleName()).append("::").append(e.getKey().getName()).append("\": ");
         sb.append(e.getValue().toJson()).append(",");
      }
      //remove last comma
      sb.deleteCharAt(sb.length() - 1);
      sb.append("}");
      return sb.toString();
   }

   /**
    * Method for exposing all recorded values. Method is not monitored by {@link MonitoringMethodHandler}.
    *
    * @return all recorded values in array-like format.
    */
   @NotMonitored
   public static String getValues() {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<Method, MetricsManager> e : microserviceRegister.entrySet()) {
         sb.append(e.getKey().getDeclaringClass().getSimpleName()).append("::").append(e.getKey().getName()).append(": ");
         for (BigDecimal bd : e.getValue().getValues()) {
            sb.append(bd).append(",");
         }
         //remove last comma
         sb.deleteCharAt(sb.length() - 1);
         sb.append("\n");
      }
      return sb.toString();
   }

   /**
    * Method showing how much values have been recorded for each microservice method. Method is not monitored by {@link MonitoringMethodHandler}.
    *
    * @return number of values for every microservice method in array-like format.
    */
   @NotMonitored
   public static String getValuesSize() {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<Method, MetricsManager> e : microserviceRegister.entrySet()) {
         sb.append(e.getKey().getDeclaringClass().getSimpleName()).append("::").append(e.getKey().getName()).append(": ").append(e.getValue().getValues().size()).append("\n");
      }
      return sb.toString();
   }

   /**
    * Method returning unmodifiable register storing {@link MetricsManager} for every method being called.
    *
    * @return unmodifiable register.
    */
   @NotMonitored
   public static Map<Method, MetricsManager> getMicroserviceRegister() {
      return Collections.unmodifiableMap(microserviceRegister);
   }
}
