/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2016 the original author or authors.
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

package io.silverware.microservices.enums;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public enum VerticleType {
   /**
    * verticle that is always executed using the single event loop thread
    */
   STANDARD,

   /**
    * verticle that runs using a thread from the thread pool, instance is never
    * executed concurrently by more than one thread
    */
   WORKER,

   /**
    * verticle that runs using a thread from the thread pool, instance can be
    * executed concurrently by more than one thread
    */
   MULTI_THREADED_WORKER
}
