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
package io.silverware.microservices.providers.hystrix.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.exception.HystrixBadRequestException;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;

public class MicroserviceHystrixCommandTest extends HystrixTestBase {

   private static final Logger log = LogManager.getLogger(MicroserviceHystrixCommandTest.class);

   private static final HystrixCommandGroupKey GROUP_KEY = HystrixCommandGroupKey.Factory.asKey("TestingGroup");
   private static final int ERROR_THRESHOLD_PERCENTAGE = 50;
   private static final int REQUEST_VOLUME_THRESHOLD = 5;
   private static final int SLEEP_WINDOW_IN_MILLISECONDS = 800;

   private boolean circuitClosed = false;

   @Test
   public void testPrimaryCall() {
      Boolean result = executeFailingCommand(false);
      assertThat(result).as("Callable has not been executed.").isNotNull().isTrue();
   }

   @Test
   public void testFallback() {
      Boolean result = executeFailingCommand(true);
      assertThat(result).as("Fallback has not been executed.").isNotNull().isFalse();
   }

   @Test
   public void testOpenAndCloseCircuit() throws InterruptedException {
      for (int i = 0; i < REQUEST_VOLUME_THRESHOLD; i++) {
         log.debug(i + ": circuit closed");
         executeFailingCommand(true);
         assertCircuitClosed();
         Thread.sleep(200);
      }

      for (int i = 0; i < REQUEST_VOLUME_THRESHOLD; i++) {
         log.debug(i + ": circuit open");
         executeFailingCommand(false);
         assertCircuitOpen();
      }

      Thread.sleep(SLEEP_WINDOW_IN_MILLISECONDS);
      executeFailingCommand(false);
      assertCircuitClosed();
   }

   @Test
   public void testRequestCaching() {
      HystrixCommand<Integer> command;

      command = createCachedCommand(1);
      assertThat(command.execute()).isEqualTo(1);
      assertThat(command.isResponseFromCache()).isFalse();

      command = createCachedCommand(2);
      assertThat(command.execute()).isEqualTo(2);
      assertThat(command.isResponseFromCache()).isFalse();

      command = createCachedCommand(1);
      assertThat(command.execute()).isEqualTo(1);
      assertThat(command.isResponseFromCache()).isTrue();
   }

   @Test
   public void testIgnoredExceptions() {
      assertThatThrownBy(() -> executeFailingCommand(true, Collections.singleton(FailingCallException.class)))
            .isInstanceOf(HystrixBadRequestException.class)
            .hasCauseInstanceOf(FailingCallException.class);
   }

   @Test
   public void testIgnoredExceptionsSuperType() {
      assertThatThrownBy(() -> executeFailingCommand(true, Collections.singleton(RuntimeException.class)))
            .isInstanceOf(HystrixBadRequestException.class)
            .hasCauseInstanceOf(FailingCallException.class);
   }

   @Test
   public void testIgnoredExceptionsOther() {
      boolean result = executeFailingCommand(true, Collections.singleton(IllegalArgumentException.class));
      assertThat(result).as("Exception should have been thrown and caught").isFalse();
   }

   private Boolean executeFailingCommand(boolean fail) {
      return executeFailingCommand(fail, Collections.emptySet());
   }

   private Boolean executeFailingCommand(boolean fail, Set<Class<? extends Throwable>> ignoredExceptions) {
      circuitClosed = false;

      HystrixCommandProperties.Setter commandProperties = HystrixCommandProperties.Setter();
      commandProperties.withCircuitBreakerEnabled(true)
                       .withCircuitBreakerErrorThresholdPercentage(ERROR_THRESHOLD_PERCENTAGE)
                       .withCircuitBreakerRequestVolumeThreshold(REQUEST_VOLUME_THRESHOLD)
                       .withCircuitBreakerSleepWindowInMilliseconds(SLEEP_WINDOW_IN_MILLISECONDS);

      HystrixCommand.Setter setter = Setter.withGroupKey(GROUP_KEY)
                                           .andCommandPropertiesDefaults(commandProperties);

      Callable<Boolean> callable = () -> {
         circuitClosed = true;
         if (fail) {
            throw new FailingCallException();
         } else {
            return true;
         }
      };
      Callable<Boolean> fallback = () -> false;
      MicroserviceHystrixCommand<Boolean> command = new MicroserviceHystrixCommand.Builder<>(setter, callable)
            .fallback(fallback)
            .ignoredExceptions(ignoredExceptions)
            .build();

      return command.execute();
   }

   private HystrixCommand<Integer> createCachedCommand(int number) {
      HystrixCommandProperties.Setter commandProperties = HystrixCommandProperties.Setter();
      commandProperties.withRequestCacheEnabled(true);

      HystrixCommand.Setter setter = Setter.withGroupKey(GROUP_KEY)
                                           .andCommandPropertiesDefaults(commandProperties);

      Callable<Integer> callable = () -> number;
      Callable<Integer> fallback = () -> 0;

      return new MicroserviceHystrixCommand.Builder<>(setter, callable)
            .fallback(fallback)
            .cacheKey(String.valueOf(number))
            .build();
   }

   private void assertCircuitClosed() {
      assertThat(circuitClosed).as("Circuit should have been closed").isTrue();
   }

   private void assertCircuitOpen() {
      assertThat(circuitClosed).as("Circuit should have been open").isFalse();
   }

}
