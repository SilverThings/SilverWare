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
package org.silverware.microservices;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Set;

/**
 * Immutable meta-data of a discovered Microservice implementation.
 *
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class MicroserviceMetaData {

   /**
    * Name of the Microservice.
    */
   private final String name;

   /**
    * Actual type of the Microservice.
    */
   private final Class type;

   /**s
    * Qualifiers of the Microservice.
    */
   private final Set<Annotation> qualifiers;

   /**
    * Create a representation of a discovered Microservice.
    *
    * @param name
    *       The name of the discovered Microservice.
    * @param type
    *       The type of the discovered Microservice.
    * @param qualifiers
    *       The qualifiers of the discovered Microservice.
    */
   public MicroserviceMetaData(@JsonProperty("name") final String name, @JsonProperty("type") final Class type, @JsonProperty("qualifiers") final Set<Annotation> qualifiers) {
      this.name = name;
      this.type = type;
      this.qualifiers = qualifiers;

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

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      }
      if (!(o instanceof MicroserviceMetaData)) {
         return false;
      }

      final MicroserviceMetaData that = (MicroserviceMetaData) o;

      if (!name.equals(that.name)) {
         return false;
      }
      if (qualifiers != null ? !qualifiers.equals(that.qualifiers) : that.qualifiers != null) {
         return false;
      }
      if (!type.equals(that.type)) {
         return false;
      }

      return true;
   }

   @Override
   public int hashCode() {
      int result = name.hashCode();
      result = 31 * result + type.hashCode();
      result = 31 * result + (qualifiers != null ? qualifiers.hashCode() : 0);
      return result;
   }

   @Override
   public String toString() {
      return "microservice " + name + " of type " + type.getCanonicalName() + " with qualifiers " + Arrays.toString(qualifiers.toArray());
   }
}
