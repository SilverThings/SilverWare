/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2015 - 2016 the original author or authors.
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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

/**
 * Microservice reference.
 *
 * Use this annotation, in conjunction with the {@link javax.inject.Inject}
 * annotation, to inject a reference to a Microservice.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
@Qualifier
@Target({ FIELD })
@Retention(RUNTIME)
@Documented
public @interface MicroserviceReference {

   /**
    * Get the name of the Microservice that should be injected.
    *
    * @return The name of the Microservice to be injected.
    */
   String value() default "";
}
