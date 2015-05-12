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
package org.silverware.microservices.silver.services.lookup;

import org.silverware.microservices.Context;
import org.silverware.microservices.MicroserviceMetaData;
import org.silverware.microservices.silver.services.LookupStrategy;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
abstract public class AbstractLookupStrategy implements LookupStrategy {

   protected Context context;
   protected MicroserviceMetaData metaData;
   protected Set<Annotation> options;

   @Override
   public void initialize(final Context context, final MicroserviceMetaData metaData, final Set<Annotation> options) {
      this.context = context;
      this.metaData = metaData;
      this.options = options;
   }
}
