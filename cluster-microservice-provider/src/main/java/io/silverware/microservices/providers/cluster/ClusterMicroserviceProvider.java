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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.providers.MicroserviceProvider;
import io.silverware.microservices.providers.cluster.internal.HttpServiceProxy;
import io.silverware.microservices.silver.ClusterSilverService;
import io.silverware.microservices.silver.cluster.ServiceHandle;
import io.silverware.microservices.util.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class ClusterMicroserviceProvider implements MicroserviceProvider, ClusterSilverService {

   private static final Logger log = LogManager.getLogger(ClusterMicroserviceProvider.class);

   private Context context;
   private JChannel channel;
   private ChannelReceiver receiver;
   private Map<MicroserviceMetaData, Set<ServiceHandle>> outboundHandles = new ConcurrentHashMap<>();
   private Queue<MicroserviceMetaData> toLookup = new ConcurrentLinkedQueue<>();

   @Override
   public void initialize(final Context context) {
      this.context = context;

      context.getProperties().putIfAbsent(CLUSTER_GROUP, "SilverWare");
      context.getProperties().putIfAbsent(CLUSTER_CONFIGURATION, "udp.xml");

      receiver = new ChannelReceiver();
   }

   @Override
   public Context getContext() {
      return context;
   }

   @Override
   public void run() {
      try {
         log.info("Hello from Cluster microservice provider!");

         channel = new JChannel((String) context.getProperties().get(CLUSTER_CONFIGURATION));
         channel.setReceiver(receiver);

         channel.connect((String) context.getProperties().get(CLUSTER_GROUP));

         channel.send(new Message(null, "Holáryjou!"));

         try {
            while (!Thread.currentThread().isInterrupted()) {
               MicroserviceMetaData metaData = toLookup.poll();
               if (metaData != null) {
                  channel.getView().forEach(address -> {
                     try {
                        List<ServiceHandle> handles = metaData.query(context, address.toString());
                        Set<ServiceHandle> localHandles = handles.stream().map(serviceHandle -> serviceHandle.withProxy(HttpServiceProxy.getProxy(context, serviceHandle))).collect(Collectors.toSet());

                        if (outboundHandles.containsKey(metaData)) {
                           outboundHandles.get(metaData).addAll(localHandles);
                        } else {
                           outboundHandles.put(metaData, localHandles);
                        }
                     } catch (Exception e) {
                        log.info(String.format("Unable to lookup Microservice %s at host %s:", metaData.toString(), address.toString()), e);
                     }
                  });
               } else {
                  Thread.sleep(100);
               }
            }
         } catch (InterruptedException ie) {
            Utils.shutdownLog(log, ie);
         } finally {
            channel.close();
         }
      } catch (Exception e) {
         log.error("Cluster microservice provider failed: ", e);
      }
   }

   @Override
   public Set<Object> lookupMicroservice(final MicroserviceMetaData metaData) {
      toLookup.add(metaData);
      return outboundHandles.get(metaData).stream().map(ServiceHandle::getService).collect(Collectors.toSet());
   }

   @Override
   public Set<Object> lookupLocalMicroservice(final MicroserviceMetaData metaData) {
      return new HashSet<>();
   }

   private class ChannelReceiver extends ReceiverAdapter {
      @Override
      public void receive(final Message msg) {
         log.info("Received message " + msg);
      }

      @Override
      public void viewAccepted(final View view) {
         Set<String> availableNodes = new HashSet<>();
         view.forEach(address -> availableNodes.add(address.toString()));

         outboundHandles.forEach((metaData, serviceHandles) -> {
            Set<ServiceHandle> toBeRemoved = new HashSet<>();

            toBeRemoved.addAll(
                  serviceHandles.stream().filter(serviceHandle ->
                     availableNodes.contains(serviceHandle.getHost())
                  ).collect(Collectors.toSet()));
            serviceHandles.removeAll(toBeRemoved);
         });

         log.info("View accepted " + view);
      }
   }
}