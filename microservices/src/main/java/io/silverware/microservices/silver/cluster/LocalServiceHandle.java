/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2015 the original author or authors.
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
package io.silverware.microservices.silver.cluster;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds a handle to a microservice implementation.
 * Keeps a reference to real Microservice implementation in the case of a local Microservice,
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class LocalServiceHandle implements ServiceHandle {

   private static final transient AtomicInteger handleSource = new AtomicInteger(0);

   private final int handle;

   private final MicroserviceMetaData query;

   private final transient Object proxy;

   public LocalServiceHandle(final MicroserviceMetaData query, final Object proxy) {
      this.handle = handleSource.getAndIncrement();
      this.query = query;
      this.proxy = proxy;
   }

   public int getHandle() {
      return handle;
   }

   @Override
   public Object getProxy() {
      return proxy;
   }

   @Override
   public String getHost() {
      return "LOCAL";
   }

   public MicroserviceMetaData getMetaData() {
      return query;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final LocalServiceHandle that = (LocalServiceHandle) o;

      if (handle != that.handle) {
         return false;
      }
      return query.equals(that.query);
   }

   @Override
   public int hashCode() {
      int result = handle;
      result = 31 * result + query.hashCode();
      return result;
   }

   @Override
   public String toString() {
      return "LocalServiceHandle{" + "handle=" + handle + ", query=" + query + ", proxy=" + proxy + '}';
   }

   /**
    * Context is not used
    *
    * @param context
    *       Local microservice context
    * @param methodName
    *       name of the method to be invoked
    * @param paramTypes
    *       classes of parameters
    * @param params
    *       parameters of method called
    * @return result of the invocation
    * @throws Exception
    *       in case of any error
    */
   @Override
   public Object invoke(final Context context, final String methodName, final Class[] paramTypes, final Object[] params) throws Exception {
      final Method method = proxy.getClass().getDeclaredMethod(methodName, paramTypes);
      return method.invoke(proxy, params);

   }

}
