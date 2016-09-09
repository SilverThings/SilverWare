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
package io.silverware.microservices.providers.cluster;

import io.silverware.microservices.providers.cluster.internal.JgroupsMessageSender;
import static io.silverware.microservices.providers.cluster.internal.RemoteServiceHandleStoreTest.META_DATA;
import io.silverware.microservices.providers.cluster.internal.message.response.MicroserviceSearchResponse;
import io.silverware.microservices.silver.cluster.RemoteServiceHandlesStore;
import io.silverware.microservices.silver.cluster.ServiceHandle;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import static org.assertj.core.api.Assertions.assertThat;
import org.jgroups.Address;
import org.jgroups.util.FutureListener;
import org.jgroups.util.RspList;
import org.testng.annotations.Test;

import java.util.Set;

/**
 * Test for ClusterMicroserviceProvider
 *   @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class ClusterMicroserviceProviderTest {

   public static final RemoteServiceHandlesStore REMOTE_SERVICE_HANDLES_STORE = new RemoteServiceHandlesStore();

   @Tested
   private ClusterMicroserviceProvider clusterMicroserviceProvider;
   @Injectable
   private RemoteServiceHandlesStore store;
   @Injectable
   private JgroupsMessageSender sender;


   @Test
   public void testLookupMicroservice() throws Exception {
      Set<ServiceHandle> mockHandles = Util.createSetFrom(Util.createHandle("1"), Util.createHandle("2"));
      Set<Object> services = Util.createSetFrom(new Object(), new Object());


      new Expectations() {{
         sender.sendToClusterAsync(META_DATA, (Set<Address>) any, (FutureListener<RspList<MicroserviceSearchResponse>>) any);
         times = 1;
         result = mockHandles;
         // TODO: 9/9/16 test this again
//         store.addHandles(META_DATA, mockHandles);
//         times = 1;
         store.getServices(META_DATA);
         result = services;
         times = 1;

      }};
      Set<Object> objects = clusterMicroserviceProvider.lookupMicroservice(META_DATA);
      assertThat(objects).isNotEmpty().isEqualTo(services);
   }

   @Test
   public void testLookupLocalMicroservice() throws Exception {
      Set<Object> objects = clusterMicroserviceProvider.lookupLocalMicroservice(META_DATA);
      assertThat(objects)
              .isNotNull()
              .isEmpty();

   }
}