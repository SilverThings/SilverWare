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

import java.util.Random;

/**
 * Lookup just local service implementations.
 *
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class LocalLookupStrategy extends AbstractLookupStrategy {

   private Random rnd = new Random();

   @Override
   public Object getService() {
      final Object[] services = context.lookupLocalMicroservice(metaData).toArray();
      return services[rnd.nextInt(services.length)];
   }

}
