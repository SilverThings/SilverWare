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

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Injection point wrapper that makes qualifiers modifiable.
 */
public class MicroserviceInjectionPoint implements InjectionPoint {

   private final InjectionPoint injectionPoint;
   private final Set<Annotation> qualifiers;

   /**
    * Wraps given injection point and puts its qualifiers to modifiable collection.
    *
    * @param injectionPoint
    *       wrapped injection point
    */
   public MicroserviceInjectionPoint(final InjectionPoint injectionPoint) {
      this.injectionPoint = injectionPoint;
      this.qualifiers = new HashSet<>(injectionPoint.getQualifiers());
   }

   @Override
   public Type getType() {
      return injectionPoint.getType();
   }

   @Override
   public Set<Annotation> getQualifiers() {
      return qualifiers;
   }

   @Override
   public Bean<?> getBean() {
      return injectionPoint.getBean();
   }

   @Override
   public Member getMember() {
      return injectionPoint.getMember();
   }

   @Override
   public Annotated getAnnotated() {
      return injectionPoint.getAnnotated();
   }

   @Override
   public boolean isDelegate() {
      return injectionPoint.isDelegate();
   }

   @Override
   public boolean isTransient() {
      return injectionPoint.isTransient();
   }
}
