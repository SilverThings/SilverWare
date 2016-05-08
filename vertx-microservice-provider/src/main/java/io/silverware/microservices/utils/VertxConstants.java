package io.silverware.microservices.utils;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public abstract class VertxConstants {

   public static final String NOT_AVAILABLE = "N/A";
   public static final String FORBIDDEN_VERTICLES = "forbidden-verticles.xml";
   public static final String VERTX_CONFIG_XML = "vertx-config.xml";
   public static final String VERTX_SCHEMA = "vertx-config-schema.xsd";

   //xml configuration attributes
   public static final String TYPE = "type";
   public static final String INSTANCES = "instances";
   public static final String ISOLATION_GROUP = "isolationGroup";
   public static final String ISOLATED_CLASSES = "isolatedClasses";
   public static final String EXTRA_CLASSPATH = "extraClasspath";
   public static final String HA = "ha";
   public static final String CONFIG = "config";
   public static final String TYPE_WORKER = "worker";
   public static final String TYPE_MULTI_THREADED_WORKER = "multi-threaded-worker";

   //groovy support
   public static final String GROOVY_FACTORY_PREFIX = "groovy:";

}
