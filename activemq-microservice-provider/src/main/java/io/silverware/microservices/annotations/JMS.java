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

import io.silverware.microservices.enums.ConnectionType;
import io.silverware.microservices.utils.ActiveMQConstants;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.jms.JMSContext;
import javax.naming.spi.InitialContextFactory;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JMS {

   /**
    * returns the {@link String} representing the server uri
    *
    * @return the server uri
    */
   String serverUri() default "";

   /**
    * returns the user name for the security purposes
    *
    * @return the user name for the security
    */
   String userName() default "";

   /**
    * returns the user password for the security purposes
    *
    * @return the user password for the security
    */
   String password() default "";

   /**
    * returns the {@link ConnectionType} for this injection
    *
    * @return the {@link ConnectionType} of this injection
    */
   ConnectionType connectionType() default ConnectionType.SHARED;

   /**
    * Only from JMS 2.0
    * Returns the session mode for this {@link JMSContext}
    *
    * @return the session mode of the {@link JMSContext}
    * @since JMS 2.0
    */
   int sessionMode() default ActiveMQConstants.DEFAULT_SESSION_TYPE;

   /**
    * Returns the initial context factory used for this injection
    *
    * @return the initial context factory for this injection
    */
   Class<? extends InitialContextFactory> initialContextFactory() default
           org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory.class;
}
