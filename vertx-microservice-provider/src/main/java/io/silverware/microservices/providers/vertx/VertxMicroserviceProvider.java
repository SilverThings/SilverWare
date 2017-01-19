/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2016 the original author or authors.
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

package io.silverware.microservices.providers.vertx;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.annotations.Deployment;
import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.silver.VertxSilverService;
import io.silverware.microservices.util.DeploymentScanner;
import io.silverware.microservices.util.Utils;
import io.silverware.microservices.utils.VertxConstants;
import io.silverware.microservices.utils.VertxUtils;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.lang.groovy.GroovyVerticle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public class VertxMicroserviceProvider implements MicroserviceProvider, VertxSilverService {

   /**
    * Logger.
    */
   private static final Logger log = LogManager.getLogger(VertxMicroserviceProvider.class);

   /**
    * Microservices context.
    */
   private Context context;

   /**
    * Vertx platform instance
    */
   private Vertx vertx;

   /**
    * Set of verticles to be deployed
    */
   private Map<String, DeploymentOptions> verticles = new HashMap<>();


   @Override
   public void initialize(final Context context) {
      this.context = context;

      loadVerticlesFromCP();
      loadVerticlesFromXml();

      if (log.isDebugEnabled()) {
         log.debug(verticles.size() + (verticles.size() == 1 ? " verticle" : " verticles") + " loaded");
      }
   }

   @Override
   public void run() {
      log.info("Hello from Vert.x Microservice Provider!");

      vertx = Vertx.vertx();

      deployVerticles(vertx);

      try {
         while (!Thread.currentThread().isInterrupted()) {
            Thread.sleep(1000);
         }
      } catch (InterruptedException ie) {
         Utils.shutdownLog(log, ie);
      } finally {
         log.info("Closing Vert.x...");
         vertx.close();
      }

      log.info("Bye from Vert.x Microservice Provider!");
   }

   @Override
   public Context getContext() {
      return context;
   }

   @Override
   public Set<Object> lookupMicroservice(MicroserviceMetaData metaData) {
      if (Vertx.class.isAssignableFrom(metaData.getType())) {
         return Collections.singleton(vertx);
      }

      return new HashSet<>();
   }


   private void loadVerticlesFromCP() {
      if (log.isDebugEnabled()) {
         log.debug("Loading verticles from classpath...");
      }

      loadJavaVerticles();
      loadGroovyVerticles();
   }

   private void loadJavaVerticles() {
      Set<Class<? extends Verticle>> verticleClasses = DeploymentScanner.getContextInstance(context).lookupSubtypes(Verticle.class);

      for (Class<? extends Verticle> verticleClass : verticleClasses) {

         Deployment deployment = verticleClass.getAnnotation(Deployment.class);

         if (VertxUtils.getForbiddenVerticles().contains(verticleClass.getName())
                 || (deployment != null && !deployment.value())) {
            continue;
         }

         verticles.put(verticleClass.getName(), VertxUtils.getDeploymentOptionsFromAnnotation(deployment));
      }
   }

   private void loadGroovyVerticles() {
      Set<Class<? extends GroovyVerticle>> groovyVerticleClasses = DeploymentScanner.getContextInstance(context).lookupSubtypes(GroovyVerticle.class);

      for (Class<? extends GroovyVerticle> groovyVerticleClass : groovyVerticleClasses) {

         Deployment deployment = groovyVerticleClass.getAnnotation(Deployment.class);

         if (VertxUtils.getForbiddenVerticles().contains(groovyVerticleClass.getName())
                 || (deployment != null && !deployment.value())) {
            continue;
         }

         //set the prefix to let the Vertx know to use the groovy verticle factory
         verticles.put(VertxConstants.GROOVY_FACTORY_PREFIX + groovyVerticleClass.getName(),
                 VertxUtils.getDeploymentOptionsFromAnnotation(deployment));
      }
   }

   private void loadVerticlesFromXml() {
      if (log.isDebugEnabled()) {
         log.debug("Loading verticles from XML configuration file...");
      }

      NodeList verticleNodes = VertxUtils.getVerticlesFromXML(VertxConstants.VERTX_CONFIG_XML);
      if (verticleNodes == null) {
         return;
      }

      for (int i = 0; i < verticleNodes.getLength(); i++) {

         Node verticle = verticleNodes.item(i);
         String verticleName = verticle.getTextContent().trim();

         if (VertxUtils.getForbiddenVerticles().contains(verticleName)) {
            continue;
         }

         verticles.put(verticleName, VertxUtils.getDeploymentOptionsFromXml(verticle));
      }
   }

   private void deployVerticles(Vertx vertx) {
      for (Map.Entry<String, DeploymentOptions> verticle : verticles.entrySet()) {

         String verticleName = verticle.getKey();
         DeploymentOptions deploymentOptions = verticle.getValue();

         //deploy verticle to vertx platform
         if (log.isDebugEnabled()) {
            log.debug("Deploying " + verticleName + " with options " + VertxUtils.printDeploymentOptions(deploymentOptions));
         }

         //asynchronous handler for monitoring of the deployment process
         vertx.deployVerticle(verticleName, deploymentOptions, res -> {
            if (res.succeeded()) {
               if (log.isDebugEnabled()) {
                  log.debug("Deployment of the verticle \"" + verticleName + "\" was successful.");
               }
            } else {
               if (log.isDebugEnabled()) {
                  log.debug("Cannot deploy \"" + verticleName + "\", cause: " + res.cause());
               }
            }
         });
      }
   }

}
