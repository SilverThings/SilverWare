package io.silverware.microservices.enums;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public enum ConnectionType {
   /**
    * represents the one {@link javax.jms.Connection} or {@link javax.jms.JMSContext}
    * shared between all users for the given URI
    */
   SHARED,

   /**
    * represents creation of new {@link javax.jms.Connection} or {@link javax.jms.JMSContext}
    * for the injection point
    */
   INJECTION
}
