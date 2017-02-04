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
package io.silverware.microservices.providers.vertx.verticle

import io.vertx.core.logging.LoggerFactory
import io.vertx.groovy.core.http.HttpServer
import io.vertx.lang.groovy.GroovyVerticle

public class MyGroovyVerticle extends GroovyVerticle {

   def logger = LoggerFactory.getLogger(this.class.getName())
   def testClass = Class.forName("io.silverware.microservices.providers.vertx.VerticleDeploymentTest")
   private HttpServer server;

   public void start() {
      logger.info("Starting " + this.class.getName())
      server = vertx.createHttpServer().requestHandler({ request ->
         request.response().setStatusCode(200)
                 .putHeader("semaphore", "release").end("response")
      })

      server.listen(8083, "localhost", { res ->
         if (res.succeeded()) {
            logger.info("Server established on http://localhost:8083")
            testClass.serverEstablished()
         } else {
            logger.error("Error initializing server: " + res.cause())
         }
      })
   }

   public void stop() {
      logger.info("Stopping" + this.class.getName())
      server.close()
   }

}