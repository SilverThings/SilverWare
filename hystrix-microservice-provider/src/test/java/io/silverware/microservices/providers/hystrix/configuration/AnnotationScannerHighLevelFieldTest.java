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

import io.silverware.microservices.annotations.hystrix.basic.Cached;
import io.silverware.microservices.annotations.hystrix.basic.CircuitBreaker;
import io.silverware.microservices.annotations.hystrix.basic.Fail;
import io.silverware.microservices.annotations.hystrix.basic.ThreadPool;
import io.silverware.microservices.annotations.hystrix.basic.Timeout;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

import java.util.Map;

public class AnnotationScannerHighLevelFieldTest extends AnnotationScannerTestBase {

   private static final int CIRCUIT_BREAKER_ERROR_PERCENTAGE = 10;
   private static final int CIRCUIT_BREAKER_REQUEST_VOLUME = 100;
   private static final int CIRCUIT_BREAKER_SLEEP_WINDOW = 1000;
   private static final String THREAD_POOL_NAME = "TestingThreadPool";
   private static final int TIMEOUT_VALUE = 2000;

   private Object noAnnotations;

   @Test
   public void testNoAnnotations() {
      Map<String, String> commandProperties = scanToCommandProperties("noAnnotations");

      SoftAssertions assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).doesNotContainKey(CommandProperties.CIRCUIT_BREAKER_ENABLED);
      assertions.assertThat(commandProperties).doesNotContainKey(CommandProperties.EXECUTION_TIMEOUT_ENABLED);
      assertions.assertThat(commandProperties).doesNotContainKey(CommandProperties.REQUEST_CACHE_ENABLED);
      assertions.assertAll();
   }

   @Cached
   private Object cached;

   @Test
   public void testCached() {
      Map<String, String> commandProperties = scanToCommandProperties("cached");

      SoftAssertions assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).doesNotContainKey(CommandProperties.CIRCUIT_BREAKER_ENABLED);
      assertions.assertThat(commandProperties).doesNotContainKey(CommandProperties.EXECUTION_TIMEOUT_ENABLED);
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.REQUEST_CACHE_ENABLED, Boolean.TRUE.toString());
      assertions.assertAll();
   }

   @CircuitBreaker
   private Object circuitBreaker;

   @Test
   public void testCircuitBreaker() {
      Map<String, String> commandProperties = scanToCommandProperties("circuitBreaker");

      SoftAssertions assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ENABLED, Boolean.TRUE.toString());
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE, String.valueOf(CircuitBreaker.DEFAULT_ERROR_PERCENTAGE));
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD, String.valueOf(CircuitBreaker.DEFAULT_REQUEST_VOLUME));
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS, String.valueOf(CircuitBreaker.DEFAULT_SLEEP_WINDOW));
      assertions.assertAll();
   }

   @CircuitBreaker(errorPercentage = CIRCUIT_BREAKER_ERROR_PERCENTAGE)
   private Object circuitBreakerErrorPercentage;

   @Test
   public void testCircuitBreakerErrorPercentage() {
      Map<String, String> commandProperties = scanToCommandProperties("circuitBreakerErrorPercentage");

      SoftAssertions assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ENABLED, Boolean.TRUE.toString());
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE, String.valueOf(CIRCUIT_BREAKER_ERROR_PERCENTAGE));
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD, String.valueOf(CircuitBreaker.DEFAULT_REQUEST_VOLUME));
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS, String.valueOf(CircuitBreaker.DEFAULT_SLEEP_WINDOW));
      assertions.assertAll();
   }

   @CircuitBreaker(requestVolume = CIRCUIT_BREAKER_REQUEST_VOLUME)
   private Object circuitBreakerRequestVolume;

   @Test
   public void testCircuitBreakerRequestVolume() {
      Map<String, String> commandProperties = scanToCommandProperties("circuitBreakerRequestVolume");

      SoftAssertions assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ENABLED, Boolean.TRUE.toString());
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE, String.valueOf(CircuitBreaker.DEFAULT_ERROR_PERCENTAGE));
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD, String.valueOf(CIRCUIT_BREAKER_REQUEST_VOLUME));
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS, String.valueOf(CircuitBreaker.DEFAULT_SLEEP_WINDOW));
      assertions.assertAll();
   }

   @CircuitBreaker(sleepWindow = CIRCUIT_BREAKER_SLEEP_WINDOW)
   private Object circuitBreakerSleepWindow;

   @Test
   public void testCircuitBreakerSleepWindow() {
      Map<String, String> commandProperties = scanToCommandProperties("circuitBreakerSleepWindow");

      SoftAssertions assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ENABLED, Boolean.TRUE.toString());
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_ERROR_THRESHOLD_PERCENTAGE, String.valueOf(CircuitBreaker.DEFAULT_ERROR_PERCENTAGE));
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_REQUEST_VOLUME_THRESHOLD, String.valueOf(CircuitBreaker.DEFAULT_REQUEST_VOLUME));
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.CIRCUIT_BREAKER_SLEEP_WINDOW_IN_MILLISECONDS, String.valueOf(CIRCUIT_BREAKER_SLEEP_WINDOW));
      assertions.assertAll();
   }

   @Fail(IllegalArgumentException.class)
   private Object ignoredException;

   @Test
   public void testIgnoredException() {
      MethodConfig methodConfig = scanToMethodConfig("ignoredException");

      Assertions.assertThat(methodConfig.getIgnoredExceptions()).containsOnly(IllegalArgumentException.class);
   }

   @Fail({ NullPointerException.class, IllegalArgumentException.class, IllegalStateException.class })
   private Object ignoredExceptions;

   @Test
   public void testIgnoredExceptions() {
      MethodConfig methodConfig = scanToMethodConfig("ignoredExceptions");

      Assertions.assertThat(methodConfig.getIgnoredExceptions()).containsOnly(NullPointerException.class, IllegalArgumentException.class, IllegalStateException.class);
   }

   @ThreadPool(THREAD_POOL_NAME)
   private Object threadPool;

   @Test
   public void testThreadPool() {
      MethodConfig methodConfig = scanToMethodConfig("threadPool");

      Assertions.assertThat(methodConfig.getThreadPoolKey()).isEqualTo(THREAD_POOL_NAME);
   }

   @Timeout
   private Object timeout;

   @Test
   public void testTimeout() {
      Map<String, String> commandProperties = scanToCommandProperties("timeout");

      SoftAssertions assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_TIMEOUT_ENABLED, Boolean.TRUE.toString());
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, String.valueOf(Timeout.DEFAULT_TIMEOUT));
      assertions.assertAll();
   }

   @Timeout(TIMEOUT_VALUE)
   private Object timeoutValue;

   @Test
   public void testTimeoutValue() {
      Map<String, String> commandProperties = scanToCommandProperties("timeoutValue");

      SoftAssertions assertions = new SoftAssertions();
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_TIMEOUT_ENABLED, Boolean.TRUE.toString());
      assertions.assertThat(commandProperties).containsEntry(CommandProperties.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS, String.valueOf(TIMEOUT_VALUE));
      assertions.assertAll();
   }

}
