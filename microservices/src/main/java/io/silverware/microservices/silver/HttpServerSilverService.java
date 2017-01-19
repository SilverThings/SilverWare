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
package io.silverware.microservices.silver;

import io.silverware.microservices.SilverWareException;
import io.silverware.microservices.silver.http.ServletDescriptor;

import java.util.List;

/**
 * Provider of an HTTP Server.
 * Typically needed by {@link MonitoringSilverService} and or {@link HttpInvokerSilverService}.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public interface HttpServerSilverService extends SilverService {

   /**
    * Context property holding an instance of the HTTP server.
    */
   String HTTP_SERVER = "silverware.http.server";

   /**
    * Property with the HTTP port number on which the server listens.
    */
   String HTTP_SERVER_PORT = "silverware.http.port";

   /**
    * Property with the HTTPS port number on which the server listens.
    */
   String HTTPS_SERVER_PORT = "silverware.https.port";

   /**
    * Property with the hostname/IP address on which the server listens.
    */
   String HTTP_SERVER_ADDRESS = "silverware.http.address";

   /**
    * Context path to which REST requests are routed.
    */
   String HTTP_SERVER_REST_CONTEXT_PATH = "silverware.http.rest.context.path";

   /**
    * Mapping of the base REST Servlet.
    */
   String HTTP_SERVER_REST_SERVLET_MAPPING_PREFIX = "silverware.http.rest.servlet.mapping.prefix";

   /**
    * Place where server keystore file is stored.
    */
   String HTTP_SERVER_KEY_STORE = "silverware.http.server.keystore";

   /**
    * Password to the server keystore file.
    */
   String HTTP_SERVER_KEY_STORE_PASSWORD = "silverware.http.server.keystore.password";

   /**
    * Place where server truststore file is stored.
    */
   String HTTP_SERVER_TRUST_STORE = "silverware.http.server.truststore";

   /**
    * Password to the server truststore file.
    */
   String HTTP_SERVER_TRUST_STORE_PASSWORD = "silverware.http.server.truststore.password";

   /**
    * Enables/Disables SSL.
    */
   String HTTP_SERVER_SSL_ENABLED = "silverware.http.server.ssl.enabled";

   /**
    * Default Keystore.
    */
   String DEFAULT_SSL_KEYSTORE = "silverware-server.keystore";

   /**
    * DefaultTruststore.
    */
   String DEFAULT_SSL_TRUSTSTORE = "silverware-server.truststore";

   /**
    * Password to the default Keystore and Truststore
    */
   String DEFAULT_SSL_STORE_PASSWORD = "silverware";

   /**
    * Deploys a servlet on the HTTP server.
    *
    * @param contextPath
    *       Context path where the servlet should be bound.
    * @param deploymentName
    *       Name of the deployment.
    * @param servletDescriptors
    *       A list of descriptions of the servlet(s).
    * @throws SilverWareException
    *       When it was not possible to deploy the servlet(s).
    */
   void deployServlet(final String contextPath, final String deploymentName,
         final List<ServletDescriptor> servletDescriptors) throws SilverWareException;

   /**
    * Tests if this service is deployed. Service is deployed if its underlying HTTP server has started.
    *
    * @return <b>true</b> if this service is deployed; <b>false</b> otherwise
    */
   boolean isDeployed();
}
