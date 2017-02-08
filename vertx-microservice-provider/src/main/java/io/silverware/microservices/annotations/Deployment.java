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

import io.silverware.microservices.enums.VerticleType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Deployment {
   /**
    * returns the verticle type
    *
    * @return the verticle type
    */
   VerticleType type() default VerticleType.STANDARD;

   /**
    * returns the number of verticle instances to be deployed
    *
    * @return the number of verticle deployments
    */
   int instances() default 1;

   /**
    * returns the name of the isolation group used in this verticle deployment
    *
    * @return the name of the isolation group
    */
   String isolationGroup() default "";

   /**
    * returns the string array of isolated classes names
    *
    * @return the isolated classes
    */
   String[] isolatedClasses() default {};

   /**
    * returns the string array of classes and packages to be added to the classpath
    *
    * @return extra classpath
    */
   String[] extraClasspath() default {};

   /**
    * returns whether the high availability option is set
    *
    * @return the high availability option
    */
   boolean ha() default false;

   /**
    * returns the name of the JSON configuration file containg configuration properties
    *
    * @return name of the JSON config file
    */
   String config() default "";

   /**
    * returns whether or not the verticle should be deployed
    *
    * @return if the verticle should be deployed
    */
   boolean value() default true;
}
