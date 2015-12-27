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
package io.silverware.microservices.silver;

/**
 * CDI Microservices deployer.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public interface CdiSilverService extends ProvidingSilverService {

   /**
    * Context property where the bean manager is stored.
    */
   String BEAN_MANAGER = "silverware.cdi.beanManager";

   /**
    * Context property where the CDI container is stored.
    */
   String CDI_CONTAINER = "silverware.cdi.container";

   /**
    * Port on which the CDI REST interface should listen.
    */
   String CDI_REST_PORT = "silverware.cdi.rest.port";

   /**
    * Host on which the CDI REST interface should listen.
    */
   String CDI_REST_HOST = "silverware.cdi.rest.host";
}
