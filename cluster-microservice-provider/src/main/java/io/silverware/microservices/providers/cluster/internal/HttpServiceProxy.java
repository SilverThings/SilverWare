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
package io.silverware.microservices.providers.cluster.internal;

import io.silverware.microservices.Context;
import io.silverware.microservices.silver.cluster.LocalServiceHandle;
import io.silverware.microservices.silver.cluster.ServiceHandle;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class HttpServiceProxy implements MethodHandler {

   private final Context context;
   private final ServiceHandle serviceHandle;

   private HttpServiceProxy(final Context context, final ServiceHandle serviceHandle) {
      this.context = context;
      this.serviceHandle = serviceHandle;
   }

   /**
    * Return proxy created for a http invoker from a given service handle
    *
    * @param context       context from which is a handle created
    * @param serviceHandle service handle for which is proxy created
    * @param <T>           type of a proxy
    * @return created proxy
    */
   public static <T> T getProxy(final Context context, final LocalServiceHandle serviceHandle) {
      try {
         ProxyFactory factory = new ProxyFactory();
         if (serviceHandle.getMetaData().getType().isInterface()) {
            factory.setInterfaces(new Class[]{serviceHandle.getMetaData().getType()});
         } else {
            factory.setSuperclass(serviceHandle.getMetaData().getType());
         }
         return (T) factory.create(new Class[0], new Object[0], new HttpServiceProxy(context, serviceHandle));
      } catch (Exception e) {
         throw new IllegalStateException("Cannot create Http proxy for class " + serviceHandle.getMetaData().getType().getName() + ": ", e);
      }
   }

   @Override
   public Object invoke(final Object o, final Method method, final Method method1, final Object[] objects) throws Throwable {
      return serviceHandle.invoke(context, method.getName(), method.getParameterTypes(), objects);
   }
}
