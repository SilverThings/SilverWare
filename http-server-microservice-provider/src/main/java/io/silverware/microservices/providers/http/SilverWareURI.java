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
package io.silverware.microservices.providers.http;

import io.silverware.microservices.silver.HttpServerSilverService;

import java.util.Map;

/**
 * SilverWare URI class to obtain SilverWare URI's built from global properties.
 *
 * @author Radek Koubsky (radek.koubsky@gmail.com)
 */
public class SilverWareURI {
   static final String HTTP = "http://";
   static final String HTTPS = "https://";
   private final Map<String, Object> properties;

   /**
    * Ctor.
    *
    * @param properties global properties
    */
   public SilverWareURI(final Map<String, Object> properties) {
      this.properties = properties;
   }

   private String host() {
      return String.valueOf(this.properties.get(HttpServerSilverService.HTTP_SERVER_ADDRESS));

   }

   private String rest() {
      return new StringBuilder(
            String.valueOf(this.properties.get(HttpServerSilverService.HTTP_SERVER_REST_CONTEXT_PATH)))
                  .append("/")
                  .append(this.properties.get(HttpServerSilverService.HTTP_SERVER_REST_SERVLET_MAPPING_PREFIX))
                  .toString();
   }

   /**
    * Returns HTTP URL.
    *
    * @return SilverWare http url
    */
   public String http() {
      return new StringBuilder(HTTP)
            .append(host())
            .append(":")
            .append(this.properties.get(HttpServerSilverService.HTTP_SERVER_PORT))
            .toString();

   }

   /**
    * Returns HTTPS URL.
    *
    * @return SilverWare https url
    */
   public String https() {
      return new StringBuilder(HTTPS)
            .append(host())
            .append(":")
            .append(this.properties.get(HttpServerSilverService.HTTPS_SERVER_PORT))
            .toString();

   }

   /**
    * Returns HTTP URL for REST.
    *
    * @return SilverWare HTTP REST URL
    */
   public String httpREST() {
      return new StringBuilder(http()).append(rest()).toString();

   }

   /**
    * Returns HTTPS URL for REST.
    *
    * @return SilverWare HTTPS REST URL
    */
   public String httpsREST() {
      return new StringBuilder(https()).append(rest()).toString();
   }
}
