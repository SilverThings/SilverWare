/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2010 - 2016 the original author or authors.
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
package io.silverware.microservices.providers.cluster.internal.message.responder;

import static io.silverware.microservices.providers.cluster.internal.exception.SilverWareClusteringException.SilverWareClusteringError.INVOCATION_EXCEPTION;

import io.silverware.microservices.Context;
import io.silverware.microservices.providers.cluster.internal.exception.SilverWareClusteringException;
import io.silverware.microservices.providers.cluster.internal.message.request.MicroserviceRemoteCallRequest;
import io.silverware.microservices.providers.cluster.internal.message.response.MicroserviceRemoteCallResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.Address;

/**
 * This class is responsible for a remote method call.
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class MicroServiceRemoteCallResponder extends AbstractResponder<MicroserviceRemoteCallRequest, MicroserviceRemoteCallResponse> {
   /**
    * Logger.
    */
   protected static Logger log = LogManager.getLogger(MicroServiceRemoteCallResponder.class);

   public MicroServiceRemoteCallResponder(Context context) {
      super(context);
   }

   @Override
   MicroserviceRemoteCallResponse doProcessMessage(Address source, MicroserviceRemoteCallRequest remoteCallRequest) {
      try {
         return new MicroserviceRemoteCallResponse(remoteCallRequest.getInvocation().invoke(context));
      } catch (Exception e) {
         log.error("Error in remote call invocation", e);
         throw new SilverWareClusteringException(INVOCATION_EXCEPTION, e);
      }
   }

}
