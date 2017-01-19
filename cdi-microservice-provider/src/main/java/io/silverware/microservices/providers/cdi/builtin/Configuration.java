/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2015 the original author or authors.
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
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public interface Configuration {

   /**
    * Gets the value of a configuration property.
    * @param propertyName Name of the configuration property to obtain.
    * @return The value of a configuration property.
    */
   Object getProperty(final String propertyName);
}
