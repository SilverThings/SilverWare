/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2010 - 2016 the original author or authors.
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

import static io.silverware.microservices.providers.cluster.internal.exception.SilverWareClusteringException.SilverWareClusteringError.INITIALIZATION_ERROR;
import static io.silverware.microservices.providers.cluster.internal.exception.SilverWareClusteringException.SilverWareClusteringError.JGROUPS_ERROR;
import static java.util.Collections.emptySet;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.providers.cluster.internal.JgroupsMessageReceiver;
import io.silverware.microservices.providers.cluster.internal.JgroupsMessageSender;
import io.silverware.microservices.providers.cluster.internal.exception.SilverWareClusteringException;
import io.silverware.microservices.providers.cluster.internal.message.KnownImplementation;
import io.silverware.microservices.providers.cluster.internal.message.response.MicroserviceSearchResponse;
import io.silverware.microservices.providers.cluster.internal.util.FutureListenerHelper;
import io.silverware.microservices.silver.ClusterSilverService;
import io.silverware.microservices.silver.cluster.RemoteServiceHandlesStore;
import io.silverware.microservices.silver.cluster.ServiceHandle;

import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This provider provides a remote method invocation via JGroups.
 *
 * @author Slavomir Krupa (slavomir.krupa@gmail.com)
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class ClusterMicroserviceProvider implements MicroserviceProvider, ClusterSilverService {

   private static final Logger log = LogManager.getLogger(ClusterMicroserviceProvider.class);

   private Context context;
   private RemoteServiceHandlesStore remoteServiceHandlesStore;
   private Map<MicroserviceMetaData, Set<Address>> alreadyQueriedAddresses = new HashMap<>();

   private JgroupsMessageSender sender;
   private MessageDispatcher messageDispatcher;
   private Long timeout = 500L;

   @Override
   public void initialize(final Context context) {
      try {
         final Stopwatch stopwatch = Stopwatch.createStarted();
         // do some basic initialization
         this.context = context;
         this.remoteServiceHandlesStore = context.getRemoteServiceHandlesStore();
         this.alreadyQueriedAddresses = new HashMap<>();
         context.getProperties().putIfAbsent(CLUSTER_GROUP, "SilverWare");
         context.getProperties().putIfAbsent(CLUSTER_CONFIGURATION, "udp.xml");
         context.getProperties().putIfAbsent(CLUSTER_LOOKUP_TIMEOUT, timeout);
         // get jgroups configuration
         final String clusterGroup = (String) this.context.getProperties().get(CLUSTER_GROUP);
         final String clusterConfiguration = (String) this.context.getProperties().get(CLUSTER_CONFIGURATION);
         this.timeout = Long.valueOf(this.context.getProperties().get(CLUSTER_LOOKUP_TIMEOUT).toString());
         log.info("Hello from Cluster microservice provider!");
         log.info("Loading cluster configuration from: {} ", clusterConfiguration);
         JChannel channel = new JChannel(clusterConfiguration);
         JgroupsMessageReceiver receiver = new JgroupsMessageReceiver(KnownImplementation.initializeReponders(context), remoteServiceHandlesStore);
         this.messageDispatcher = new MessageDispatcher(channel, receiver, receiver, receiver);
         this.sender = new JgroupsMessageSender(this.messageDispatcher);
         log.info("Setting cluster group: {} ", clusterGroup);
         channel.connect(clusterGroup);
         receiver.setMyAddress(channel.getAddress());
         stopwatch.stop();
         log.info("Initialization of ClusterMicroserviceProvider took {} ms. ", stopwatch.elapsed(TimeUnit.MILLISECONDS));
      } catch (Exception e) {
         log.error("Cluster microservice initialization failed.", e);
         throw new SilverWareClusteringException(INITIALIZATION_ERROR, e);
      }
   }

   @Override
   public Context getContext() {
      return context;
   }

   @Override
   public void run() {
      try {
         while (!Thread.currentThread().isInterrupted()) {

         }
      } catch (final Exception e) {
         log.error("Cluster microservice provider failed.", e);
      } finally {
         log.info("Bye from Cluster microservice provider!");
         try {
            this.messageDispatcher.close();
         } catch (IOException e) {
            throw new SilverWareClusteringException(JGROUPS_ERROR, "Unexpected error while closing MessageDispatcher", e);
         }
      }
   }

   @Override
   public Set<Object> lookupMicroservice(final MicroserviceMetaData metaData) {
      try {
         Set<Address> addressesForMetadata = alreadyQueriedAddresses.getOrDefault(metaData, new HashSet<>());
         this.sender.sendToClusterAsync(metaData, addressesForMetadata,
               new FutureListenerHelper<MicroserviceSearchResponse>(rspList -> {
                  try {
                     RspList<MicroserviceSearchResponse> responseRspList = rspList.get(10, TimeUnit.SECONDS);
                     if (log.isTraceEnabled()) {
                        log.trace("Response retrieved!  {}", responseRspList);
                     }
                     Collection<Rsp<MicroserviceSearchResponse>> result = responseRspList.values();
                     if (log.isTraceEnabled()) {
                        log.trace("Size of a responses is : {} ", responseRspList.getResults().size());
                     }
                     result.stream().filter(Rsp::hasException).forEach(rsp -> log.error("Exception was thrown during lookup on node: " + rsp.getSender(), rsp.getException()));

                     Set<ServiceHandle> remoteServiceHandles = result.stream()
                                                                     .filter(rsp -> rsp.wasReceived() && !rsp.hasException() && rsp.getValue().getResult().canBeUsed())
                                                                     .map((rsp) -> new RemoteServiceHandle(rsp.getSender(), rsp.getValue().getHandle(), sender, metaData))
                                                                     .collect(Collectors.toSet());
                     // this is to save jgroups traffic for a given metadata
                     addressesForMetadata.addAll(responseRspList.values().stream().map(Rsp::getSender).collect(Collectors.toSet()));
                     alreadyQueriedAddresses.put(metaData, addressesForMetadata);
                     this.remoteServiceHandlesStore.addHandles(metaData, remoteServiceHandles);
                  } catch (Throwable e) {
                     log.error("Error while looking up microservices.", e);
                  }

               }));
         // If this is first query for the metadata we should wait for a response
         if (addressesForMetadata.isEmpty()) {
            Thread.sleep(timeout);
         }

         return this.remoteServiceHandlesStore.getServices(metaData);
      } catch (Throwable e) {
         log.error("Error while looking up microservices.", e);
         return emptySet();
      }
   }

   @Override
   public Set<Object> lookupLocalMicroservice(final MicroserviceMetaData metaData) {
      return emptySet();
   }

}