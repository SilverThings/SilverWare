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

import com.cedarsoftware.util.io.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.Bean;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.annotations.ParamName;
import io.silverware.microservices.silver.CdiSilverService;

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
 * @author <a href="mailto:mswiech@redhat.com">Martin Swiech</a>
 */
public class RestInterface extends AbstractVerticle {

   /**
    * Logger.
    */
   private static final Logger log = LogManager.getLogger(RestInterface.class);

   private final Context context;
   private final int port;
   private final String host;
   private Vertx vertx;

   private Map<String, Bean> gatewayRegistry = new HashMap<>();

   public RestInterface(final Context context) {
      this.context = context;
      port = Integer.valueOf(context.getProperties().getOrDefault(CdiSilverService.CDI_REST_PORT, "8081").toString());
      host = context.getProperties().getOrDefault(CdiSilverService.CDI_REST_HOST, "").toString();
   }

   @SuppressWarnings("checkstyle:JavadocMethod")
   public void registerGateway(final String serviceName, final Bean bean) {
      gatewayRegistry.put(serviceName, bean);
   }

   @SuppressWarnings("checkstyle:JavadocMethod")
   public void deploy() {
      if (gatewayRegistry.size() > 0) {
         VertxOptions vertxOptions = new VertxOptions().setWorkerPoolSize(100);
         vertx = Vertx.vertx(vertxOptions);
         DeploymentOptions deploymentOptions = new DeploymentOptions().setWorker(true);
         vertx.deployVerticle(this, deploymentOptions);
      }
   }

   @SuppressWarnings("checkstyle:JavadocMethod")
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
      router.get("/rest/:microservice/:method").handler(this::callNoParamMethod);
      router.post("/rest/:microservice/:method").handler(this::callMethod);

      HttpServerOptions options = new HttpServerOptions().setAcceptBacklog(1000);
      HttpServer server = vertx.createHttpServer(options).requestHandler(router::accept);
      if (host.isEmpty()) {
         server.listen(port);
      } else {
         server.listen(port, host);
      }
   }

   @SuppressWarnings("checkstyle:JavadocMethod")
   public void listBeans(final RoutingContext routingContext) {
      JsonArray beans = new JsonArray();

      gatewayRegistry.keySet().forEach(beans::add);

      routingContext.response().end(beans.encodePrettily());
   }

   @SuppressWarnings("checkstyle:JavadocMethod")
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

   private String stackTraceAsString(final Exception e) throws IOException {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      return sw.toString();
   }

   @SuppressWarnings("checkstyle:JavadocMethod")
   public void callMethod(final RoutingContext routingContext) {
      final String microserviceName = routingContext.request().getParam("microservice");
      final String methodName = routingContext.request().getParam("method");
      final Bean bean = gatewayRegistry.get(microserviceName);

      routingContext.request().bodyHandler(buffer -> {
         final JsonObject mainJsonObject = new JsonObject(buffer.toString());

         try {
            final Class<?> beanClass = bean.getBeanClass();
            List<Method> methods = Arrays.asList(beanClass.getDeclaredMethods()).stream().filter(method -> method.getName().equals(methodName) && method.getParameterCount() == mainJsonObject.size()).collect(Collectors.toList());

            if (methods.size() == 0) {
               throw new IllegalStateException(String.format("No such method %s with compatible parameters.", methodName));
            }

            if (methods.size() > 1) {
               throw new IllegalStateException("Overridden methods are not supported yet.");
            }

            final Method m = methods.get(0);

            final Parameter[] methodParams = m.getParameters();
            final Object[] paramValues = new Object[methodParams.length];
            final ConvertUtilsBean convert = new ConvertUtilsBean();
            for (int i = 0; i < methodParams.length; i++) {
               final Parameter methodParameter = methodParams[i];
               final String paramName = getParamName(methodParameter, m, beanClass);
               final Object jsonObject = mainJsonObject.getValue(paramName);
               paramValues[i] = convert.convert(jsonObject, methodParameter.getType());
            }

            @SuppressWarnings("unchecked")
            Set<Object> services = context.lookupLocalMicroservice(new MicroserviceMetaData(microserviceName, beanClass, bean.getQualifiers()));
            JsonObject response = new JsonObject();
            try {
               Object result = m.invoke(services.iterator().next(), paramValues);
               response.put("result", Json.encodePrettily(result));
               response.put("resultPlain", JsonWriter.objectToJson(result));
            } catch (Exception e) {
               response.put("exception", e.toString());
               response.put("stackTrace", stackTraceAsString(e));
               log.warn("Could not call method: ", e);
            }

            routingContext.response().end(response.encodePrettily());
         } catch (Exception e) {
            log.warn(String.format("Unable to call method %s#%s: ", microserviceName, methodName), e);
            routingContext.response().setStatusCode(503).end("Resource not available.");
         }
      });
   }

   private static String getParamName(final Parameter methodParameter, Method method, final Class<?> clazz) {
      final ParamName paramName = methodParameter.getAnnotation(ParamName.class);
      if (paramName != null && paramName.value() != null && !paramName.value().trim().isEmpty()) {
         return paramName.value().trim();
      }
      final JsonProperty jsonProperty = methodParameter.getAnnotation(JsonProperty.class);
      if (jsonProperty != null && jsonProperty.value() != null && !jsonProperty.value().trim().isEmpty()) {
         return jsonProperty.value().trim();
      }

      if (!methodParameter.isNamePresent()) {
         log.warn(String.format("Method parameter name is not present for method %s in class %s. Please use compilation argument (or test compilation argument) \"-parameters\""
               + " or use annotation @%s or @%s for parameters of this method.", method.getName(), clazz.getCanonicalName(), ParamName.class.getCanonicalName(), JsonProperty.class.getCanonicalName()));
      }
      return methodParameter.getName();
   }

   @SuppressWarnings({"unchecked", "checkstyle:JavadocMethod"})
   public void callNoParamMethod(final RoutingContext routingContext) {
      String microserviceName = routingContext.request().getParam("microservice");
      String methodName = routingContext.request().getParam("method");
      Bean bean = gatewayRegistry.get(microserviceName);

      try {
         Method m = bean.getBeanClass().getDeclaredMethod(methodName);
         Set<Object> services = context.lookupLocalMicroservice(new MicroserviceMetaData(microserviceName, bean.getBeanClass(), bean.getQualifiers()));
         JsonObject response = new JsonObject();
         try {
            Object result = m.invoke(services.iterator().next());
            response.put("result", Json.encodePrettily(result));
         } catch (Exception e) {
            response.put("exception", e.toString());
            response.put("stackTrace", stackTraceAsString(e));
            log.warn("Could not call method: ", e);
         }

         routingContext.response().end(response.encodePrettily());
      } catch (Exception e) {
         log.warn(String.format("Unable to call method %s#%s: ", microserviceName, methodName), e);
         routingContext.response().setStatusCode(503).end("Resource not available.");
      }
   }

}
