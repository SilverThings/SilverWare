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
package org.silverware.microservices.providers.cluster;

import static org.testng.Assert.*;

import org.silverware.microservices.silver.HttpServerSilverService;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class ClusterMicroserviceProviderTest {

   @Test
   public void testHttpInvoker() throws Exception {
      System.getProperties().setProperty(HttpServerSilverService.HTTP_SERVER_ADDRESS, "1.2.3.4");
      final BootUtil bootUtil = new BootUtil();
      final Map<String, Object> platformProperties = bootUtil.getContext().getProperties();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName(), HttpServerMicroserviceProvider.class.getPackage().getName(), HttpInvokerMicroserviceProvider.class.getPackage().getName(), CdiMicroserviceProvider.class.getPackage().getName());
      platform.start();
   }

}