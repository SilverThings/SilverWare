/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2015 - 2017 the original author or authors.
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

import java.lang.reflect.Method;
import javax.enterprise.inject.spi.InjectionPoint;

import javassist.util.proxy.MethodHandler;

/**
 * Method handler for invoking microservice methods.
 *
 * Descendants need to have a constructor with a single parameter of type MicroserviceMethodHandler (the next method handler).
 * The implementations of methods should call the same method on the next method handler but they can alter its behaviour by making some additional changes.
 * Descendants also need to specify the priority on their classes. The higher it is, the later they are invoked (closer to the actual service call).
 *
 * @see javax.annotation.Priority
 */
public abstract class MicroserviceMethodHandler implements MethodHandler {

   public static final int DEFAULT_PRIORITY = 100;

   @Override
   public final Object invoke(final Object self, final Method thisMethod, final Method proceed, final Object[] args) throws Throwable {
      return invoke(thisMethod, args);
   }

   public abstract Object invoke(Method method, Object... args) throws Exception;

   public abstract MicroserviceProxyBean getProxyBean();

   public abstract InjectionPoint getInjectionPoint();

}
