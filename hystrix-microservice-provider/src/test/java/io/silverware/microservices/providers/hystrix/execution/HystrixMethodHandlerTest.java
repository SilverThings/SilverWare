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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.silverware.microservices.providers.cdi.internal.MicroserviceMethodHandler;
import io.silverware.microservices.providers.cdi.internal.MicroserviceProxyBean;
import io.silverware.microservices.providers.hystrix.configuration.CommandProperties;
import io.silverware.microservices.providers.hystrix.configuration.MethodConfig;
import io.silverware.microservices.providers.hystrix.configuration.ServiceConfig;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

public class HystrixMethodHandlerTest extends HystrixTestBase {

   private static final String SERVICE_NAME = "TestingMicroservice";
   private static final String BEAN_NAME = "TestingBean";
   private static final String FIELD_NAME = "testingField";

   private MicroserviceProxyBean microserviceProxyBean;
   private MicroserviceMethodHandler parentMethodHandler;

   @BeforeMethod
   public void setUpMethodHandlers() {
      microserviceProxyBean = Mockito.mock(MicroserviceProxyBean.class);
      Mockito.when(microserviceProxyBean.getMicroserviceName()).thenReturn(SERVICE_NAME);

      parentMethodHandler = Mockito.mock(MicroserviceMethodHandler.class);
      Mockito.when(parentMethodHandler.getProxyBean()).thenReturn(microserviceProxyBean);

      InjectionPoint injectionPoint = Mockito.mock(InjectionPoint.class);
      Mockito.when(parentMethodHandler.getInjectionPoint()).thenReturn(injectionPoint);

      Bean bean = Mockito.mock(Bean.class);
      Mockito.when(injectionPoint.getBean()).thenReturn(bean);
      Mockito.when(bean.getName()).thenReturn(BEAN_NAME);

      Member member = Mockito.mock(Member.class);
      Mockito.when(injectionPoint.getMember()).thenReturn(member);
      Mockito.when(member.getName()).thenReturn(FIELD_NAME);
   }

   @Test
   public void testGetProxyBean() {
      HystrixMethodHandler hystrixMethodHandler = new HystrixMethodHandler(parentMethodHandler, null);
      Assertions.assertThat(hystrixMethodHandler.getProxyBean()).isEqualTo(microserviceProxyBean);
   }

   @Test
   public void testInvokeDefault() throws Exception {
      MethodConfig methodConfig = MethodConfig.createBuilder(SERVICE_NAME, BEAN_NAME + ":" + FIELD_NAME).build();
      ServiceConfig serviceConfig = new ServiceConfig(methodConfig);
      HystrixMethodHandler hystrixMethodHandler = new HystrixMethodHandler(parentMethodHandler, serviceConfig);

      Method method = UnstableMicroservice.class.getMethod("fail");
      Mockito.when(parentMethodHandler.invoke(method)).thenThrow(new FailingCallException());

      assertThatThrownBy(() -> hystrixMethodHandler.invoke(method))
            .isInstanceOf(FailingCallException.class);
   }

   @Test
   public void testInvokeObjectMethod() throws Exception {
      MethodConfig methodConfig = MethodConfig.createBuilder(SERVICE_NAME, BEAN_NAME + ":" + FIELD_NAME)
                                              .hystrixActive(true)
                                              .build();
      ServiceConfig serviceConfig = new ServiceConfig(methodConfig);
      HystrixMethodHandler hystrixMethodHandler = new HystrixMethodHandler(parentMethodHandler, serviceConfig);

      Method method = UnstableMicroservice.class.getMethod("toString");
      Mockito.when(parentMethodHandler.invoke(method)).thenThrow(new FailingCallException());

      assertThatThrownBy(() -> hystrixMethodHandler.invoke(method))
            .isInstanceOf(FailingCallException.class);
   }

   @Test
   public void testInvokeCircuitBreaker() throws Exception {
      MethodConfig methodConfig = MethodConfig.createBuilder(SERVICE_NAME, BEAN_NAME + ":" + FIELD_NAME)
                                              .hystrixActive(true)
                                              .commandProperty(CommandProperties.CIRCUIT_BREAKER_ENABLED, Boolean.TRUE.toString())
                                              .build();
      ServiceConfig serviceConfig = new ServiceConfig(methodConfig);
      HystrixMethodHandler hystrixMethodHandler = new HystrixMethodHandler(parentMethodHandler, serviceConfig);

      Method method = UnstableMicroservice.class.getMethod("fail");
      Mockito.when(parentMethodHandler.invoke(method)).thenThrow(new FailingCallException());

      assertThatThrownBy(() -> hystrixMethodHandler.invoke(method))
            .isInstanceOf(HystrixRuntimeException.class)
            .hasMessageContaining("failed");
   }

