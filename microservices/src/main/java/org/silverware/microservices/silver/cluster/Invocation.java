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
package org.silverware.microservices.silver.cluster;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silverware.microservices.Context;
import org.silverware.microservices.SilverWareException;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class Invocation {

   private static Logger log = LogManager.getLogger(Invocation.class);

   private final int handle;

   private final String method;

   private final Object[] params;

   public Invocation(final int handle, final String method, final Object[] params) {
      this.handle = handle;
      this.method = method;
      this.params = params;
   }

   public int getHandle() {
      return handle;
   }

   public String getMethod() {
      return method;
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
      if (method != null ? !method.equals(that.method) : that.method != null) {
         return false;
      }
      // Probably incorrect - comparing Object[] arrays with Arrays.equals
      return Arrays.equals(params, that.params);

   }

   @Override
   public int hashCode() {
      int result = handle;
      result = 31 * result + (method != null ? method.hashCode() : 0);
      result = 31 * result + (params != null ? Arrays.hashCode(params) : 0);
      return result;
   }

   @Override
   public String toString() {
      return "Invocation{" +
            "handle=" + handle +
            ", method='" + method + '\'' +
            ", params=" + Arrays.toString(params) +
            '}';
   }

   public Object invoke(final Context context) throws Exception {
      if (log.isTraceEnabled()) {
         log.trace("Invoking Microservice with invocation {}.", toString());
      }

      final ServiceHandle serviceHandle = context.getInboundServiceHandle(handle);

      if (serviceHandle == null) {
         throw new SilverWareException(String.format("Handle no. %d. No such handle found.", getHandle()));
      }

      final Class[] paramTypes = new Class[params.length];
      for (int i = 0; i < params.length; i++) {
         paramTypes[i] = params[i].getClass();
      }

      final Method method = serviceHandle.getService().getClass().getDeclaredMethod(getMethod(), paramTypes);
      return method.invoke(serviceHandle.getService(), params);
   }


}

