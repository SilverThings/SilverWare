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
package io.silverware.microservices.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

/**
 * Specifies which {@link io.silverware.microservices.providers.cdi.internal.MicroserviceProxyBean} is used for the given injection point.
 */
@Qualifier
@Target({ TYPE, FIELD, PARAMETER, METHOD })
@Retention(RUNTIME)
@Documented
public @interface MicroserviceProxy {

   /**
    * The name of the microservice bean which is the proxy being injected into.
    *
    * @return microservice bean name
    */
   String beanName();

   /**
    * The field of the microservice bean which is the proxy being injected into.
    *
    * @return injected field name
    */
   String fieldName();

}
