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
package io.silverware.microservices.silver.cluster;

import io.silverware.microservices.Context;
import io.silverware.microservices.SilverWareException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Carries information needed to invoke a Microservice remotely.
 * Can actually invoke the local Microservice based on the provided data
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class Invocation implements Serializable {

   private static final Logger log = LogManager.getLogger(Invocation.class);

   private final int handle;

   private final String method;

   private final Class[] paramTypes;

   private final Object[] params;

   public Invocation(final int handle, final String method, final Class[] paramTypes, final Object[] params) {
      this.handle = handle;
      this.method = method;
      this.paramTypes = paramTypes;
      this.params = params;
   }

   public int getHandle() {
      return handle;
   }

   public String getMethod() {
      return method;
   }

   public Class[] getParamTypes() {
      return paramTypes;
   }

   public Object[] getParams() {
      return params;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final Invocation that = (Invocation) o;

      if (handle != that.handle) {
         return false;
      }
      if (!method.equals(that.method)) {
         return false;
      }
      // Probably incorrect - comparing Object[] arrays with Arrays.equals
      if (!Arrays.equals(paramTypes, that.paramTypes)) {
         return false;
      }
      // Probably incorrect - comparing Object[] arrays with Arrays.equals
      return Arrays.equals(params, that.params);

   }

   @Override
   public int hashCode() {
      int result = handle;
      result = 31 * result + method.hashCode();
      result = 31 * result + Arrays.hashCode(paramTypes);
      result = 31 * result + Arrays.hashCode(params);
      return result;
   }

   @Override
   public String toString() {
      return "Invocation{" + "handle=" + handle + ", method='" + method + '\'' + ", paramTypes=" + Arrays.toString(paramTypes) + ", params=" + Arrays.toString(params) + '}';
   }

   /**
    * Invokes a method with given context
    *
    * @param context context which will be used to invoke method
    * @return result of the invocation
    * @throws Exception when some error occurs
    */
   public Object invoke(final Context context) throws Exception {
      if (log.isTraceEnabled()) {
         log.trace("Invoking Microservice with invocation {}.", toString());
      }

      final LocalServiceHandle serviceHandle = context.getInboundServiceHandle(handle);

      if (serviceHandle == null) {
         throw new SilverWareException(String.format("Handle no. %d. No such handle found.", getHandle()));
      }

      final Method method = serviceHandle.getProxy().getClass().getDeclaredMethod(getMethod(), paramTypes);
      return method.invoke(serviceHandle.getProxy(), params);
   }

}
