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
package io.silverware.microservices.providers.vertx.verticle;

import io.silverware.microservices.providers.vertx.VerticleDeploymentTest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public class TestVerticle extends AbstractVerticle {

   private static final Logger log = LogManager.getLogger(TestVerticle.class);

   private HttpServer server;

   @Override
   public void start() {
      log.info("TestVerticle starting...");

      String host = "localhost";
      int port = 8082;

      server = vertx.createHttpServer(new HttpServerOptions().setHost(host).setPort(port))
              .requestHandler(req -> req.response().setStatusCode(200)
                      .putHeader("semaphore", "release").end("response"));
      server.listen(res -> {
         if (res.succeeded()) {
            log.info("Server established on http://" + host + ":" + port);
            VerticleDeploymentTest.serverEstablished();
         } else {
            log.error("Failed to bind");
         }
      });

   }

   @Override
   public void stop() throws Exception {
      log.info("TestVerticle stopping.");

      server.close();
   }
}
