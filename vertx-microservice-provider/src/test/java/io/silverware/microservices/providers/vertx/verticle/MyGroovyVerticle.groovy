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