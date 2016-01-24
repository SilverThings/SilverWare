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
package io.silverware.microservices.silver.services.lookup;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.silver.services.LookupStrategy;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
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
