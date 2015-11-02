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
package io.silverware.microservices.silver.http;

import java.util.Properties;

/**
 * Describes a servlet deployment.
 *
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class ServletDescriptor {

   /**
    * Name of the servlet.
    */
   private final String name;

   /**
    * Servlet class.
    */
   private final Class<?> servletClass;

   /**
    * Context path mapping of the servlet.
    */
   private final String mapping;

   /**
    * Servlet's initial parameters.
    */
   private final Properties properties;

   public ServletDescriptor(final String name, final Class<?> servletClass, final String mapping, final Properties properties) {
      this.name = name;
      this.servletClass = servletClass;
      this.mapping = mapping;
      this.properties = properties;
   }

   public String getName() {
      return name;
   }

   public Class<?> getServletClass() {
      return servletClass;
   }

   public String getMapping() {
      return mapping;
   }

   public Properties getProperties() {
      return properties;
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      }
      if (o == null || getClass() != o.getClass()) {
         return false;
      }

      final ServletDescriptor that = (ServletDescriptor) o;

      if (!name.equals(that.name)) {
         return false;
      }
      if (!servletClass.equals(that.servletClass)) {
         return false;
      }
      if (!mapping.equals(that.mapping)) {
         return false;
      }
      return !(properties != null ? !properties.equals(that.properties) : that.properties != null);

   }

   @Override
   public int hashCode() {
      int result = name.hashCode();
      result = 31 * result + servletClass.hashCode();
      result = 31 * result + mapping.hashCode();
      result = 31 * result + (properties != null ? properties.hashCode() : 0);
      return result;
   }

   @Override
   public String toString() {
      return "ServletDescriptor{" +
            "name='" + name + '\'' +
            ", servletClass=" + servletClass +
            ", mapping='" + mapping + '\'' +
            ", properties=" + properties +
            '}';
   }
}
