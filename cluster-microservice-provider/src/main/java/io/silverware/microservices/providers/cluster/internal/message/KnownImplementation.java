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
package io.silverware.microservices.providers.cluster.internal.message;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.providers.cluster.internal.message.request.MicroserviceRemoteCallRequest;
import io.silverware.microservices.providers.cluster.internal.message.responder.MicroServiceRemoteCallResponder;
import io.silverware.microservices.providers.cluster.internal.message.responder.MicroserviceSearchResponder;
import io.silverware.microservices.providers.cluster.internal.message.responder.Responder;
import io.silverware.microservices.providers.cluster.internal.message.response.MicroserviceRemoteCallResponse;
import io.silverware.microservices.providers.cluster.internal.message.response.MicroserviceSearchResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Enum containing all processors for a given classes.
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public enum KnownImplementation {
   MICROSERVICE_REMOTE_CALL(MicroserviceRemoteCallResponse.class, MicroserviceRemoteCallRequest.class, MicroServiceRemoteCallResponder.class, MicroServiceRemoteCallResponder::new),
   MICROSERVICE_REMOTE_SEARCH(MicroserviceSearchResponse.class, MicroserviceMetaData.class, MicroserviceSearchResponder.class, MicroserviceSearchResponder::new);

   private final Class responseClass;
   private final Class messageClass;
   private final Class responderClass;
   private final Function<Context, Responder> responderFactory;
   private static final List<KnownImplementation> KNOWN_IMPLEMENTATIONS = Arrays.asList(values());

   KnownImplementation(Class responseClass, Class messageClass, Class responderClass, Function<Context, Responder> responderFactory) {
      this.responseClass = responseClass;
      this.messageClass = messageClass;
      this.responderClass = responderClass;
      this.responderFactory = responderFactory;
   }

   public Class getResponseClass() {
      return responseClass;
   }

   public Class getMessageClass() {
      return messageClass;
   }

   public Class getResponderClass() {
      return responderClass;
   }

   public static Map<Class, Responder> initializeReponders(Context context) {
      return KNOWN_IMPLEMENTATIONS.stream().collect(Collectors.toMap(KnownImplementation::getMessageClass, v -> v.responderFactory.apply(context)));
   }
}
