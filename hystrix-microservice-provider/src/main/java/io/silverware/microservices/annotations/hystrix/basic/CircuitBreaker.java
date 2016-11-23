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
package io.silverware.microservices.annotations.hystrix.basic;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Activates circuit breaker fault tolerance mechanism.
 *
 * The circuit opens when error threshold percentage and request volume threshold are exceeded at the same time.
 * After that it waits for a while (sleep window), tries a single request and if it succeeds, the circuit closes again.
 */
@Documented
@Retention(RUNTIME)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
public @interface CircuitBreaker {

   int DEFAULT_ERROR_PERCENTAGE = 50;
   int DEFAULT_REQUEST_VOLUME = 20;
   int DEFAULT_SLEEP_WINDOW = 5000;

   /**
    * Error percentage at or above which the circuit should trip open and start short-circuiting requests to fallback logic.
    *
    * @return error threshold percentage
    */
   int errorPercentage() default DEFAULT_ERROR_PERCENTAGE;

   /**
    * Minimum number of requests in a rolling window that will trip the circuit.
    *
    * @return request volume threshold
    */
   int requestVolume() default DEFAULT_REQUEST_VOLUME;

   /**
    * Amount of time (in milliseconds), after tripping the circuit, to reject requests before allowing attempts again
    * to determine if the circuit should again be closed.
    *
    * @return sleep window in milliseconds
    */
   int sleepWindow() default DEFAULT_SLEEP_WINDOW;

}
