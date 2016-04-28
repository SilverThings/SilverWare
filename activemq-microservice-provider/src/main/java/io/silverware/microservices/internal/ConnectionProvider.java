package io.silverware.microservices.internal;

import io.silverware.microservices.utils.ActiveMQConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSRuntimeException;

/**
 * This class provides the connections for the JMS 1.1 and the JMS 2.0 endpoints
 *
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public class ConnectionProvider {

   private static final Logger log = LogManager.getLogger(ConnectionProvider.class);

   private String uri;
   private ConnectionFactory connectionFactory;

   //JMS 1.1
   /**
    * shared connection with no security settings
    */
   private Connection sharedConnection;

   /**
    * map of shared connections with provided security credentials
    */
   private Map<JMSCredentials, Connection> sharedSecuredConnections = new HashMap<>();

   /**
    * secured and not secured connection inserted with INJECTION {@link io.silverware.microservices.enums.ConnectionType}
    */
   private Set<Connection> nonSharedConnections = new HashSet<>();

   //JMS 2.0
   /**
    * map of available shared {@link JMSContext} for the given session mode
    */
   private Map<Integer, JMSContext> sharedSessionModeJMSContexts = new HashMap<>();

   /**
    * map of shared {@link JMSContext} instances with security credentials
    */
   private Map<JMSCredentials, JMSContext> sharedSecuredJMSContexts = new HashMap<>();

   /**
    * set of non-shared instances of {@link JMSContext}
    */
   private Set<JMSContext> nonSharedJMSContexts = new HashSet<>();


   public ConnectionProvider(String uri, ConnectionFactory connectionFactory) {
      if (uri == null) {
         throw new IllegalArgumentException("Connection URI cannot be null");
      }

      if (connectionFactory == null) {
         throw new IllegalArgumentException("Connection Factory cannot be null");
      }

      this.uri = uri;
      this.connectionFactory = connectionFactory;
   }

   public String getUri() {
      return uri;
   }

   /**
    * Sets the uri
    *
    * @param uri the new uri, not null
    */
   public void setUri(String uri) {
      if (uri == null) {
         throw new IllegalArgumentException("Connection URI cannot be null");
      }
      this.uri = uri;
   }

   public ConnectionFactory getConnectionFactory() {
      return connectionFactory;
   }

   /**
    * Sets the connection factory
    *
    * @param connectionFactory new connection factory, not null
    */
   public void setConnectionFactory(ConnectionFactory connectionFactory) {
      if (connectionFactory == null) {
         throw new IllegalArgumentException("Connection Factory cannot be null");
      }
      this.connectionFactory = connectionFactory;
   }

   /**
    * Returns or create the default {@link Connection}
    *
    * @return returns the default {@link Connection}
    */
   public Connection getSharedConnection() {
      if (sharedConnection == null) {
         sharedConnection = createConnection();
      }
      return sharedConnection;
   }

   /**
    * Returns or create the shared {@link Connection} with security settings
    *
    * @param userName user name
    * @param password user password
    * @return returns the shared {@link Connection} with security settings
    */
   public Connection getSharedConnection(String userName, String password) {
      Connection result = null;
      final JMSCredentials credentials = new JMSCredentials(userName, password);

      if (sharedSecuredConnections.containsKey(credentials)) {
         result = sharedSecuredConnections.get(credentials);
      } else {
         result = createSecuredConnection(userName, password);

         if (result != null) {
            sharedSecuredConnections.put(credentials, result);
         }
      }

      return result;
   }

   /**
    * Returns the shared {@link JMSContext} for the given session mode
    *
    * @param sessionMode session mode, should be one of the {@link JMSContext} constants
    * @return the shared {@link JMSContext} for the given session mode
    */
   public JMSContext getSharedJMSContext(Integer sessionMode) {
      if (sharedSessionModeJMSContexts.containsKey(sessionMode)) {
         return sharedSessionModeJMSContexts.get(sessionMode);
      }

      JMSContext jmsContext = null;

      jmsContext = createJMSContext(sessionMode);

      if (jmsContext == null) {
         log.error("Invalid argument for the session type");
         return null;
      }

      sharedSessionModeJMSContexts.put(sessionMode, jmsContext);
      return jmsContext;
   }

   /**
    * Return the secured shared {@link JMSContext} for the given session mode and user credentials
    *
    * @param sessionMode session mode, should be one of the {@link JMSContext} constants
    * @param userName    user name
    * @param password    user password
    * @return the {@link JMSContext} for the given session mode and security settings
    */
   public JMSContext getSharedJMSContext(Integer sessionMode, String userName, String password) {

      JMSContext result = null;
      JMSCredentials credentials = new JMSCredentials(userName, password, sessionMode);

      if (sharedSecuredJMSContexts.containsKey(credentials)) {
         result = sharedSecuredJMSContexts.get(credentials);
      } else {
         result = createSecuredJMSContext(sessionMode, userName, password);

         if (result != null) {
            sharedSecuredJMSContexts.put(credentials, result);
         }
      }

      return result;
   }

   /**
    * Creates a non-shared {@link Connection} for individual injection
    *
    * @return created non-shared {@link Connection}
    */
   public Connection createNonSharedConnection() {

      Connection connection = createConnection();

      if (connection != null) {
         nonSharedConnections.add(connection);
         return connection;
      }

      return null;
   }

   /**
    * Creates a non-shared {@link Connection} for individual injection with security settings
    *
    * @param userName user name
    * @param password user password
    * @return created secured non-shared {@link Connection}
    */
   public Connection createNonSharedConnection(String userName, String password) {

      Connection connection = createSecuredConnection(userName, password);

      if (connection != null) {
         nonSharedConnections.add(connection);
         return connection;
      }

      return null;
   }

   /**
    * Returns an instance of non-shared {@link JMSContext} for the given session mode
    *
    * @param sessionMode session mode, should be one of the {@link JMSContext} constants
    * @return non-shared intance of {@link JMSContext} with selected session mode
    */
   public JMSContext createNonSharedJMSContext(int sessionMode) {

      JMSContext jmsContext = createJMSContext(sessionMode);

      if (jmsContext != null) {
         nonSharedJMSContexts.add(jmsContext);
      }

      return jmsContext;
   }

   /**
    * Returns the non-shared instance of {@link JMSContext} with security identity
    *
    * @param sessionMode session mode, should be one of the {@link JMSContext} constants
    * @param userName    user name
    * @param password    user password
    * @return instance of non-shared secured instance of {@link JMSContext}
    */
   public JMSContext createNonSharedJMSContext(int sessionMode, String userName, String password) {

      JMSContext jmsContext = createSecuredJMSContext(sessionMode, userName, password);

      if (jmsContext != null) {
         nonSharedJMSContexts.add(jmsContext);
      }

      return jmsContext;
   }

   /**
    * Closes all of the resources created by this provider
    */
   public void close() {
      log.info("Closing JMS connection provider: " + (uri.isEmpty() ? "default JNDI connection" : uri) + "...");

      try {
         if (sharedConnection != null) {
            sharedConnection.close();
         }

         for (Connection con : nonSharedConnections) {
            con.close();
         }

         for (Connection secureConnection : sharedSecuredConnections.values()) {
            secureConnection.close();
         }

         for (JMSContext sharedContext : sharedSessionModeJMSContexts.values()) {
            if (sharedContext != null) {
               sharedContext.close();
            }
         }

         for (JMSContext securedContext : sharedSecuredJMSContexts.values()) {
            securedContext.close();
         }

         for (JMSContext jmsContext : nonSharedJMSContexts) {
            jmsContext.close();
         }
      } catch (JMSException | JMSRuntimeException jmse) {
         log.error("Unable to close toolkit " + getUri() + ": ", jmse);
      }
   }

   private Connection createConnection() {
      try {
         Connection connection = connectionFactory.createConnection();
         connection.start();
         return connection;
      } catch (JMSException jmse) {
         log.error("Cannot create or start the connection: ", jmse);
      }

      return null;
   }

   private Connection createSecuredConnection(String userName, String password) {
      try {
         Connection connection = connectionFactory.createConnection(userName, password);
         connection.start();
         return connection;
      } catch (JMSException jmse) {
         log.error("Cannot create or start secure connection for user: " + userName);
      }

      return null;
   }

   private JMSContext createJMSContext(int sessionMode) {
      JMSContext jmsContext = null;

      try {
         if (sessionMode == ActiveMQConstants.DEFAULT_SESSION_TYPE) {
            jmsContext = connectionFactory.createContext();
         } else {
            jmsContext = connectionFactory.createContext(sessionMode);
         }

         jmsContext.start();

         return jmsContext;
      } catch (JMSRuntimeException jmsre) {
         log.error("Cannot create or start the JMSContext: ", jmsre);
      }

      return jmsContext;
   }

   private JMSContext createSecuredJMSContext(int sessionMode, String userName, String password) {
      JMSContext jmsContext = null;

      try {
         if (sessionMode == ActiveMQConstants.DEFAULT_SESSION_TYPE) {
            jmsContext = connectionFactory.createContext(userName, password);
         } else {
            jmsContext = connectionFactory.createContext(userName, password, sessionMode);
         }
      } catch (JMSRuntimeException jmsre) {
         log.error("Cannot create or start the secure JMSContext: ", jmsre);
      }

      return jmsContext;
   }

   private static class JMSCredentials {

      private String name;
      private String password;
      private int sessionMode;

      public JMSCredentials(String name, String password) {
         this.name = name;
         this.password = password;
         this.sessionMode = ActiveMQConstants.DEFAULT_SESSION_TYPE;
      }

      public JMSCredentials(String name, String password, int sessionMode) {
         this.name = name;
         this.password = password;
         this.sessionMode = sessionMode;
      }

      public String getName() {
         return name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public String getPassword() {
         return password;
      }

      public void setPassword(String password) {
         this.password = password;
      }

      public int getSessionMode() {
         return sessionMode;
      }

      public void setSessionMode(int sessionMode) {
         this.sessionMode = sessionMode;
      }

      @Override
      public boolean equals(Object o) {
         if (o == null) {
            return false;
         }
         if (this == o) {
            return true;
         }

         if (!(o instanceof JMSCredentials)) {
            return false;
         }

         final JMSCredentials that = (JMSCredentials) o;

         if (!name.equals(that.getName())) {
            return false;
         }

         if (!password.equals(that.getPassword())) {
            return false;
         }

         return sessionMode == that.getSessionMode();
      }

      @Override
      public int hashCode() {
         int result = name != null ? name.hashCode() : 0;
         result = 31 * result + (password != null ? password.hashCode() : 0);
         result = 31 * result + sessionMode;
         return result;
      }
   }
}
