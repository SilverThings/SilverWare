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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import io.silverware.microservices.util.Utils;

import java.util.Map;

/**
 * Main class to boot the Microservices platforms.
 *
 * @author Martin Večeřa <marvenec@gmail.com>
 */
@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "DM_EXIT", justification = "This class is allowed to terminate the JVM.")
public final class Boot {

   private static final Logger log = LogManager.getLogger(Boot.class);

   private static final String PROPERTY_LETTER = "D";

   /**
    * Starts the Microservices platform.
    *
    * Uses Executor Microservice Provider as a boot hook.
    *
    * @param args Any additional properties can be specified at the command line via -Dprop=value.
    */
   public static void main(final String... args) {
      Thread.currentThread().setName(Executor.THREAD_PREFIX + Executor.MAIN_THREAD);

      log.info("=== Welcome to SilverWare ===");

      try {
         Executor.bootHook(getInitialContext(args));
      } catch (InterruptedException ie) {
         Utils.shutdownLog(log, ie);
      }

      log.info("Goodbye.");
      logFlush(); // this is needed for Ctrl+C termination
   }

   /**
    * Flushes all appenders and terminate logging.
    */
   private static void logFlush() {
      ((LoggerContext) LogManager.getContext()).stop();
   }

   /**
    * Creates initial context pre-filled with system properties and command line arguments.
    * @param args Command line arguments.
    * @return Initial context pre-filled with system properties and command line arguments.
    */
   @SuppressWarnings("static-access")
   private static Context getInitialContext(final String... args) {
      final Context context = new Context();
      final Map<String, Object> contextProperties = context.getProperties();
      final Options options = new Options();
      final CommandLineParser commandLineParser = new GnuParser();

      System.getProperties().forEach((key, value) -> contextProperties.put((String) key, value));

      options.addOption(OptionBuilder.withArgName("property=value").hasArgs(2).withValueSeparator().withDescription("system properties").create(PROPERTY_LETTER));

      try {
         final CommandLine commandLine = commandLineParser.parse(options, args);
         commandLine.getOptionProperties(PROPERTY_LETTER).forEach((key, value) -> contextProperties.put((String) key, value));
      } catch (ParseException pe) {
         log.error("Cannot parse arguments: ", pe);
         System.exit(1);
      }

      context.getProperties().put(Executor.SHUTDOWN_HOOK, "true");

      return context;
   }
}
