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
package org.silverware.microservices.silver.cluster;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import org.silverware.microservices.Context;
import org.silverware.microservices.MicroserviceMetaData;
import org.silverware.microservices.silver.HttpInvokerSilverService;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Holds a handle to a microservice implementation.
 * Can keep a reference to real Microservice implementation in the case of a local Microservice,
 * otherwise it holds a remote proxy.
 * Can act as a Http Invoker client to invoke the remote Microservice.
 *
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class ServiceHandle implements Serializable {

   final static transient private AtomicInteger handleSource = new AtomicInteger(0);

   final private int handle;

   final private String host;

   final private MicroserviceMetaData query;

   final private transient Object service;

   public ServiceHandle(final String host, final MicroserviceMetaData query, final Object service) {
      this.handle = handleSource.getAndIncrement();
      this.host = host;
      this.query = query;
      this.service = service;
   }

   public ServiceHandle withProxy(final Object proxy) {
      return new ServiceHandle(host, query, proxy);
   }

   public int getHandle() {
      return handle;
   }

   public String getHost() {
      return host;
   }

   public MicroserviceMetaData getQuery() {
      return query;
   }

   public Object getService() {
      return service;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final ServiceHandle that = (ServiceHandle) o;

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
      return "ServiceHandle{" +
            "handle=" + handle +
            ", host='" + host + '\'' +
            ", query=" + query +
            ", service=" + service +
            '}';
   }

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

   public Object invoke(final Context context, final String method, final Object[] params) throws Exception {
      final Class[] paramTypes = new Class[params.length];
      for (int i = 0; i < params.length; i++) {
         paramTypes[i] = params.getClass();
      }

      return invoke(context, method, paramTypes, params);
   }

}
