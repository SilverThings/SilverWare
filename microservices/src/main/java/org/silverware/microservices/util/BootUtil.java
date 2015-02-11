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
package org.silverware.microservices.util;

import org.silverware.microservices.Context;
import org.silverware.microservices.Executor;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class BootUtil {

   private Context context = new Context();

   public Thread getMicroservicePlatform(final String packages) {
      Thread t = new Thread(new Runnable() {
         @Override
         public void run() {
            context.getProperties().put(Context.DEPLOYMENT_PACKAGES, packages);
            try {
               Executor.bootHook(context);
            } catch (InterruptedException ie) {
               // we are likely to be done
            }
         }
      });

      t.setName("SilverWare-boot");

      return t;
   }

   public Context getContext() {
      return context;
   }
}