   @Test
   public void testInvokeTimeout() throws Exception {
      MethodConfig methodConfig = MethodConfig.createBuilder(SERVICE_NAME, BEAN_NAME + ":" + FIELD_NAME)
                                              .hystrixActive(true)
                                              .commandProperty(CommandProperties.EXECUTION_TIMEOUT_ENABLED, Boolean.TRUE.toString())
                                              .build();
      ServiceConfig serviceConfig = new ServiceConfig(methodConfig);
      HystrixMethodHandler hystrixMethodHandler = new HystrixMethodHandler(parentMethodHandler, serviceConfig);

      Method method = UnstableMicroservice.class.getMethod("timeout");
      Mockito.when(parentMethodHandler.invoke(method)).thenAnswer(a -> method.invoke(UnstableMicroservice.class));

      assertThatThrownBy(() -> hystrixMethodHandler.invoke(method))
            .isInstanceOf(HystrixRuntimeException.class)
            .hasMessageContaining("timed-out");
   }

   private void testRequestCaching(boolean enabled) throws Exception {
      MethodConfig methodConfig = MethodConfig.createBuilder(SERVICE_NAME, BEAN_NAME + ":" + FIELD_NAME)
                                              .hystrixActive(true)
                                              .commandProperty(CommandProperties.REQUEST_CACHE_ENABLED, String.valueOf(enabled))
                                              .build();
      ServiceConfig serviceConfig = new ServiceConfig(methodConfig);
      HystrixMethodHandler hystrixMethodHandler = new HystrixMethodHandler(parentMethodHandler, serviceConfig);

      Method method = UnstableMicroservice.class.getMethod("cache", int.class);
      Mockito.when(parentMethodHandler.invoke(Mockito.any(), Mockito.any()))
             .thenAnswer(answer -> method.invoke(UnstableMicroservice.class, (Integer) answer.getArgument(1)));

      hystrixMethodHandler.invoke(method, 1);
      hystrixMethodHandler.invoke(method, 2);
      hystrixMethodHandler.invoke(method, 1);

      Mockito.verify(parentMethodHandler, Mockito.times(enabled ? 1 : 2)).invoke(method, 1);
      Mockito.verify(parentMethodHandler, Mockito.times(1)).invoke(method, 2);
   }

   @Test
   public void testInvokeRequestCachingEnabled() throws Exception {
      testRequestCaching(true);
   }

   @Test
   public void testInvokeRequestCachingDisabled() throws Exception {
      testRequestCaching(false);
   }

   @Test
   public void testInvokeRequestCachingParameters() throws Exception {
      MethodConfig methodConfig = MethodConfig.createBuilder(SERVICE_NAME, BEAN_NAME + ":" + FIELD_NAME)
                                              .hystrixActive(true)
                                              .cacheKeyParameterIndex(1)
                                              .commandProperty(CommandProperties.REQUEST_CACHE_ENABLED, Boolean.TRUE.toString())
                                              .build();
      ServiceConfig serviceConfig = new ServiceConfig(methodConfig);
      HystrixMethodHandler hystrixMethodHandler = new HystrixMethodHandler(parentMethodHandler, serviceConfig);

      Method method = UnstableMicroservice.class.getMethod("add", int.class, int.class);
      Mockito.when(parentMethodHandler.invoke(Mockito.any(), Mockito.any(), Mockito.any()))
             .thenAnswer(answer -> method.invoke(UnstableMicroservice.class, (Integer) answer.getArgument(1), (Integer) answer.getArgument(2)));

      hystrixMethodHandler.invoke(method, 1, 1);
      hystrixMethodHandler.invoke(method, 1, 2);
      hystrixMethodHandler.invoke(method, 2, 1);

      Mockito.verify(parentMethodHandler, Mockito.times(1)).invoke(method, 1, 1);
      Mockito.verify(parentMethodHandler, Mockito.times(1)).invoke(method, 1, 2);
      Mockito.verify(parentMethodHandler, Mockito.times(0)).invoke(method, 2, 1);
   }

   private static class UnstableMicroservice {

      public static int add(int number1, int number2) {
         return number1 + number2;
      }

      public static int cache(int number) {
         return number;
      }

      public static void fail() {
         throw new FailingCallException();
      }

      public static Object timeout() {
         try {
            Thread.sleep(1500);
         } catch (InterruptedException ex) {
            // ignore
         }
         throw new FailingCallException();
      }

   }

}
