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
import io.silverware.microservices.providers.cluster.internal.JgroupsMessageSender;
import io.silverware.microservices.providers.cluster.internal.message.request.MicroserviceRemoteCallRequest;
import io.silverware.microservices.providers.cluster.internal.message.response.MicroserviceRemoteCallResponse;
import io.silverware.microservices.silver.cluster.Invocation;
import io.silverware.microservices.silver.cluster.ServiceHandle;
import org.jgroups.Address;

/**
 * This class represent remote handle which allows invocation remote services
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class RemoteServiceHandle implements ServiceHandle {

   private final Address address;
   private final int handle;
   private final JgroupsMessageSender sender;

   public RemoteServiceHandle(Address address, int handle, JgroupsMessageSender sender) {
      this.address = address;
      this.handle = handle;
      this.sender = sender;
   }

   @Override
   public String getHost() {
      return address.toString();
   }


   @Override
   public Object invoke(Context context, String method, Class[] paramTypes, Object[] params) throws Exception {
      Invocation invocation = new Invocation(handle, method, paramTypes, params);
      MicroserviceRemoteCallResponse response = sender.sendToAddressSync(address, new MicroserviceRemoteCallRequest(invocation));
      return response.getMessageCallResult();

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
}
