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
package io.silverware.microservices.silver.services;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.annotations.InvocationPolicy;
import io.silverware.microservices.silver.services.lookup.RandomRobinLookupStrategy;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Set;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class LookupStrategyFactory {

   /**
    * Logger.
    */
   private static final Logger log = LogManager.getLogger(LookupStrategyFactory.class);

   /**
    * Returns strategy based on given parameters
    *
    * @param context  silverware context
    * @param metaData - metadata for microservice
    * @param options  - other options
    * @return strategy
    */
   public static LookupStrategy getStrategy(final Context context, final MicroserviceMetaData metaData, final Set<Annotation> options) {
      LookupStrategy strategy = null;

      for (Annotation option : options) {
         if (option.annotationType().isAssignableFrom(InvocationPolicy.class)) {
            InvocationPolicy policy = (InvocationPolicy) option;
            Class<? extends LookupStrategy> clazz = policy.lookupStrategy();

            try {
               Constructor c = clazz.getConstructor();
               strategy = (LookupStrategy) c.newInstance();
               strategy.initialize(context, metaData, options);
               break;
            } catch (Exception e) {
               log.warn(String.format("Could not instantiate lookup strategy class %s:", clazz.getName()), e);
            }
         }
      }

      if (strategy == null) {
         strategy = new RandomRobinLookupStrategy();
         strategy.initialize(context, metaData, options);
      }

      return strategy;
   }
}
