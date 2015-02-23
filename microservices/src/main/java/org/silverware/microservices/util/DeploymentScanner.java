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
import org.silverware.microservices.Context;
import org.silverware.microservices.providers.MicroserviceProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class DeploymentScanner {

   private static final Logger log = LogManager.getLogger(DeploymentScanner.class);

   private static DeploymentScanner defaultScanner = null;

   private final Reflections reflections;

   private DeploymentScanner() {
      reflections = new Reflections();
   }

   private DeploymentScanner(final String... packages) {
      reflections = new Reflections(packages);
   }

   public static synchronized DeploymentScanner getDefaultInstance() {
      if (defaultScanner == null) {
         defaultScanner = new DeploymentScanner();
      }

      return defaultScanner;
   }

   public static DeploymentScanner getInstance(final String... packages) {
      return new DeploymentScanner(packages);
   }

   public static DeploymentScanner getContextInstance(final Context context) {
      final String packages = (String) context.getProperties().get(Context.DEPLOYMENT_PACKAGES);
      if (packages != null) {
         if (log.isDebugEnabled()) {
            log.debug("Limited deployment packages: " + packages);
         }
         return getInstance(packages.split(Pattern.quote("[ ]*,[ ]*")));
      } else {
         return DeploymentScanner.getDefaultInstance();
      }
   }

   public Set<Class<? extends MicroserviceProvider>> lookupMicroserviceProviders() {
      return reflections.getSubTypesOf(MicroserviceProvider.class);
   }

   @SuppressWarnings("unchecked")
   public Set lookupSubtypes(final Class clazz) {
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