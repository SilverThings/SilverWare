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
package io.silverware.microservices;

import static io.silverware.microservices.Executor.SHUTDOWN_HOOK;

import io.silverware.microservices.util.Utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Main class to boot the Microservices platforms.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "DM_EXIT", justification = "This class is allowed to terminate the JVM.")
public final class Boot {

   private static final Logger log = LogManager.getLogger(Boot.class);

   private static final String PROPERTY_LETTER = "D";
   private static final String PROPERTY_FILE_LETTER = "p";

   /**
    * Starts the Microservices platform.
    *
    * Uses Executor Microservice Provider as a boot hook.
    *
    * @param args
    *       Any additional properties can be specified at the command line via -Dprop=value.
    */
   public static void main(final String... args) {
      preMainConfig();

      log.info("=== Welcome to SilverWare ===");
      final Context initialContext = getInitialContext(args);
      try {
         Executor.bootHook(initialContext);
      } catch (InterruptedException ie) {
         Utils.shutdownLog(log, ie);
      }

      log.info("Goodbye.");
      logFlush(); // this is needed for Ctrl+C termination
      // if we had clean up everything. Why should we end with non-zero code?
      if (Boolean.parseBoolean(String.valueOf(initialContext.getProperties().get(SHUTDOWN_HOOK)))) {
         System.exit(0);
      }
   }

   /**
    * Performs some quick system settings for a smooth run.
    */
   private static void preMainConfig() {
      System.setProperty("java.net.preferIPv4Stack", "true");
      Thread.currentThread().setName(Executor.THREAD_PREFIX + Executor.MAIN_THREAD);
   }

   /**
    * Flushes all appenders and terminate logging.
    */
   private static void logFlush() {
      ((LoggerContext) LogManager.getContext()).stop();
   }

   /**
    * Load custom properties from filepath
    * @param propertiesFile file with properties which will be loaded
    * @return Properties from given file path
    */

   private static Properties loadProperties(final File propertiesFile) {
      log.info("Loading configuration from file {}.", propertiesFile.getAbsolutePath());
      Properties props = new Properties();
      try (FileInputStream fis = new FileInputStream(propertiesFile)) {
         props.load(fis);
      } catch (IOException ioe) {
         log.warn("Cannot read configuration property file {}.", propertiesFile.getAbsolutePath());
      }

      return props;
   }

   /**
    * Creates initial context pre-filled with system properties, command line arguments, custom property file and default property file.
    *
    * @param args
    *       Command line arguments.
    * @return Initial context pre-filled with system properties, command line arguments, custom property file and default property file.
    */
   @SuppressWarnings("static-access")
   private static Context getInitialContext(final String... args) {
      final Context context = new Context();
      final Map<String, Object> contextProperties = context.getProperties();
      final Options options = new Options();
      final CommandLineParser commandLineParser = new DefaultParser();

      System.getProperties().forEach((key, value) -> contextProperties.put((String) key, value));

      options.addOption(Option.builder(PROPERTY_LETTER).argName("property=value").numberOfArgs(2).valueSeparator().desc("system properties").build());
      options.addOption(Option.builder(PROPERTY_FILE_LETTER).longOpt("properties").desc("Custom property file").hasArg().argName("PROPERTY_FILE").build());

      try {
         final CommandLine commandLine = commandLineParser.parse(options, args);
         commandLine.getOptionProperties(PROPERTY_LETTER).forEach((key, value) -> contextProperties.put((String) key, value));

         // process custom properties file
         if (commandLine.hasOption(PROPERTY_FILE_LETTER)) {
            final File propertiesFile = new File(commandLine.getOptionValue(PROPERTY_FILE_LETTER));
            if (propertiesFile.exists()) {
               final Properties props = loadProperties(propertiesFile);
               props.forEach((key, val) -> contextProperties.putIfAbsent(key.toString(), val));
            } else {
               log.error("Specified property file {} does not exists.", propertiesFile.getAbsolutePath());
            }
         }
      } catch (ParseException pe) {
         log.error("Cannot parse arguments: ", pe);
         new HelpFormatter().printHelp("SilverWare usage:", options);
         System.exit(1);
      }
      contextProperties.putIfAbsent(SHUTDOWN_HOOK, "true");
      return context;
   }
}
