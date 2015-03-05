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
package org.silverware.microservices.providers.cdi.internal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class MicroserviceProxy implements MethodHandler {

   private static final Logger log = LogManager.getLogger(MicroserviceProxy.class);

   private final Class<?> type;

   private final Object service;

   private MicroserviceProxy(final Class<?> type) throws Exception {
      this.type = type;
      if (!type.isInterface()) {
         service = type.getConstructor(new Class[0]).newInstance();
      } else {
         service = null;
      }
      log.info("Created service " + service);
   }

   @SuppressWarnings("unchecked")
   public static <T> T getProxy(Class<T> t) {
      try {
         ProxyFactory factory = new ProxyFactory();
         if (t.isInterface()) {
            factory.setInterfaces(new Class[] { t });
         } else {
            factory.setSuperclass(t);
         }
         return (T) factory.create(new Class[0], new Object[0], new MicroserviceProxy(t));
      } catch (Exception e) {
         throw new IllegalStateException("Cannot create proxy for class " + t.getClass().getName() + ": ", e);
      }
   }

   @Override
   public Object invoke(final Object o, final Method thisMethod, final Method proceed, final Object[] args) throws Throwable {
      log.info("Invocation of " + thisMethod + ", proceed " + proceed);

      return thisMethod.invoke(service, args);
   }
}
