/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2015 - 2017 the original author or authors.
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
package io.silverware.microservices.providers.cdi.builtin;

/**
 * Stores any objects and state across services in the given JVM.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public interface Storage {

   String STORAGE = "silverware.cdi.storage";

   /**
    * Stores a new or overwrites an old value in the sotrage.
    * @param key Name of the value.
    * @param value The actual value.
    */
   void put(final String key, final Object value);

   /**
    * Gets a value from the storage.
    * @param key Name of the value to get.
    * @return The value stored under the given name.
    */
   Object get(final String key);

   /**
    * Drops the given value from the storage.
    * @param key The value name to be dropped.
    * @return True iff the value was in the storage.
    */
   boolean drop(final String key);

}
