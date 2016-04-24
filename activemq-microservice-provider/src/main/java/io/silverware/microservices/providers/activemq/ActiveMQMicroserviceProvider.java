/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2010 - 2013 the original author or authors.
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

package io.silverware.microservices.providers.activemq;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.annotations.JMS;
import io.silverware.microservices.enums.ConnectionType;
import io.silverware.microservices.internal.ConnectionProvider;
import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.silver.ActiveMQSilverService;
import io.silverware.microservices.util.Utils;
import io.silverware.microservices.utils.ActiveMQConstants;

import org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public class ActiveMQMicroserviceProvider implements MicroserviceProvider, ActiveMQSilverService {

   /**
    * Logger.
    */
   private static final Logger log = LogManager.getLogger(ActiveMQMicroserviceProvider.class);

   /**
    * Microservices context.
    */
   private Context context;

   /**
    * javax.naming.Context for JNDI
    */
   private InitialContext initialContext;

   /**
    * map of already initialized instances of {@link ConnectionProvider}
    */
   private Map<String, ConnectionProvider> connectionProviders = new HashMap<>();

   @Override
   public void initialize(final Context context) {
      this.context = context;
   }

   @Override
   public void run() {
      log.info("Hello from ActiveMQ Microservice Provider!");

      //Connect to JMS via JNDI - jndi.properties file must be provided by client

      try {
         initialContext = new InitialContext();

         if (initialContext.getEnvironment().get(javax.naming.Context.INITIAL_CONTEXT_FACTORY) == null) {
            //throws NamingException if no properties file is found or it does not contain the required initial factory property
         }
      } catch (NamingException ne) {

         //if no JNDI property file was provided the provider will use by the default the ActiveMQInitialContextFactory
         //from the Artemis project

         log.info("Invalid or no JNDI configuration file found on the classpath: ", ne);
         log.info("Creating default initial context factory " + ActiveMQInitialContextFactory.class.getSimpleName() + "...");
         initialContext = createDefaultArtemisInitialContext(ActiveMQInitialContextFactory.class);
      }

      if (initialContext == null) {
         //the provider cannot run without the initial context
         log.error("Cannot run the " + this.getClass().getSimpleName() + "without the " + InitialContext.class.getSimpleName());
         return;
      }

      //try to locate the default connection factory in the JNDI and create the default connection
      initDefaultConnectionFactory();

      try {
         while (!Thread.currentThread().isInterrupted()) {
            Thread.sleep(1000);
         }
      } catch (InterruptedException ie) {
         Utils.shutdownLog(log, ie);
      } finally {
         log.info("Closing messaging platform...");

         try {
            initialContext.close();
         } catch (NamingException ne) {
            log.error("Unable to close initial context: ", ne);
         }

         //cleanup
         for (ConnectionProvider provider : connectionProviders.values()) {
            provider.close();
         }
      }

      log.info("Bye from ActiveMQ Microservice Provider!");
   }

   @Override
   public Context getContext() {
      return context;
   }

   @Override
   public Set<Object> lookupMicroservice(MicroserviceMetaData metaData) {

      ConnectionProvider provider = null;

      JMS jmsAnnotation = getJMSAnnotation(metaData.getAnnotations());

      //get or create connection provider
      provider = getJMSConnectionProvider(jmsAnnotation);

      if (provider == null) {
         log.error("Cannot create a connection: injection must contain a valid @"
                 + JMS.class.getSimpleName() + " annotation or the default connection factory " +
                 "must be specified in the JNDI configuration file");
         return Collections.emptySet();
      }

      if (Connection.class.isAssignableFrom(metaData.getType())) {

         Connection connection = getConnection(provider, jmsAnnotation);

         return Collections.singleton(connection);
      } else if (JMSContext.class.isAssignableFrom(metaData.getType())) {

         JMSContext jmsContext = getJMSContext(provider, jmsAnnotation);

         return Collections.singleton(jmsContext);
      }

      return Collections.emptySet();
   }

   private InitialContext createDefaultArtemisInitialContext(Class initialContextFactoryClass) {
      log.info("Creating default " + InitialContext.class.getSimpleName() + " with " +
              initialContextFactoryClass.getSimpleName());

      Properties properties = new Properties();
      properties.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, initialContextFactoryClass.getName());

      InitialContext result = null;

      //note that this merges the other provided JNDI elements
      try {
         result = new InitialContext(properties);
      } catch (NamingException ne) {
         log.error("Cannot create default initial context", ne);
      }

      return result;
   }

   private void initDefaultConnectionFactory() {
      //lookup the factory in JNDI config
      try {
         ConnectionFactory factory = lookupJNDIConnectionFactory();

         ConnectionProvider defaultToolkit = new ConnectionProvider(ActiveMQConstants.DEFAULT_CONNECTION, factory);

         connectionProviders.put(ActiveMQConstants.DEFAULT_CONNECTION, defaultToolkit);
      } catch (NamingException ne) {
         log.debug("No default connection factory was provided in the context");
      }
   }

   private ConnectionFactory lookupJNDIConnectionFactory() throws NamingException {
      return (ConnectionFactory) initialContext.lookup(ActiveMQConstants.CONNECTION_FACTORY_JNDI);
   }

   private ConnectionProvider getJMSConnectionProvider(JMS jmsAnnotation) {

      ConnectionProvider provider = null;

      if (jmsAnnotation != null && !jmsAnnotation.serverUri().isEmpty()) {
         String uri = jmsAnnotation.serverUri();
         provider = connectionProviders.get(uri);

         if (provider == null) {
            //create new provider
            provider = new ConnectionProvider(uri, createConnectionFactoryForURI(uri));
            connectionProviders.put(uri, provider);
         }
      } else {
         provider = connectionProviders.get(ActiveMQConstants.DEFAULT_CONNECTION);
      }

      return provider;
   }

   private Connection getConnectionForConnectionType(ConnectionProvider provider, ConnectionType type) {

      if (type.equals(ConnectionType.SHARED)) {
         return provider.getSharedConnection();
      } else if (type.equals(ConnectionType.INJECTION)) {
         return provider.createNonSharedConnection();
      }

      return null;
   }

   private Connection getAuthenticatedConnectionForConnectionType(
           ConnectionProvider provider, String userName, String password, ConnectionType type) {

      if (type.equals(ConnectionType.SHARED)) {
         return provider.getSharedConnection(userName, password);
      } else if (type.equals(ConnectionType.INJECTION)) {
         return provider.createNonSharedConnection(userName, password);
      }

      return null;
   }

   private Connection getConnection(ConnectionProvider provider, JMS jmsAnnotation) {

      if (jmsAnnotation == null) {
         //return default connection
         return getConnectionForConnectionType(provider, ConnectionType.SHARED);
      }

      String userName = jmsAnnotation.userName();
      String password = jmsAnnotation.password();
      ConnectionType type = jmsAnnotation.connectionType();

      if (userName.isEmpty()) {
         //no security infromation provided
         return getConnectionForConnectionType(provider, type);
      } else {
         return getAuthenticatedConnectionForConnectionType(provider, userName, password, type);
      }
   }

   private JMSContext getJMSContextForConnectionType(ConnectionProvider provider, ConnectionType type) {
      return getJMSContextForConnectionType(provider, type, ActiveMQConstants.DEFAULT_SESSION_TYPE);
   }

   private JMSContext getJMSContextForConnectionType(ConnectionProvider provider, ConnectionType type, int sessionMode) {

      if (type.equals(ConnectionType.SHARED)) {
         return provider.getSharedJMSContext(sessionMode);
      } else if (type.equals(ConnectionType.INJECTION)) {
         return provider.createNonSharedJMSContext(sessionMode);
      }

      return null;
   }

   private JMSContext getAuthenticatedJMSContextForConnectionType(
           ConnectionProvider provider, String userName, String password, ConnectionType type, int sessionMode) {

      if (type.equals(ConnectionType.SHARED)) {
         return provider.getSharedJMSContext(sessionMode, userName, password);
      } else if (type.equals(ConnectionType.INJECTION)) {
         return provider.createNonSharedJMSContext(sessionMode, userName, password);
      }

      return null;
   }

   private JMSContext getJMSContext(ConnectionProvider provider, JMS jmsAnnotation) {

      if (jmsAnnotation == null) {
         //return default JMSContext
         return getJMSContextForConnectionType(provider, ConnectionType.SHARED);
      }

      String userName = jmsAnnotation.userName();
      String password = jmsAnnotation.password();
      int sessionMode = jmsAnnotation.sessionMode();
      ConnectionType type = jmsAnnotation.connectionType();

      if (userName.isEmpty()) {
         //no credentials provided
         return getJMSContextForConnectionType(provider, type, sessionMode);
      } else {
         return getAuthenticatedJMSContextForConnectionType(provider, userName, password, type, sessionMode);
      }
   }

   private ConnectionFactory createConnectionFactoryForURI(String uri) {

      log.info("Creating connection factory for the URI: " + uri);
      ConnectionFactory factory = null;

      if (addPropertyToContext(ActiveMQConstants.CONNECTION_FACTORY, uri)) {
         try {
            factory = lookupJNDIConnectionFactory();
         } catch (NamingException ne) {
            log.error("Cannot create connection factory for the given URI: ", ne);
         }
      }

      return factory;
   }

   private boolean addPropertyToContext(String name, Object value) {
      try {
         Properties properties = new Properties();
         properties.putAll(initialContext.getEnvironment());
         properties.put(name, value);

         initialContext.close();
         initialContext = new InitialContext(properties);
         return true;
      } catch (NamingException ne) {
         log.error("error creating new initial context: ", ne);
      }

      return false;
   }

   private JMS getJMSAnnotation(Set<Annotation> annotations) {
      for (Annotation annotation : annotations) {
         if (JMS.class.isAssignableFrom(annotation.getClass())) {
            return (JMS) annotation;
         }
      }

      return null;
   }
}
