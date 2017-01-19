/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2015 the original author or authors.
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
package io.silverware.microservices;

import io.silverware.microservices.silver.HttpInvokerSilverService;
import io.silverware.microservices.silver.cluster.ServiceHandle;
import io.silverware.microservices.util.VersionComparator;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Immutable meta-data of a discovered Microservice implementation.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public final class MicroserviceMetaData implements Serializable {

   /**
    * Name of the Microservice.
    */
   private final String name;

   /**
    * Actual type of the Microservice.
    */
   private final Class type;

   /**
    * Qualifiers of the Microservice.
    */
   private final Set<Annotation> qualifiers;

   /**
    * Annotations of the Microservice.
    */
   private final Set<Annotation> annotations;

   /**
    * Microservice specification version.
    */
   private final String apiVersion;

   /**
    * Microservice implementation version.
    */
   private final String implVersion;

   /**
    * Create a representation of a discovered Microservice.
    *
    * @param name
    *       The name of the discovered Microservice.
    * @param type
    *       The type of the discovered Microservice.
    * @param qualifiers
    *       The qualifiers of the discovered Microservice.
    * @param annotations
    *       The annotations of the discovered Microservice.
    * @param apiVersion
    *       The specification version we are looking for.
    * @param implVersion
    *       The implementation version we are looking for.
    */
   public MicroserviceMetaData(final String name, final Class type, final Set<Annotation> qualifiers, final Set<Annotation> annotations, final String apiVersion, final String implVersion) {
      this.name = name;
      this.type = type;
      this.qualifiers = qualifiers;
      this.annotations = annotations;
      this.apiVersion = apiVersion;
      this.implVersion = implVersion;

      if (name == null || type == null) {
         throw new IllegalStateException("Name and type fields cannot be null.");
      }
   }

   /**
    * Gets the name of the discovered Microservice.
    *
    * @return The name of the discovered Microservice.
    */
   public String getName() {
      return name;
   }

   /**
    * Gets the type of the discovered Microservice.
    *
    * @return The type of the discovered Microservice.
    */
   public Class getType() {
      return type;
   }

   /**
    * Gets the qualifiers of the discovered Microservice.
    *
    * @return The qualifiers of the discovered Microservice.
    */
   public Set<Annotation> getQualifiers() {
      return qualifiers;
   }

   /**
    * Gets the annotations of the discovered Microservice.
    *
    * @return The annotations of the discovered Microservice.
    */
   public Set<Annotation> getAnnotations() {
      return annotations;
   }

   /**
    * Gets the Microservice specification version.
    *
    * @return The Microservice specification version.
    */
   public String getApiVersion() {
      return apiVersion;
   }

   /**
    * Gets the Microservice implementation version.
    *
    * @return The Microservice implementation version.
    */
   public String getImplVersion() {
      return implVersion;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      }
      if (!(o instanceof MicroserviceMetaData)) {
         return false;
      }

      final MicroserviceMetaData that = (MicroserviceMetaData) o;

      if (!getName().equals(that.getName())) {
         return false;
      }
      if (!getType().equals(that.getType())) {
         return false;
      }
      if (getQualifiers() != null ? !getQualifiers().equals(that.getQualifiers()) : that.getQualifiers() != null) {
         return false;
      }
      return getAnnotations() != null ? getAnnotations().equals(that.getAnnotations()) : that.getAnnotations() == null;
   }

   @Override
   public int hashCode() {
      int result = getName().hashCode();
      result = 31 * result + getType().hashCode();
      result = 31 * result + (getQualifiers() != null ? getQualifiers().hashCode() : 0);
      result = 31 * result + (getAnnotations() != null ? getAnnotations().hashCode() : 0);
      return result;
   }

   @Override
   public String toString() {
      return "microservice " + name + " of type " + type.getCanonicalName() + " with qualifiers " + Arrays.toString(qualifiers.toArray())
            + " and with annotations " + Arrays.toString(annotations.toArray()) + " (version: spec. " + apiVersion + ", impl. " + implVersion + ")";
   }

   /**
    * Compares api version from query with a implementation version of a this object resolve whether it satisfies.
    *
    * @param query
    *       other metada object which specify version
    * @return boolean representing whether this object satisfies a query
    */
   public boolean satisfies(MicroserviceMetaData query) {
      return equals(query) &&
            satisfiesJustVersion(query);
   }

   /**
    * Compares api version from query with a implementation version of a this object resolve whether it satisfies.
    *
    * @param query
    *       other metada object which specify version
    * @return boolean representing whether this object satisfies just version comparision
    */
   public boolean satisfiesJustVersion(MicroserviceMetaData query) {
      return VersionComparator.forVersion(query, this.implVersion).satisfies(query.implVersion)
            && VersionComparator.forVersion(query, this.apiVersion).satisfies(query.apiVersion);
   }

   /**
    * Queries host and retrieves all service handles
    *
    * @param context
    *       context of a host
    * @param host
    *       address of a host
    * @return a list of a service handles
    * @throws Exception
    *       when something went wrong
    */
   public List<ServiceHandle> query(final Context context, final String host) throws Exception {
      String urlBase = "http://" + host + "/" + context.getProperties().get(HttpInvokerSilverService.INVOKER_URL) + "/query";

      HttpURLConnection con = (HttpURLConnection) new URL(urlBase).openConnection();
      con.setRequestMethod("POST");
      con.setDoInput(true);
      con.setDoOutput(true);
      con.connect();

      JsonWriter jsonWriter = new JsonWriter(con.getOutputStream());
      jsonWriter.write(this);
      JsonReader jsonReader = new JsonReader(con.getInputStream());
      List<ServiceHandle> response = (List<ServiceHandle>) jsonReader.readObject();

      con.disconnect();

      return response;
   }
}
