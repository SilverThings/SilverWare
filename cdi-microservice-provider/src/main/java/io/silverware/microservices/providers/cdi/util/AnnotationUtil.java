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
package io.silverware.microservices.providers.cdi.util;

import java.lang.annotation.Annotation;
import java.util.Optional;
import java.util.Set;

/**
 * Helps to work with annotation types.
 */
public class AnnotationUtil {

   private AnnotationUtil() {
   }

   public static boolean matches(final Annotation annotation, final Class<? extends Annotation> annotationType) {
      return annotation.annotationType().isAssignableFrom(annotationType);
   }

   public static boolean containsAnnotation(final Set<Annotation> annotations, final Class<? extends Annotation> annotationType) {
      return annotations.stream().anyMatch(annotation -> matches(annotation, annotationType));
   }

   public static <T extends Annotation> Optional<T> findAnnotation(final Set<Annotation> annotations, final Class<T> annotationType) {
      return annotations.stream().filter(annotation -> matches(annotation, annotationType)).findFirst().map(annotationType::cast);
   }

}
