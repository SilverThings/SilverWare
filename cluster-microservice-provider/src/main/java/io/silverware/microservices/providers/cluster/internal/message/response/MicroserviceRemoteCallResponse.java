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
package io.silverware.microservices.providers.cluster.internal.message.response;

import io.silverware.microservices.providers.cluster.internal.message.request.MicroserviceRemoteCallRequest;

/**
 * Message which is sent as response for {@link MicroserviceRemoteCallRequest}
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class MicroserviceRemoteCallResponse implements ResponseMessage {

   private final Object messageCallResult;

   public MicroserviceRemoteCallResponse(Object messageCallResult) {
      this.messageCallResult = messageCallResult;
   }

   public Object getMessageCallResult() {
      return messageCallResult;
   }
}
