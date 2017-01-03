/*
 * -----------------------------------------------------------------------\
 * PerfCake
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
package io.silverware.microservices.providers.http.invoker.internal;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.silver.HttpInvokerSilverService;
import io.silverware.microservices.silver.HttpServerSilverService;
import io.silverware.microservices.silver.cluster.Invocation;
import io.silverware.microservices.silver.cluster.LocalServiceHandle;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class HttpInvokerServlet extends HttpServlet {

   private static Context context;
   private static String url;

   public static void setContext(final Context ctx) {
      context = ctx;
      url = ctx.getProperties().get(HttpServerSilverService.HTTP_SERVER_ADDRESS) + ":" + ctx.getProperties().get(HttpServerSilverService.HTTP_SERVER_PORT);
   }

   @Override
   protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
      resp.getWriter().print("Hello from Microservices Http Invoker. Try using POST instead.");
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
   }

   @Override
   protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
      if (req.getRequestURI().endsWith(HttpInvokerSilverService.QUERY_COMMAND)) {
         final JsonReader jsonReader = new JsonReader(req.getInputStream());
         final MicroserviceMetaData metaData = (MicroserviceMetaData) jsonReader.readObject();
         final List<LocalServiceHandle> localHandles = context.assureLocalHandles(metaData);
         List<HttpServiceHandle> httpServiceHandles = localHandles.stream().map(h -> new HttpServiceHandle(url, h.getHandle())).collect(Collectors.toList());
         final JsonWriter jsonWriter = new JsonWriter(resp.getOutputStream());
         jsonWriter.write(httpServiceHandles);
      } else if (req.getRequestURI().endsWith(HttpInvokerSilverService.INVOKE_COMMAND)) {
         final JsonReader jsonReader = new JsonReader(req.getInputStream());
         final Invocation invocation = (Invocation) jsonReader.readObject();
         try {
            final JsonWriter jsonWriter = new JsonWriter(resp.getOutputStream());
            jsonWriter.write(invocation.invoke(context));
         } catch (Exception e) {
            throw new IOException(String.format("Unable to invoke Microservice using invocation %s: ", invocation.toString()), e);
         }
      } else {
         resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Unsupported operation.");
      }
   }
}
