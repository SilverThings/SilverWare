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
package io.silverware.microservices.providers.cluster.internal.message.responder;

import static io.silverware.microservices.providers.cluster.internal.exception.SilverWareClusteringException.SilverWareClusteringError.MULTIPLE_IMPLEMENTATIONS_FOUND;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.providers.cluster.internal.exception.SilverWareClusteringException;
import io.silverware.microservices.providers.cluster.internal.message.response.MicroserviceSearchResponse;
import io.silverware.microservices.silver.cluster.LocalServiceHandle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.Address;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is responsible for retrieving correct microservices
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class MicroserviceSearchResponder extends AbstractResponder<MicroserviceMetaData, MicroserviceSearchResponse> {

   /**
    * Logger.
    */
   protected static Logger log = LogManager.getLogger(MicroserviceSearchResponder.class);


   public MicroserviceSearchResponder(Context context) {
      super(context);
   }

   @Override
   MicroserviceSearchResponse doProcessMessage(Address source, MicroserviceMetaData query) {
      List<LocalServiceHandle> serviceHandles = filterVersionCompatible(context.assureHandles(query), query);

      if (serviceHandles.size() > 1) {
         log.error("Multiple implementations found.");
         throw new SilverWareClusteringException(MULTIPLE_IMPLEMENTATIONS_FOUND);
      }
      if (serviceHandles.isEmpty()) {
         if (log.isTraceEnabled()) {
            log.trace("No services found for metadata: {} ", query);
         }
         return new MicroserviceSearchResponse(null, MicroserviceSearchResponse.Result.NOT_FOUND);
      } else if (log.isTraceEnabled()) {
         log.trace("{} services found for {}", serviceHandles.size(), query);
      }
      return new MicroserviceSearchResponse(serviceHandles.get(0).getHandle(), MicroserviceSearchResponse.Result.FOUND);

   }

   private List<LocalServiceHandle> filterVersionCompatible(List<LocalServiceHandle> localServiceHandles, MicroserviceMetaData query) {
      return localServiceHandles.stream().filter(handle -> handle.getMetaData().satisfies(query)).collect(Collectors.toList());
   }

}
