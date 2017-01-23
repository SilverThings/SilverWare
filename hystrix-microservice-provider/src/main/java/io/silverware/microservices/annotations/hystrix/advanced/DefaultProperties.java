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
package io.silverware.microservices.annotations.hystrix.advanced;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to specify default parameters for Hystrix commands which can be changed by {@link HystrixCommand} annotation.
 */
@Documented
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultProperties {

   /**
    * Specifies default group key used for each Hystrix command by default unless a command specifies group key explicitly.
    * For additional info about this property see {@link HystrixCommand#groupKey()}.
    *
    * @return default group key
    */
   String groupKey() default "";

   /**
    * Specifies default thread pool key used for each hystrix command by default unless a command specifies thread pool key explicitly.
    * For additional info about this property see {@link HystrixCommand#threadPoolKey()}
    *
    * @return default thread pool
    */
   String threadPoolKey() default "";

   /**
    * Specifies command properties that will be used for each Hystrix command by default unless command properties explicitly specified in {@link HystrixCommand}.
    *
    * @return command properties
    */
   HystrixProperty[] commandProperties() default {};

   /**
    * Specifies thread pool properties that will be used for each Hystrix command by default unless thread pool properties explicitly specified in {@link HystrixCommand}.
    *
    * @return thread pool properties
    */
   HystrixProperty[] threadPoolProperties() default {};

   /**
    * Defines exceptions which should be ignored and wrapped to throw in {@link com.netflix.hystrix.exception.HystrixBadRequestException}.
    * All methods annotated with {@link HystrixCommand} will automatically inherit this property.
    *
    * @return exceptions to ignore
    */
   Class<? extends Throwable>[] ignoredExceptions() default {};

}
