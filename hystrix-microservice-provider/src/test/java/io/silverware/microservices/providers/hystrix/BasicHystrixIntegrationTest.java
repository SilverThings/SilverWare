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
package io.silverware.microservices.providers.hystrix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.annotations.hystrix.Fallback;
import io.silverware.microservices.annotations.hystrix.basic.Cached;
import io.silverware.microservices.annotations.hystrix.basic.CircuitBreaker;
import io.silverware.microservices.annotations.hystrix.basic.Fail;
import io.silverware.microservices.annotations.hystrix.basic.ThreadPool;
import io.silverware.microservices.annotations.hystrix.basic.Timeout;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;

public class BasicHystrixIntegrationTest extends SilverWareHystrixTestBase {

   private static final int TIMEOUT = 200;
   private static final String THREAD_POOL = "basicTestingThreadPool";

   private static AtomicBoolean cached = new AtomicBoolean(true);
   private static AtomicReference<String> threadName = new AtomicReference<>();

   private ResilientMicroservice resilientMicroservice;

   @BeforeClass
   public void lookupResilientMicroservice() {
      resilientMicroservice = lookupBean(ResilientMicroservice.class);
   }

   @Test
   public void testCircuitBreaker() throws Exception {
      CircuitBreakerMicroservice circuitBreakerMicroservice = resilientMicroservice.circuitBreakerMicroservice;

      assertThatThrownBy(circuitBreakerMicroservice::call)
            .isInstanceOf(HystrixRuntimeException.class)
            .hasRootCauseInstanceOf(FailingCallException.class);
   }

   @Test
   public void testTimeout() throws Exception {
      TimeoutMicroservice timeoutMicroservice = resilientMicroservice.timeoutMicroservice;

      assertThatThrownBy(timeoutMicroservice::call)
            .isInstanceOf(HystrixRuntimeException.class)
            .hasCauseInstanceOf(TimeoutException.class);
   }

   @Test
   public void testFail() throws Exception {
      FailMicroservice failMicroservice = resilientMicroservice.failMicroservice;

      assertThatThrownBy(failMicroservice::call)
            .isInstanceOf(FailingCallException.class);
   }

   @Test
   public void testFallbackWithCircuitBreaker() throws Exception {
      FallbackMicroservice fallbackMicroservice = resilientMicroservice.fallbackMicroservice;

      boolean fallbackExecuted = fallbackMicroservice.call();
      assertThat(fallbackExecuted).as("Fallback was not executed").isTrue();
   }

   @Test
   public void testFallbackWithTimeout() throws Exception {
      TimeoutFallbackMicroservice timeoutFallbackMicroservice = resilientMicroservice.timeoutFallbackMicroservice;

      boolean fallbackExecuted = timeoutFallbackMicroservice.call();
      assertThat(fallbackExecuted).as("Fallback was not executed").isTrue();
   }

   @Test
   public void testThreadPool() throws Exception {
      ThreadPoolMicroservice threadPoolMicroservice = resilientMicroservice.threadPoolMicroservice;

      threadName.set("");
      threadPoolMicroservice.call();
      assertThat(threadName.get()).containsIgnoringCase(THREAD_POOL);
   }

   @Test
   public void testThreadPoolDefault() throws Exception {
      ThreadPoolDefaultMicroservice threadPoolMicroservice = resilientMicroservice.threadPoolDefaultMicroservice;

      threadName.set("");
      threadPoolMicroservice.call();
      assertThat(threadName.get()).containsIgnoringCase(ThreadPoolDefaultMicroservice.class.getSimpleName());
   }

   @Test
   public void testCached() throws Exception {
      CachedMicroservice cachedMicroservice = resilientMicroservice.cachedMicroservice;

      cached.set(true);
      int result = cachedMicroservice.call(1);
      assertThat(result).isEqualTo(1);
      assertThat(cached.get()).isFalse();

      cached.set(true);
      result = cachedMicroservice.call(2);
      assertThat(result).isEqualTo(2);
      assertThat(cached.get()).isFalse();

      cached.set(true);
      result = cachedMicroservice.call(1);
      assertThat(result).isEqualTo(1);
      assertThat(cached.get()).isTrue();
   }

   @Microservice
   public static class ResilientMicroservice {

      @Inject
      @CircuitBreaker
      @MicroserviceReference
      private CircuitBreakerMicroservice circuitBreakerMicroservice;

      @Inject
      @Timeout
      @MicroserviceReference
      private TimeoutMicroservice timeoutMicroservice;

      @Inject
      @CircuitBreaker
      @MicroserviceReference
      private FallbackMicroservice fallbackMicroservice;

      @Inject
      @Timeout(TIMEOUT)
      @MicroserviceReference
      private TimeoutFallbackMicroservice timeoutFallbackMicroservice;

      @Inject
      @Fail(FailingCallException.class)
      @CircuitBreaker
      @MicroserviceReference
      private FailMicroservice failMicroservice;

      @Inject
      @Cached
      @MicroserviceReference
      private CachedMicroservice cachedMicroservice;

      @Inject
      @ThreadPool(THREAD_POOL)
      @MicroserviceReference
      private ThreadPoolMicroservice threadPoolMicroservice;

      @Inject
      @CircuitBreaker
      @MicroserviceReference
      private ThreadPoolDefaultMicroservice threadPoolDefaultMicroservice;

   }

   @Microservice
   public static class CircuitBreakerMicroservice {

      public void call() {
         throw new FailingCallException();
      }
   }

   @Microservice
   public static class TimeoutMicroservice {

      public void call() throws Exception {
         Thread.sleep(Timeout.DEFAULT_TIMEOUT + 100);
      }
   }

   public interface FallbackMicroservice {

      boolean call();

   }

   @Microservice
   public static class PrimaryFallbackMicroservice implements FallbackMicroservice {

      @Override
      public boolean call() {
         throw new FailingCallException();
      }
   }

   @Fallback
   @Microservice
   public static class FallbackFallbackMicroservice implements FallbackMicroservice {

      @Override
      public boolean call() {
         return true;
      }
   }

   public interface TimeoutFallbackMicroservice {

      boolean call() throws Exception;

   }

   @Microservice
   public static class PrimaryTimeoutFallbackMicroservice implements TimeoutFallbackMicroservice {

      @Override
      public boolean call() throws Exception {
         Thread.sleep(TIMEOUT + 100);
         return false;
      }
   }

   @Fallback
   @Microservice
   public static class FallbackTimeoutFallbackMicroservice implements TimeoutFallbackMicroservice {

      @Override
      public boolean call() {
         return true;
      }
   }

   @Microservice
   public static class FailMicroservice {

      public void call() {
         throw new FailingCallException();
      }
   }

   @Microservice
   public static class CachedMicroservice {

      public int call(int number) {
         cached.set(false);
         return number;
      }
   }

   @Microservice
   public static class ThreadPoolMicroservice {

      public void call() {
         threadName.set(Thread.currentThread().getName());
      }

   }

   @Microservice
   public static class ThreadPoolDefaultMicroservice {

      public void call() {
         threadName.set(Thread.currentThread().getName());
      }

   }

}
