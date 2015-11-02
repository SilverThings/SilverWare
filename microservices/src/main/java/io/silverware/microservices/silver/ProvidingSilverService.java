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
package io.silverware.microservices.silver;

import io.silverware.microservices.MicroserviceMetaData;

import java.util.Set;

/**
 * SilverService that is able to provide Microservices.
 *
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public interface ProvidingSilverService extends SilverService {

   /**
    * Looks up all available Microservices supported by this provider.
    *
    * @param metaData Meta-data query.
    * @return All available Microservices supported by this provider that meet the given query.
    */
   Set<Object> lookupMicroservice(final MicroserviceMetaData metaData);

   /**
    * Looks up all local Microservices supported by this provider.
    * By default it relies on {@link #lookupMicroservice(MicroserviceMetaData)}.
    *
    * @param metaData Meta-data query.
    * @return All local Microservices supported by this provider that meet the given query.
    */
   default Set<Object> lookupLocalMicroservice(final MicroserviceMetaData metaData) {
      return lookupMicroservice(metaData);
   }
}
