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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.annotations.hystrix.basic.CircuitBreaker;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import javax.inject.Inject;

public class MultipleInjectionsIntegrationTest extends SilverWareHystrixTestBase {

   @Test
   public void testDifferentInjectionPointProxies() {
      NormallyResilientMicroservice normallyResilientMicroservice = lookupBean(NormallyResilientMicroservice.class);
      IndependentlyFailingMicroservice safelyFailingMicroservice = normallyResilientMicroservice.failingMicroservice;

      assertThatThrownBy(safelyFailingMicroservice::call)
            .isInstanceOf(HystrixRuntimeException.class)
            .hasRootCauseInstanceOf(FailingCallException.class);

      NeverResilientMicroservice neverResilientMicroservice = lookupBean(NeverResilientMicroservice.class);
      IndependentlyFailingMicroservice failingMicroservice = neverResilientMicroservice.failingMicroservice;

      assertThatThrownBy(failingMicroservice::call)
            .isInstanceOf(InvocationTargetException.class)
            .hasRootCauseInstanceOf(FailingCallException.class);
   }

   @Test
   public void testIndependentHystrixConfiguration() throws Exception {
      InstantlyResilientMicroservice instantlyResilientMicroservice = lookupBean(InstantlyResilientMicroservice.class);
      IndependentlyFailingMicroservice instantlyFailingMicroservice = instantlyResilientMicroservice.failingMicroservice;

      assertThatThrownBy(instantlyFailingMicroservice::call)
            .isInstanceOf(HystrixRuntimeException.class)
            .hasRootCauseInstanceOf(FailingCallException.class);

      // waiting for the circuit breaker to be tripped
      Thread.sleep(500);

      assertThatThrownBy(instantlyFailingMicroservice::call)
            .isInstanceOf(HystrixRuntimeException.class)
            .hasMessageContaining("short-circuited");

      NormallyResilientMicroservice normallyResilientMicroservice = lookupBean(NormallyResilientMicroservice.class);
      IndependentlyFailingMicroservice failingMicroservice = normallyResilientMicroservice.failingMicroservice;

      assertThatThrownBy(failingMicroservice::call)
            .isInstanceOf(HystrixRuntimeException.class)
            .hasRootCauseInstanceOf(FailingCallException.class);
   }

   @Microservice
   public static class NormallyResilientMicroservice {

      @Inject
      @CircuitBreaker
      @MicroserviceReference
      IndependentlyFailingMicroservice failingMicroservice;

   }

   @Microservice
   public static class InstantlyResilientMicroservice {

      @Inject
      @CircuitBreaker(requestVolume = 1)
      @MicroserviceReference
      IndependentlyFailingMicroservice failingMicroservice;

   }

   @Microservice
   public static class NeverResilientMicroservice {

      @Inject
      @MicroserviceReference
      IndependentlyFailingMicroservice failingMicroservice;

   }

   @Microservice
   public static class IndependentlyFailingMicroservice {

      public void call() {
         throw new FailingCallException();
      }

   }

}
