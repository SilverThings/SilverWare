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

import org.silverware.microservices.MicroserviceMetaData;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class ServiceHandle implements Serializable {

   final static private AtomicInteger handleSource = new AtomicInteger(0);

   final private int handle;

   final private String host;

   final private MicroserviceMetaData query;

   public ServiceHandle(final String host, final MicroserviceMetaData query) {
      this.handle = handleSource.getAndIncrement();
      this.host = host;
      this.query = query;
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
      if (!host.equals(that.host)) {
         return false;
      }
      return query.equals(that.query);

   }

   @Override
   public int hashCode() {
      int result = handle;
      result = 31 * result + host.hashCode();
      result = 31 * result + query.hashCode();
      return result;
   }

   @Override
   public String toString() {
      return "ServiceHandle{" +
            "handle=" + handle +
            ", host='" + host + '\'' +
            ", query=" + query +
            '}';
   }
}
