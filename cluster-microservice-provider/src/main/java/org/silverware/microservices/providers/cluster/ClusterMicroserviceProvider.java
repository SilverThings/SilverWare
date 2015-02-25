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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.silverware.microservices.Context;
import org.silverware.microservices.providers.MicroserviceProvider;
import org.silverware.microservices.silver.ClusterSilverService;
import org.silverware.microservices.util.Utils;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class ClusterMicroserviceProvider implements MicroserviceProvider, ClusterSilverService {

   private static final Logger log = LogManager.getLogger(ClusterMicroserviceProvider.class);

   private Context context;
   private JChannel channel;
   private ChannelReceiver receiver;

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
               Thread.sleep(1000);
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

   private static class ChannelReceiver extends ReceiverAdapter {
      @Override
      public void receive(final Message msg) {
         log.info("Received message " + msg);
      }

      @Override
      public void viewAccepted(final View view) {
         log.info("View accepted " + view);
      }
   }
}