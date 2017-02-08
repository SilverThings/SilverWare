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

import org.assertj.core.api.Assertions;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

abstract class AnnotationScannerTestBase {

   protected static final String METHOD_1 = "method1";
   protected static final String METHOD_2 = "method2";
   protected static final String METHOD_3 = "method3";
   protected static final String METHOD_4 = "method4";

   protected static final String MICROSERVICE_NAME = "TestingMicroservice";
   protected static final String BEAN_NAME = "TestingBean";
   protected static final String FIELD_NAME = "testingField";

   private Annotated annotated;
   private InjectionPoint injectionPoint;

   @BeforeMethod
   public void createInjectionPoint() {
      annotated = Mockito.mock(Annotated.class);

      injectionPoint = Mockito.mock(InjectionPoint.class);
      Mockito.when(injectionPoint.getAnnotated()).thenReturn(annotated);

      Bean bean = Mockito.mock(Bean.class);
      Mockito.when(injectionPoint.getBean()).thenReturn(bean);
      Mockito.when(bean.getName()).thenReturn(BEAN_NAME);

      Member member = Mockito.mock(Member.class);
      Mockito.when(injectionPoint.getMember()).thenReturn(member);
      Mockito.when(member.getName()).thenReturn(FIELD_NAME);
   }

   protected Map<String, String> scanToCommandProperties(String fieldName) {
      MethodConfig methodConfig = scanToMethodConfig(fieldName);

      Map<String, String> commandProperties = methodConfig.getCommandProperties();
      Assertions.assertThat(commandProperties).isNotNull();

      return commandProperties;
   }

   protected MethodConfig scanToMethodConfig(String fieldName) {
      ServiceConfig serviceConfig = scanToServiceConfig(fieldName);

      MethodConfig methodConfig = serviceConfig.getDefaultConfig();
      Assertions.assertThat(methodConfig).isNotNull();

      return methodConfig;
   }

   protected ServiceConfig scanToServiceConfig(String fieldName) {
      Set<Annotation> annotations = getFieldAnnotations(fieldName);
      Mockito.when(annotated.getAnnotations()).thenReturn(annotations);
      return AnnotationScanner.scan(injectionPoint, MICROSERVICE_NAME);
   }

   private Set<Annotation> getFieldAnnotations(String fieldName) {
      try {
         Annotation[] annotations = getClass().getDeclaredField(fieldName).getAnnotations();
         return new HashSet<>(Arrays.asList(annotations));
      } catch (NoSuchFieldException ex) {
         throw new RuntimeException(ex);
      }
   }

   protected interface RemoteMicroservice {

      void method1(Object param);

      String method2();

      int method3(long param);

      long method4(long param1, long param2);

   }

   protected static MethodConfig getMethodConfig(ServiceConfig serviceConfig, String methodName) {
      Method method = getMethod(methodName);
      return serviceConfig.getMethodConfig(method);
   }

   private static Method getMethod(String methodName) {
      switch (methodName) {
         case METHOD_1:
            return getMethod(methodName, Object.class);
         case METHOD_2:
            return getMethod(methodName, new Class<?>[] {});
         case METHOD_3:
            return getMethod(methodName, long.class);
         case METHOD_4:
            return getMethod(methodName, long.class, long.class);
         default:
            throw new IllegalArgumentException("Unknown method name: " + methodName);
      }
   }

   private static Method getMethod(String name, Class<?>... parameterTypes) {
      try {
         return RemoteMicroservice.class.getMethod(name, parameterTypes);
      } catch (NoSuchMethodException ex) {
         throw new RuntimeException(ex);
      }
   }

   protected static String createCommandKey(String methodName) {
      return BEAN_NAME + ":" + FIELD_NAME + ":" + methodName;
   }

}
