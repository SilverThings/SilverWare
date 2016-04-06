/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2014 - 2016 the original author or authors.
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
package io.silverware.microservices.providers.rest.internal;

import io.silverware.microservices.providers.rest.annotation.JsonService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class JsonRestServiceProxy implements MethodHandler {

   private static final Logger log = LogManager.getLogger(JsonRestServiceProxy.class);

   private JsonRestService restService;
   private Class iface;

   public JsonRestServiceProxy(final Class iface, final JsonService service) {
      this.restService = new JsonRestService(service.endpoint(), service.httpMethod());
      this.iface = iface;
   }

   @SuppressWarnings("unchecked")
   public static <T> T getProxy(final Class iface, final JsonService service) {
      try {
         ProxyFactory factory = new ProxyFactory();
         factory.setInterfaces(new Class[] { iface });
         return (T) factory.create(new Class[0], new Object[0], new JsonRestServiceProxy(iface, service));
      } catch (Exception e) {
         throw new IllegalStateException("Cannot create proxy for REST interface " + iface.getName() + ": ", e);
      }
   }

   @Override
   public Object invoke(final Object o, final Method thisMethod, final Method proceed, final Object[] args) throws Throwable {
      if (thisMethod.getDeclaringClass() == Object.class) {
         final String methodName = thisMethod.getName();
         final int paramCount = thisMethod.getParameterTypes().length;

         if ("toString".equals(methodName) && paramCount == 0) {
            return "REST proxy for " + iface.getName();
         } else if ("equals".equals(methodName) && paramCount == 1) {
            return this.equals(args[0]);
         } else if ("hashCode".equals(methodName) && paramCount == 0) {
            return this.hashCode();
         } else if ("getClass".equals(methodName) && paramCount == 0) {
            return iface;
         }
      }

      if (log.isDebugEnabled()) {
         log.debug("Invocation of " + thisMethod + ", proceed " + proceed);
      }

      final Map<String, Object> argsMap = new HashMap<>();
      final Parameter[] parameters = thisMethod.getParameters();
      for (int i = 0; i < parameters.length; i++) {
         argsMap.put(parameters[i].getName(), args[i]);
      }

      return restService.call(thisMethod.getName(), argsMap);
   }

}
