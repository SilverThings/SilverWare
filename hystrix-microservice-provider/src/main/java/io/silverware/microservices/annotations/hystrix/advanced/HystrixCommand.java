/*
 * -----------------------------------------------------------------------\
 * Netflix, SilverWare
 *  
 * Copyright (C) 2012-2016 the original author or authors.
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
 * This annotation is used to specify some methods which should be processes as Hystrix commands.
 */
@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface HystrixCommand {

   /**
    * The command group key is used for grouping together commands such as for reporting, alerting, dashboards or team/library ownership.
    * <p>
    * Default: the name of the microservice being called (e.g. ticketService)
    *
    * @return group key
    */
   String groupKey() default "";

   /**
    * Hystrix command key.
    * <p>
    * Default: the name of the microservice making a call, called microservice and called method (e.g. holidayService:ticketService:bookTicket)
    *
    * @return command key
    */
   String commandKey() default "";

   /**
    * The thread-pool key is used to represent a {@link com.netflix.hystrix.HystrixThreadPool} for monitoring, metrics publishing, caching and other such uses.
    * <p>
    * Default: the name of the microservice being called (e.g. ticketService)
    *
    * @return thread pool key
    */
   String threadPoolKey() default "";

   /**
    * Specifies command properties.
    *
    * @return command properties
    */
   HystrixProperty[] commandProperties() default {};

   /**
    * Specifies thread pool properties.
    *
    * @return thread pool properties
    */
   HystrixProperty[] threadPoolProperties() default {};

   /**
    * Defines exceptions which should be ignored and wrapped to throw in {@link com.netflix.hystrix.exception.HystrixBadRequestException}.
    *
    * @return exceptions to ignore
    */
   Class<? extends Throwable>[] ignoredExceptions() default {};

}
