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
package io.silverware.microservices;

import io.silverware.microservices.silver.HttpInvokerSilverService;
import io.silverware.microservices.silver.cluster.ServiceHandle;
import io.silverware.microservices.util.Utils;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

import java.lang.annotation.Annotation;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Immutable meta-data of a discovered Microservice implementation.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public final class MicroserviceMetaData {

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
   private final String specVersion;

   /**
    * Microservice implementation version.
    */
   private final String implVersion;

   /**
    * Create a representation of a discovered Microservice.
    *
    * @param name
    *        The name of the discovered Microservice.
    * @param type
    *        The type of the discovered Microservice.
    * @param qualifiers
    *        The qualifiers of the discovered Microservice.
    * @param annotations
    *        The annotations of the discovered Microservice.
    */
   public MicroserviceMetaData(final String name, final Class type, final Set<Annotation> qualifiers, final Set<Annotation> annotations) {
      this.name = name;
      this.type = type;
      this.qualifiers = qualifiers;
      this.annotations = annotations;
      this.specVersion = Utils.getClassSpecVersion(type);
      this.implVersion = Utils.getClassImplVersion(type);

      if (name == null || type == null) {
         throw new IllegalStateException("Name and type fields cannot be null.");
      }
   }

   /**
    * Create a representation of a discovered Microservice.
    *
    * @param name
    *        The name of the discovered Microservice.
    * @param type
    *        The type of the discovered Microservice.
    * @param qualifiers
    *        The qualifiers of the discovered Microservice.
    */
   public MicroserviceMetaData(final String name, final Class type, final Set<Annotation> qualifiers) {
      this.name = name;
      this.type = type;
      this.qualifiers = qualifiers;
      this.annotations = new HashSet<>();
      this.specVersion = Utils.getClassSpecVersion(type);
      this.implVersion = Utils.getClassImplVersion(type);

      if (name == null || type == null) {
         throw new IllegalStateException("Name and type fields cannot be null.");
      }
   }

   /**
    * Create a representation of a discovered Microservice.
    *
    * @param name
    *        The name of the discovered Microservice.
    * @param type
    *        The type of the discovered Microservice.
    * @param qualifiers
    *        The qualifiers of the discovered Microservice.
    * @param annotations
    *        The annotations of the discovered Microservice.
    * @param specVersion
    *        The specification version we are looking for.
    * @param implVersion
    *        The implementation version we are looking for.
    */
   public MicroserviceMetaData(final String name, final Class type, final Set<Annotation> qualifiers, final Set<Annotation> annotations, final String specVersion, final String implVersion) {
      this.name = name;
      this.type = type;
      this.qualifiers = qualifiers;
      this.annotations = annotations;
      this.specVersion = specVersion;
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
   public String getSpecVersion() {
      return specVersion;
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
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      MicroserviceMetaData that = (MicroserviceMetaData) o;

      if (!name.equals(that.name)) {
         return false;
      }
      if (!type.equals(that.type)) {
         return false;
      }
      if (qualifiers != null ? !qualifiers.equals(that.qualifiers) : that.qualifiers != null) {
         return false;
      }
      if (annotations != null ? !annotations.equals(that.annotations) : that.annotations != null) {
         return false;
      }
      return !(specVersion != null ? !specVersion.equals(that.specVersion) : that.specVersion != null);

   }

   @Override
   public int hashCode() {
      int result = name.hashCode();
      result = 31 * result + type.hashCode();
      result = 31 * result + (qualifiers != null ? qualifiers.hashCode() : 0);
      result = 31 * result + (annotations != null ? annotations.hashCode() : 0);
      return result;
   }

   @Override
   public String toString() {
      return "microservice " + name + " of type " + type.getCanonicalName() + " with qualifiers " + Arrays.toString(qualifiers.toArray())
            + " and with annotations " + Arrays.toString(annotations.toArray()) + " (version: spec. " + specVersion + ", impl. " + implVersion + ")";
   }

   @SuppressWarnings({"unchecked", "checkstyle:JavadocMethod"})
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
