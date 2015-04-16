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
package org.silverware.microservices.silver;

import org.silverware.microservices.SilverWareException;
import org.silverware.microservices.silver.http.ServletDescriptor;

import java.util.List;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public interface HttpServerSilverService extends SilverService {

   String HTTP_SERVER = "silverware.http.server";
   String HTTP_SERVER_PORT = "silverware.http.port";
   String HTTP_SERVER_ADDRESS = "silverware.http.address";

   void deployServlet(final String contextPath, final String deploymentName, final List<ServletDescriptor> servletDescriptors) throws SilverWareException;
}
