/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2010 - 2013 the original author or authors.
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
package org.silverware.microservices.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.silverware.microservices.Microservice;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class DeploymentScanner {

   private static final Logger log = LogManager.getLogger(DeploymentScanner.class);

   private static final Reflections reflections = new Reflections();

   public static Set<Class<? extends Microservice>> lookupMicroservices() {
      return reflections.getSubTypesOf(Microservice.class);
   }

   @SuppressWarnings("unchecked")
   public static Set lookupSubtypes(final Class clazz) {
      return reflections.getSubTypesOf(clazz);
   }

   public static <T> List<T> instantiate(final Set<Class<T>> classes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
      final List<T> instances = new ArrayList<>();

      for (Class<T> clazz : classes) {
         if (log.isDebugEnabled()) {
            log.debug("Creating instance of " + clazz.getName());
         }

         final Constructor c = clazz.getConstructor();

         @SuppressWarnings("unchecked")
         final T t = (T) c.newInstance();

         instances.add(t);
      }

      return instances;
   }
}
