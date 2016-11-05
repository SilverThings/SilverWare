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
package io.silverware.microservices.providers.http.resteasy;

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;

/**
 * Silverware resource factory producing a Microservice from a context
 * and Microservice metadata.
 *
 * @author Radek Koubsky (radekkoubsky@gmail.com)
 */
public class SilverwareResourceFactory implements ResourceFactory {
   private final Context context;
   private final MicroserviceMetaData microserviceMetadata;

   /**
    * Ctor.
    *
    * @param context
    *       Microservices context
    * @param microserviceMetadata
    *       Microservice metadata
    */
   public SilverwareResourceFactory(final Context context, final MicroserviceMetaData microserviceMetadata) {
      super();
      this.context = context;
      this.microserviceMetadata = microserviceMetadata;
   }

   @Override
   public Class<?> getScannableClass() {
      return this.microserviceMetadata.getType();
   }

   @Override
   public void registered(final ResteasyProviderFactory factory) {
      // TODO Auto-generated method stub

   }

   @Override
   public Object createResource(final HttpRequest request, final HttpResponse response,
         final ResteasyProviderFactory factory) {
      /*
       * What should follow if an empty set of microservices is returned?
       */
      return this.context.lookupMicroservice(this.microserviceMetadata).iterator().next();
   }

   @Override
   public void requestFinished(final HttpRequest request, final HttpResponse response, final Object resource) {
      // TODO Auto-generated method stub

   }

   @Override
   public void unregistered() {
      // TODO Auto-generated method stub

   }

}
