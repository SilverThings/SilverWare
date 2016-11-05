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
package io.silverware.microservices.providers.cluster;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.providers.cluster.internal.JgroupsMessageSender;
import io.silverware.microservices.providers.cluster.internal.exception.SilverWareClusteringException;
import io.silverware.microservices.providers.cluster.internal.message.request.MicroserviceRemoteCallRequest;
import io.silverware.microservices.providers.cluster.internal.message.response.MicroserviceRemoteCallResponse;
import io.silverware.microservices.silver.cluster.Invocation;
import io.silverware.microservices.silver.cluster.ServiceHandle;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.Address;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static io.silverware.microservices.providers.cluster.internal.exception.SilverWareClusteringException.SilverWareClusteringError.INVOCATION_EXCEPTION;
import static io.silverware.microservices.providers.cluster.internal.exception.SilverWareClusteringException.SilverWareClusteringError.PROCESSING_ERROR;

/**
 * This class represent remote handle which allows invocation remote services
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class RemoteServiceHandle implements ServiceHandle, MethodHandler {

   private static final Logger log = LogManager.getLogger(ClusterMicroserviceProvider.class);

   private final Address address;
   private final int handle;
   private final JgroupsMessageSender sender;
   private final Object proxy;
   private final Class type;

   public RemoteServiceHandle(Address address, int handle, JgroupsMessageSender sender, MicroserviceMetaData metaData) {
      this.address = address;
      this.handle = handle;
      this.sender = sender;
      this.type = metaData.getType();
      ProxyFactory factory = new ProxyFactory();
      Class type = metaData.getType();
      if (type.isInterface()) {
         factory.setInterfaces(new Class[]{type});
      } else {
         factory.setSuperclass(type);
      }
      try {
         proxy = factory.create(new Class[0], new Object[0], this);
      } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
         throw new SilverWareClusteringException(PROCESSING_ERROR, e);
      }
   }

   @Override
   public String getHost() {
      return address.toString();
   }

   @Override
   public Object getProxy() {
      return proxy;
   }

   @Override
   public Object invoke(Context context, String method, Class[] paramTypes, Object[] params) throws Exception {
      int paramCount = params.length;
      if ("toString".equals(method) && paramCount == 0) {
         return toString();
      } else if ("hashCode".equals(method) && paramCount == 0) {
         return this.hashCode();
      } else if ("equals".equals(method) && paramCount == 1) {
         return this.equals(params[0]);
      } else if ("getClass".equals(method) && paramCount == 0) {
         return type;
      }
      Invocation invocation = new Invocation(handle, method, paramTypes, params);
      try {
         MicroserviceRemoteCallResponse response = sender.sendToAddressSync(address, new MicroserviceRemoteCallRequest(invocation));
         return response.getMessageCallResult();
      } catch (SilverWareClusteringException e) {
         log.error(e);
         if (e.getReason() == INVOCATION_EXCEPTION) {
            throw (Exception) e.getCause();
         }
         throw e;
      }
   }


   @Override
   public Object invoke(Object self, Method method, Method proceed, Object[] args) throws Throwable {
      return invoke(null, method.getName(), method.getParameterTypes(), args);
   }


   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (!(o instanceof RemoteServiceHandle)) {
         return false;
      }

      RemoteServiceHandle that = (RemoteServiceHandle) o;

      if (handle != that.handle) {
         return false;
      }
      return address.equals(that.address);

   }

   @Override
   public int hashCode() {
      int result = address.hashCode();
      result = 31 * result + handle;
      return result;
   }

   @Override
   public String toString() {
      return "RemoteServiceHandle for " +
              "address: " + address +
              "and type: " + type + " .";
   }
}
