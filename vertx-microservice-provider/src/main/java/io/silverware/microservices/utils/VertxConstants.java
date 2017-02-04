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
