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
package org.silverware.microservices.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class Utils {

   private static final Logger log = LogManager.getLogger(Utils.class);

   private static final int MAX_HTTP_TRIES = 60;

   public static void shutdownLog(final Logger log, final InterruptedException ie) {
      log.info("Execution interrupted, exiting.");
      if (log.isTraceEnabled()) {
         log.trace("Interrupted from: ", ie);
      }
   }

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

   public static String readFromUrl(String urlString) throws IOException {
      return new Scanner(new URL(urlString).openStream(), "UTF-8").useDelimiter("\\A").next();
   }

}