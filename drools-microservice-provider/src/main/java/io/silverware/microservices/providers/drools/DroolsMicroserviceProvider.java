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
import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.silver.DroolsSilverService;
import io.silverware.microservices.util.DeployStats;
import io.silverware.microservices.util.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class DroolsMicroserviceProvider implements MicroserviceProvider, DroolsSilverService {

   private static final Logger log = LogManager.getLogger(DroolsMicroserviceProvider.class);

   private Context context;

   private Map<String, KieSession> sessions = new HashMap<>();
   private KieContainer container;

   private void createDroolsSession() {
      final KieServices ks = KieServices.Factory.get();
      container = ks.getKieClasspathContainer();
      context.getProperties().put(DROOLS_CONTAINER, container);
   }

   @Override
   public void initialize(final Context context) {
      this.context = context;
   }

   @Override
   public Context getContext() {
      return context;
   }

   @Override
   public void run() {
      try {
         log.info("Hello from Drools microservice provider!");

         //createDroolsSession();

         try {
            while (!Thread.currentThread().isInterrupted()) {
               Thread.sleep(1000);
            }
         } catch (InterruptedException ie) {
            Utils.shutdownLog(log, ie);
         } finally {
            sessions.forEach((k, v) -> v.dispose());
         }
      } catch (Exception e) {
         log.error("Drools microservice provider failed: ", e);
      }
   }

   @Override
   public Set<Object> lookupMicroservice(final MicroserviceMetaData metaData) {
      if (KieSession.class.isAssignableFrom(metaData.getType())) {
         String name = metaData.getName();

         if (name == null || name.isEmpty()) {
            return Collections.singleton(container.newKieSession());
         } else {
            return Collections.singleton(sessions.computeIfAbsent(name, newName -> container.newKieSession(newName)));
         }
      }

      return new HashSet<>();
   }

}
