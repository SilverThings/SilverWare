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
package io.silverware.microservices.silver.services.lookup;

import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class RandomRobinLookupStrategy extends AbstractLookupStrategy {

   @Override
   public Object getService() {
      Set<Object> services = context.lookupMicroservice(metaData);
      if (services.isEmpty()) {
         throw new RuntimeException("No service found for: " + metaData);
      }
      return services.toArray()[ThreadLocalRandom.current().nextInt(services.size())];
   }
}
