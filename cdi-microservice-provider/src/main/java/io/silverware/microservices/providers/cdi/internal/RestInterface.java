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
package io.silverware.microservices.providers.cdi.internal;

import io.silverware.microservices.Context;
import io.silverware.microservices.silver.CdiSilverService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.inject.spi.Bean;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class RestInterface extends AbstractVerticle {

   /**
    * Logger.
    */
   private static final Logger log = LogManager.getLogger(RestInterface.class);

   final private int port;
   final private String host;
   private Vertx vertx;

   private Map<String, Bean> gatewayRegistry = new HashMap<>();

   public RestInterface(final Context context) {
      port = Integer.valueOf(context.getProperties().getOrDefault(CdiSilverService.CDI_REST_PORT, "8081").toString());
      host = context.getProperties().getOrDefault(CdiSilverService.CDI_REST_HOST, "").toString();
   }

   public void registerGateway(final String serviceName, final Bean bean) {
      gatewayRegistry.put(serviceName, bean);
   }

   public void deploy() {
      if (gatewayRegistry.size() > 0) {
         VertxOptions vertxOptions = new VertxOptions().setWorkerPoolSize(100);
         vertx = Vertx.vertx(vertxOptions);
         DeploymentOptions deploymentOptions = new DeploymentOptions().setWorker(true);
         vertx.deployVerticle(this, deploymentOptions);
      }
   }

   public void undeploy() {
      if (vertx != null) {
         vertx.close();
      }
   }

   @Override
   public void start() throws Exception {
      Router router = Router.router(vertx);
      router.get("/rest").handler(this::listBeans);
      router.get("/rest/:microservice").handler(this::listMethods);
      router.post("/rest/:microservice/:method").handler(this::callMethod);
      router.get("/rest/:microservice/:method").handler(this::callNoParamMethod);

      HttpServerOptions options = new HttpServerOptions().setAcceptBacklog(1000);
      HttpServer server = vertx.createHttpServer(options).requestHandler(router::accept);
      if (host.isEmpty()) {
         server.listen(port);
      } else {
         server.listen(port, host);
      }
   }

   public void listBeans(final RoutingContext routingContext) {
      JsonArray beans = new JsonArray();

      gatewayRegistry.keySet().forEach(beans::add);

      routingContext.response().end(beans.encodePrettily());
   }

   public void listMethods(final RoutingContext routingContext) {
      String microserviceName = routingContext.request().getParam("microservice");
      Bean bean = gatewayRegistry.get(microserviceName);

      if (bean == null) {
         routingContext.response().setStatusCode(503).end("Resource not available");
      } else {
         JsonArray methods = new JsonArray();

         for (final Method m : bean.getBeanClass().getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers())) {
               JsonObject method = new JsonObject();
               method.put("methodName", m.getName());
               JsonArray params = new JsonArray();
               for (Class c : m.getParameterTypes()) {
                  params.add(c.getName());
               }
               method.put("parameters", params);
               method.put("returns", m.getReturnType().getName());

               methods.add(method);
            }
         }

         routingContext.response().end(methods.encodePrettily());
      }
   }

   public void callMethod(final RoutingContext routingContext) {

   }

   @SuppressWarnings("unchecked")
   public void callNoParamMethod(final RoutingContext routingContext) {
      String microserviceName = routingContext.request().getParam("microservice");
      String methodName = routingContext.request().getParam("method");

      try {
         Method m = gatewayRegistry.get(microserviceName).getBeanClass().getDeclaredMethod(methodName);
         Object result = m.invoke(gatewayRegistry.get(microserviceName).getBeanClass());

         routingContext.response().end(Json.encodePrettily(result));
      } catch (Exception e) {
         log.warn(String.format("Unable to call method %s#%s: ", microserviceName, methodName), e);
         routingContext.response().setStatusCode(503).end("Resource not available.");
      }
   }

}
