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
package io.silverware.microservices.providers.http.invoker.internal;

import io.silverware.microservices.Context;
import io.silverware.microservices.silver.HttpInvokerSilverService;
import io.silverware.microservices.silver.cluster.Invocation;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Can act as a Http Invoker client to invoke the remote Microservice.
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class HttpServiceHandle {

   private final int handle;

   private final String host;

   public HttpServiceHandle(final String host, int handle) {
      this.host = host;
      this.handle = handle;
   }

   public int getHandle() {
      return handle;
   }

   /**
    * Context is not used
    *
    * @param context
    *       Local microservice context
    * @param method
    *       name of the method to be invoked
    * @param paramTypes
    *       parameters types
    * @param params
    *       parameters of method called
    * @return result of the invocation
    * @throws Exception
    *       in case of any error
    */
   public Object invoke(final Context context, final String method, final Class[] paramTypes, final Object[] params) throws Exception {
      String urlBase = "http://" + host + "/" + context.getProperties().get(HttpInvokerSilverService.INVOKER_URL) + "/invoke";

      HttpURLConnection con = (HttpURLConnection) new URL(urlBase).openConnection();
      con.setRequestMethod("POST");
      con.setDoInput(true);
      con.setDoOutput(true);
      con.connect();

      Invocation invocation = new Invocation(handle, method, paramTypes, params);
      JsonWriter jsonWriter = new JsonWriter(con.getOutputStream());
      jsonWriter.write(invocation);
      JsonReader jsonReader = new JsonReader(con.getInputStream());
      Object response = jsonReader.readObject();

      con.disconnect();

      return response;
   }
}
