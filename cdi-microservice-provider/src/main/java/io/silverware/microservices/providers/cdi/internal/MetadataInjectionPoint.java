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

import org.jboss.weld.literal.DefaultLiteral;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * A "synthetic" injection point used to obtain InjectionPoint metadata.
 */
class MetadataInjectionPoint implements InjectionPoint {

   private final Bean<?> bean;

   MetadataInjectionPoint(Bean<?> bean) {
      this.bean = bean;
   }

   @Override
   public Type getType() {
      return InjectionPoint.class;
   }

   @Override
   public Set<Annotation> getQualifiers() {
      return Collections.singleton(DefaultLiteral.INSTANCE);
   }

   @Override
   public Bean<?> getBean() {
      return bean;
   }

   @Override
   public Member getMember() {
      return null;
   }

   @Override
   public Annotated getAnnotated() {
      // Dummy annotated needed for validation
      return new AnnotatedField() {

         @Override
         public boolean isStatic() {
            return false;
         }

         @Override
         public AnnotatedType getDeclaringType() {
            return null;
         }

         @Override
         public Type getBaseType() {
            return null;
         }

         @Override
         public Set<Type> getTypeClosure() {
            return null;
         }

         @Override
         public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            return null;
         }

         @Override
         public Set<Annotation> getAnnotations() {
            return Collections.emptySet();
         }

         @Override
         public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return false;
         }

         @Override
         public Field getJavaMember() {
            return null;
         }
      };
   }

   @Override
   public boolean isDelegate() {
      return false;
   }

   @Override
   public boolean isTransient() {
      return false;
   }

}
