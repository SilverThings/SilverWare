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
package io.silverware.microservices.providers;

import io.silverware.microservices.Context;

/**
 * Simple minimalistic microservice implementation interface.
 * Upon boot, the initialize method is called. After a successful initialization, all services will be started in their
 * dedicated thread. A proper shutdown must be part of the run() method.
 *
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public interface MicroserviceProvider extends Runnable {

   default void initialize(final Context context) {
   }

   void run();
}
