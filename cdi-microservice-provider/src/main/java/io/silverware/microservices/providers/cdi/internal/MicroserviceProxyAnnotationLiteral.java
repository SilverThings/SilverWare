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
package io.silverware.microservices.providers.cdi.internal;

import io.silverware.microservices.annotations.MicroserviceProxy;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;

/**
 * Implementation of {@link io.silverware.microservices.annotations.MicroserviceProxy} for given {@link javax.enterprise.inject.spi.InjectionPoint}.
 */
public class MicroserviceProxyAnnotationLiteral extends AnnotationLiteral<MicroserviceProxy> implements MicroserviceProxy {

   private final String beanName;
   private final String fieldName;

   public MicroserviceProxyAnnotationLiteral(final InjectionPoint injectionPoint) {
      this.beanName = injectionPoint.getBean().getName();
      this.fieldName = injectionPoint.getMember().getName();
   }

   @Override
   public String beanName() {
      return beanName;
   }

   @Override
   public String fieldName() {
      return fieldName;
   }
}
