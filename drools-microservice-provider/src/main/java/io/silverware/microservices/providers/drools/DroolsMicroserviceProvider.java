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
package io.silverware.microservices.providers.drools;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.SilverWareException;
import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.silver.DroolsSilverService;
import io.silverware.microservices.util.DeployStats;
import io.silverware.microservices.util.DeploymentScanner;
import io.silverware.microservices.util.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class DroolsMicroserviceProvider implements MicroserviceProvider, DroolsSilverService {

   private static final Logger log = LogManager.getLogger(DroolsMicroserviceProvider.class);

   private Context context;

   private final DeployStats stats = new DeployStats();

   private void createDroolsSession() throws SilverWareException {
      throw new SilverWareException("Unimplemented");
   }

   private void loadRoutesFromXml() {
      // routeResources = DeploymentScanner.getContextInstance(context).lookupResources(".*camel-.*\\.xml");
   }

   @Override
   public void initialize(final Context context) {
      this.context = context;

      loadRoutesFromXml();
   }

   @Override
   public Context getContext() {
      return context;
   }

   @Override
   public void run() {
      //if (routes.size() > 0 || routeResources.size() > 0) {
         try {
            log.info("Hello from Drools microservice provider!");

            createDroolsSession();

            //log.info("Total Camel routes " + stats.toString() + ".");

            //camelContext.start();

            try {
               while (!Thread.currentThread().isInterrupted()) {
                  Thread.sleep(1000);
               }
            } catch (InterruptedException ie) {
               Utils.shutdownLog(log, ie);
            } finally {
               //camelContext.stop();
            }
         } catch (Exception e) {
            log.error("Drools microservice provider failed: ", e);
         }
      //} else {
      //   log.warn("No KIE Jars to deploy. Drools microservice provider is terminated.");
      //}
   }

   @Override
   public Set<Object> lookupMicroservice(final MicroserviceMetaData metaData) {
      return new HashSet<>();
   }

}
