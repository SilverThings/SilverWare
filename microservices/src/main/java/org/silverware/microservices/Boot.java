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
package org.silverware.microservices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silverware.microservices.util.Utils;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public final class Boot {

   private static final Logger log = LogManager.getLogger(Boot.class);

   public static void main(final String... args) {
      log.info("=== Welcome to SilverWare ===");

      try {
         final Thread bootThread = new Thread(new Executor());
         bootThread.start();
         bootThread.join();
      } catch (InterruptedException ie) {
         Utils.shutdownLog(log, ie);
      }

      log.info("Goodbye.");
   }
}
