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
package io.silverware.microservices.silver.cluster;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.silver.HttpInvokerSilverService;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds a handle to a microservice implementation.
 * Can keep a reference to real Microservice implementation in the case of a local Microservice,
 * otherwise it holds a remote proxy.
 * Can act as a Http Invoker client to invoke the remote Microservice.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
// TODO: 9/8/16 Remove htpp invoker - it should be in separated object
public class LocalServiceHandle implements ServiceHandle {

   private static final transient AtomicInteger handleSource = new AtomicInteger(0);

   private final int handle;

   private final String host;

   private final MicroserviceMetaData query;

   private final transient Object service;

   public LocalServiceHandle(final String host, final MicroserviceMetaData query, final Object service) {
      this.handle = handleSource.getAndIncrement();
      this.host = host;
      this.query = query;
      this.service = service;
   }

   public ServiceHandle withProxy(final Object proxy) {
      return new LocalServiceHandle(host, query, proxy);
   }

   public int getHandle() {
      return handle;
   }

   @Override
   public Object getProxy() {
      return service;
   }

   @Override
   public String getHost() {
      return host;
   }

   public MicroserviceMetaData getMetaData() {
      return query;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final LocalServiceHandle that = (LocalServiceHandle) o;

      if (handle != that.handle) {
         return false;
      }
      if (host != null ? !host.equals(that.host) : that.host != null) {
         return false;
      }

      return query.equals(that.query);
   }

   @Override
   public int hashCode() {
      int result = handle;
      result = 31 * result + (host != null ? host.hashCode() : 0);
      result = 31 * result + query.hashCode();
      return result;
   }

   @Override
   public String toString() {
      return "ServiceHandle{" + "handle=" + handle + ", host='" + host + '\'' + ", query=" + query + ", service=" + service + '}';
   }

   @Override
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
