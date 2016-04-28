package io.silverware.microservices.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public class InitialContextProvider {

   /**
    * Logger.
    */
   private static final Logger log = LogManager.getLogger(InitialContextProvider.class);

   /**
    * Creates a new {@link InitialContext} for ActiveMQ server with the selected
    * initial context factory class
    *
    * @param initialContextFactoryClass initial context factory class
    * @return new {@link InitialContext} for the given class
    */
   public static InitialContext createInitialContext(Class initialContextFactoryClass) {
      log.info("Creating the " + InitialContext.class.getSimpleName() + " with " +
              initialContextFactoryClass.getSimpleName());

      Properties properties = new Properties();
      properties.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY, initialContextFactoryClass.getName());

      InitialContext result = null;

      try {
         result = new InitialContext(properties);
      } catch (NamingException ne) {
         log.error("Cannot create the initial context", ne);
      }

      return result;
   }

   /**
    * Creates a new {@link InitialContext} as a copy of the given context
    *
    * @param other {@link InitialContext} to copy
    * @return new {@link InitialContext} as a copy of the provided one
    */
   public static InitialContext createInitialContext(final InitialContext other) {
      InitialContext result = null;

      try {
         result = new InitialContext(other.getEnvironment());
      } catch (NamingException ne) {
         log.error("Cannot create the initial context", ne);
      }

      return result;
   }
}
