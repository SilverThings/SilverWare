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
import io.silverware.microservices.annotations.hystrix.HystrixConfig;
import io.silverware.microservices.annotations.hystrix.advanced.HystrixCommand;
import io.silverware.microservices.annotations.hystrix.advanced.HystrixProperty;
import io.silverware.microservices.annotations.hystrix.basic.Timeout;
import io.silverware.microservices.providers.hystrix.configuration.CommandProperties;

import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandMetrics;
import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;

public class AdvancedHystrixIntegrationTest extends SilverWareHystrixTestBase {

   private static final String GROUP = "advancedTestingGroup";
   private static final String COMMAND = "advancedTestingCommand";
   private static final String THREAD_POOL = "advancedTestingThreadPool";

   private static AtomicReference<String> threadName = new AtomicReference<>();

   private AdvancedFailingMicroservice failingMicroservice;

   @BeforeClass
   public void lookupResilientMicroservice() {
      AdvancedResilientMicroservice resilientMicroservice = lookupBean(AdvancedResilientMicroservice.class);
      failingMicroservice = resilientMicroservice.failingMicroservice;
   }

   @Test
   public void testEmptyConfiguration() {
      assertThatThrownBy(() -> failingMicroservice.none())
            .hasCauseExactlyInstanceOf(FailingCallException.class);
   }

   @Test
   public void testCircuitBreaker() {
      assertThatThrownBy(() -> failingMicroservice.circuitBreaker())
            .isInstanceOf(HystrixRuntimeException.class)
            .hasRootCauseInstanceOf(FailingCallException.class);
   }

   @Test
   public void testCircuitBreakerWithFallback() {
      assertThat(failingMicroservice.circuitBreakerWithFallback()).as("Fallback should have been executed").isTrue();
   }

   @Test
   public void testTimeout() {
      assertThatThrownBy(() -> failingMicroservice.timeout())
            .isInstanceOf(HystrixRuntimeException.class)
            .hasRootCauseInstanceOf(TimeoutException.class);
   }

   @Test
   public void testTimeoutWithFallback() throws Exception {
      assertThat(failingMicroservice.timeoutWithFallback()).as("Fallback should have been executed").isTrue();
   }

   @Test
   public void testGroupKey() {
      HystrixCommandKey commandKey = HystrixCommandKey.Factory.asKey("advancedResilientMicroservice:failingMicroservice:groupKey");

      HystrixCommandMetrics commandMetrics = HystrixCommandMetrics.getInstance(commandKey);
      assertThat(commandMetrics).isNull();

      failingMicroservice.groupKey();

      commandMetrics = HystrixCommandMetrics.getInstance(commandKey);
      assertThat(commandMetrics).isNotNull();
      assertThat(commandMetrics.getCommandGroup().name()).isEqualTo(GROUP);
   }

   @Test
   public void testCommandKey() {
      HystrixCommandKey commandKey = HystrixCommandKey.Factory.asKey(COMMAND);

      HystrixCommandMetrics commandMetrics = HystrixCommandMetrics.getInstance(commandKey);
      assertThat(commandMetrics).isNull();

      failingMicroservice.commandKey();

      commandMetrics = HystrixCommandMetrics.getInstance(commandKey);
      assertThat(commandMetrics).isNotNull();
      assertThat(commandMetrics.getCommandKey().name()).isEqualTo(COMMAND);
   }

   @Test
   public void testThreadPoolKey() {
      threadName.set("");
      failingMicroservice.threadPoolKey();
      assertThat(threadName.get()).containsIgnoringCase(THREAD_POOL);
   }

   @Microservice
   public static class AdvancedResilientMicroservice {

      @Inject
      @MicroserviceReference
      @HystrixConfig(AdvancedFailingMicroserviceConfiguration.class)
      private AdvancedFailingMicroservice failingMicroservice;

   }

   public interface AdvancedFailingMicroservice {

      void none();

      void circuitBreaker();

      boolean circuitBreakerWithFallback();

      void timeout() throws Exception;

      boolean timeoutWithFallback() throws Exception;

      void groupKey();

      void commandKey();

      void threadPoolKey();

   }

   @Microservice
   public static class AdvancedFailingMicroserviceImpl implements AdvancedFailingMicroservice {

      @Override
      public void none() {
         throw new FailingCallException();
      }

      @Override
      public void circuitBreaker() {
         throw new FailingCallException();
      }

      @Override
      public boolean circuitBreakerWithFallback() {
         throw new FailingCallException();
      }

      @Override
      public void timeout() throws Exception {
         Thread.sleep(Timeout.DEFAULT_TIMEOUT + 100);
      }

      @Override
      public boolean timeoutWithFallback() throws Exception {
         Thread.sleep(Timeout.DEFAULT_TIMEOUT + 100);
         return false;
      }

      @Override
      public void groupKey() {

      }

      @Override
      public void commandKey() {

      }

      @Override
      public void threadPoolKey() {
         threadName.set(Thread.currentThread().getName());
      }
   }

   public interface AdvancedFailingMicroserviceConfiguration extends AdvancedFailingMicroservice {

      @HystrixCommand(
            commandProperties = @HystrixProperty(name = CommandProperties.CIRCUIT_BREAKER_ENABLED, value = "true")
      )
      void circuitBreaker();

      @HystrixCommand(
            commandProperties = @HystrixProperty(name = CommandProperties.CIRCUIT_BREAKER_ENABLED, value = "true")
      )
      boolean circuitBreakerWithFallback();

      @HystrixCommand(
            commandProperties = @HystrixProperty(name = CommandProperties.EXECUTION_TIMEOUT_ENABLED, value = "true")
      )
      void timeout() throws Exception;

      @HystrixCommand(
            commandProperties = @HystrixProperty(name = CommandProperties.EXECUTION_TIMEOUT_ENABLED, value = "true")
      )
      boolean timeoutWithFallback() throws Exception;

      @HystrixCommand(
            groupKey = GROUP
      )
      void groupKey();

      @HystrixCommand(
            commandKey = COMMAND
      )
      void commandKey();

      @HystrixCommand(
            threadPoolKey = THREAD_POOL
      )
      void threadPoolKey();
   }

   @Fallback
   @Microservice
   public static class AdvancedFailingMicroserviceFallback implements AdvancedFailingMicroservice {

      @Override
      public void none() {
         throw new UnsupportedOperationException();
      }

      @Override
      public void circuitBreaker() {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean circuitBreakerWithFallback() {
         return true;
      }

      @Override
      public void timeout() throws Exception {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean timeoutWithFallback() throws Exception {
         return true;
      }

      @Override
      public void groupKey() {
         throw new UnsupportedOperationException();
      }

      @Override
      public void commandKey() {
         throw new UnsupportedOperationException();
      }

      @Override
      public void threadPoolKey() {
         throw new UnsupportedOperationException();
      }
   }

}
