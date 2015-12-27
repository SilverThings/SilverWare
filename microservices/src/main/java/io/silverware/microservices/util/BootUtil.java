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
package io.silverware.microservices.util;

import io.silverware.microservices.Context;
import io.silverware.microservices.Executor;

/**
 * Boots the Microservices platform. Intended to be used as a public API to initialize
 * the platform from another code.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class BootUtil {

   /**
    * Context of the current platform instance.
    */
   private Context context = new Context();

   /**
    * Gets the Microservices platform and searches for providers in the given list of packages.
    *
    * @param packages A variable array of package names.
    * @return A thread with the running platform.
    */
   public Thread getMicroservicePlatform(final String... packages) {
      return getMicroservicePlatform(String.join(",", packages));
   }

   /**
    * Gets the Microservices platform and searches for providers in the given list of packages.
    *
    * @param packages A comma separated list of package names.
    * @return A thread with the running platform.
    */
   public Thread getMicroservicePlatform(final String packages) {
      Thread t = new Thread(() -> {
         context.getProperties().put(Context.DEPLOYMENT_PACKAGES, packages);
         try {
            Executor.bootHook(context);
         } catch (InterruptedException ie) {
            // we are likely to be done
         }
      });

      t.setName("SilverWare-boot");

      return t;
   }

   /**
    * Gets the context of the created platform.
    *
    * @return The context of the created platform.
    */
   public Context getContext() {
      return context;
   }
}
