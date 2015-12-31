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
package io.silverware.microservices.providers.camel;

import io.silverware.microservices.Context;
import io.silverware.microservices.silver.CdiSilverService;
import io.silverware.microservices.util.Utils;

import org.apache.camel.CamelContext;
import org.apache.camel.cdi.internal.CamelContextMap;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Gets the Camel Context from CDI Bean Manager to make camel-cdi integration work.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class CamelCdiContextFactory implements CamelContextFactory {

   private static final Logger log = LogManager.getLogger(CamelCdiContextFactory.class);

   public CamelContext createCamelContext(final Context context) {
      CdiSilverService cdiSilverService = null;
      log.info("Camel CDI extension is present, waiting for 5s for CDI initialization...");

      // first lookup the cdi silverservice
      int i = 0;
      while (cdiSilverService == null && i++ < 10) {
         cdiSilverService = (CdiSilverService) context.getProvider(CdiSilverService.class);
         Utils.sleep(500);
      }

      if (cdiSilverService != null) {
         log.info("Got CDI SilverService, waiting for 5s for its full deployment...");

         // now wait for it to complete deployment
         i = 0;
         while (!cdiSilverService.isDeployed() && i++ < 10) {
            Utils.sleep(500);
         }

         if (cdiSilverService.isDeployed()) {
            log.info("CDI SilverService connected successfully!");

            CamelContextMap camelContextMap = cdiSilverService.findByType(CamelContextMap.class);
            return camelContextMap.getCamelContext("camel-1");
         } else {
            log.warn("CDI deployment took to long, trying to continue.");
         }
      } else {
         log.warn("Failed to find CDI SilverService, trying to continue.");
      }

      return new DefaultCamelContext();
   }

}
