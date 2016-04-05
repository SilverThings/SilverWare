/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2014 - 2016 the original author or authors.
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
package io.silverware.microservices.providers.rest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.providers.rest.annotation.JsonService;
import io.silverware.microservices.providers.rest.api.RestService;
import io.silverware.microservices.providers.rest.internal.JsonRestService;
import io.silverware.microservices.providers.rest.internal.JsonRestServiceProxy;
import io.silverware.microservices.silver.RestClientSilverService;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class RestClientMicroserviceProvider implements MicroserviceProvider, RestClientSilverService {

   private static final Logger log = LogManager.getLogger(RestClientMicroserviceProvider.class);

   private Context context;

   @Override
   public void initialize(final Context context) {
      this.context = context;
   }

   @Override
   public Context getContext() {
      return context;
   }

   @Override
   public void run() {
      log.info("Hello from Camel microservice provider!");
   }

   private JsonService getJsonServiceAnnotation(final Set<Annotation> annotations) {
      for (Annotation a : annotations) {
         if (a instanceof JsonService) {
            return (JsonService) a;
         }
      }

      return null;
   }

   @Override
   public Set<Object> lookupMicroservice(final MicroserviceMetaData metaData) {

      if (RestService.class.isAssignableFrom(metaData.getType()) | metaData.getType().isInterface()) {
         JsonService js = getJsonServiceAnnotation(metaData.getAnnotations());

         if (js != null) {
            if (RestService.class.isAssignableFrom(metaData.getType())) {
               return Collections.singleton(new JsonRestService(js.endpoint(), js.httpMethod()));
            } else {
               return JsonRestServiceProxy.getProxy(metaData.getType(), js);
            }
         } else {
            log.warn("Attempt to inject RestService without JsonService qualifier.");
         }
      }

      return new HashSet<>();
   }

}
