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
package io.silverware.microservices.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * Generic utilities.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class Utils {

   /**
    * Logger.
    */
   private static final Logger log = LogManager.getLogger(Utils.class);

   /**
    * Maximum number of attempts to wait for an URL to become available.
    */
   private static final int MAX_HTTP_TRIES = 60;

   /**
    * Logs a shutdown message with the given exception.
    *
    * @param log A logger where to log the message to.
    * @param ie  An exception causing the shutdown.
    */
   public static void shutdownLog(final Logger log, final InterruptedException ie) {
      log.info("Execution interrupted, exiting.");
      if (log.isTraceEnabled()) {
         log.trace("Interrupted from: ", ie);
      }
   }

   /**
    * Waits for the URL to become available.
    *
    * @param urlString The URL to check for.
    * @param code      The expected HTTP response code.
    * @return Returns true if the URL was available, false otherwise.
    * @throws Exception When it was not possible to check the URL.
    */
   public static boolean waitForHttp(String urlString, int code) throws Exception {
      final URL url = new URL(urlString);
      int lastCode = -1;

      for (int i = 0; i < MAX_HTTP_TRIES; ++i) {
         try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            lastCode = conn.getResponseCode();

            if (lastCode == code) {
               conn.disconnect();
               return true;
            }
            conn.disconnect();
         } catch (IOException x) {
            // Continue waiting
         }

         Thread.sleep(1000);
      }

      return false;
   }

   /**
    * Completely reads the content of the given URL as a string.
    *
    * @param urlString The URL to read from.
    * @return The content of the given URL.
    * @throws IOException When it was not possible to read from the URL.
    */
   public static String readFromUrl(String urlString) throws IOException {
      return new Scanner(new URL(urlString).openStream(), "UTF-8").useDelimiter("\\A").next();
   }

   /**
    * Gets the manifest entry for the given class.
    *
    * @param clazz     The class I want to obtain entry for.
    * @param entryName The name of the entry to obtain.
    * @return The entry from manifest, null if there is no such entry or the manifest file does not exists.
    * @throws IOException When it was not possible to get the manifest file.
    */
   public static String getManifestEntry(final Class clazz, final String entryName) throws IOException {
      Enumeration<URL> resources = clazz.getClassLoader().getResources("META-INF/MANIFEST.MF");
      while (resources.hasMoreElements()) {
         try (final InputStream is = resources.nextElement().openStream()) {
            Manifest manifest = new Manifest(is);
            Attributes attr = manifest.getMainAttributes();
            String value = attr.getValue(entryName);
            if (value != null && value.length() > 0) {
               return value;
            }
         }
      }

      return null;
   }


   /**
    * Do the best to sleep for the given time. Ignores {@link InterruptedException}.
    *
    * @param ms The number of milliseconds to sleep for.
    */
   public static void sleep(final long ms) {
      try {
         Thread.sleep(ms);
      } catch (InterruptedException ie) {
         // ignored
      }
   }
}
