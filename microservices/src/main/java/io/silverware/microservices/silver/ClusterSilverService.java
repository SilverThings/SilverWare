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
 * Makes it possible for the platforms to discover each other in a cluster.
 * Can lookup services on other instances. Kepps handles to remote services.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public interface ClusterSilverService extends ProvidingSilverService {

   /**
    * Property with the cluster group name.
    */
   String CLUSTER_GROUP = "silverware.cluster.group";

   /**
    * Property with the cluster configuration name.
    */
   String CLUSTER_CONFIGURATION = "silverware.cluster.configuration";

   /**
    * Timeout in milliseconds for lookup in a cluster.
    */
   String CLUSTER_LOOKUP_TIMEOUT = "silverware.cluster.lookup.timeout";

}
