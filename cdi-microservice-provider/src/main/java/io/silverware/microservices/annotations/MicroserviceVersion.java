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
package io.silverware.microservices.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation can be used in two different scenarios:
 * <ul>
 * <li><strong>Microservice:</strong> can specify version of Microservice Implementation and API. </li>
 * <li><strong>Injection point:</strong> can specify supported version of a Microservice API. </li>
 * </ul>
 * These versions are used in the lookup of the Microservices in the cluster.
 * See @{@link io.silverware.microservices.providers.cdi.util.VersionResolver}
 * See @{@link io.silverware.microservices.util.VersionComparator}
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Target({ ElementType.TYPE })
public @interface MicroserviceVersion {

   /**
    * Gets the API version of the Microservice.
    */
   String api() default "";

   /**
    * Gets the implementation version of the Microservice.
    * If not defined then {@link io.silverware.microservices.providers.cdi.util.VersionResolver} continues in search for microservice version.
    */
   String implementation() default "";
}
