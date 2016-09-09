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
package io.silverware.microservices.providers.cluster.internal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.util.FutureListener;
import org.jgroups.util.RspList;
import org.jgroups.util.Util;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class implementing functionality for sending messages
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class JgroupsMessageSender {
   /**
    * Logger.
    */
   private static final Logger log = LogManager.getLogger(JgroupsMessageSender.class);


   private static RequestOptions SYNC_OPTIONS = RequestOptions.SYNC();
   private static RequestOptions ASYNC_OPTIONS = RequestOptions.ASYNC();


   private MessageDispatcher dispatcher;
   private Set<Address> filteredAdresses;

   public JgroupsMessageSender(MessageDispatcher dispatcher) {
      if (dispatcher == null) {
         throw new IllegalArgumentException("dispatcher");
      }
      this.dispatcher = dispatcher;
   }

   private Set<Address> getFilteredAdresses() {
      if (this.filteredAdresses == null) {
         this.filteredAdresses = new HashSet<>();
         // add my address
         filteredAdresses.add(this.dispatcher.getChannel().getAddress());
         // add cluster address
         filteredAdresses.add(this.dispatcher.getChannel().getView().getCreator());
      }
      return this.filteredAdresses;
   }

   /**
    * Send multicast message to all nodes in cluster
    *
    * @param content content of message
    */
   public <T> RspList<T> sendToClusterSync(Serializable content) throws Exception {
      return this.dispatcher.castMessage(getMembersAdresses(), new Message(null, content), SYNC_OPTIONS);
   }

   /**
    * Send async multicast message to all nodes in cluster
    *
    * @param content  content of message
    * @param listener listener which will be called when result will be available
    */
   public <T> void sendToClusterAsync(Serializable content, Set<Address> addressesToSkip, FutureListener<RspList<T>> listener) throws Exception {
      List<Address> othertMembersAdresses = getOthertMembersAdresses().stream().filter(address -> !addressesToSkip.contains(address)).collect(Collectors.toList());
      if (!othertMembersAdresses.isEmpty()) {
         this.dispatcher.castMessageWithFuture(othertMembersAdresses, new Message(null, content), SYNC_OPTIONS, listener);
      } else {
         log.debug("No message sent.");
      }
   }

   /**
    * Send async multicast message to all nodes in cluster
    *
    * @param content  content of message
    * @param listener listener which will be called when result will be available
    */
   public <T> void sendToClusterAsync(Serializable content, FutureListener<RspList<T>> listener) throws Exception {
      sendToClusterAsync(content, Collections.emptySet(), listener);
   }

   private List<Address> getMembersAdresses() {
      return this.dispatcher.getChannel().getView().getMembers();
   }

   private List<Address> getOthertMembersAdresses() {
      Set<Address> filteredAdresses = getFilteredAdresses();
      return this.getMembersAdresses().stream().filter(address -> !filteredAdresses.contains(address)).collect(Collectors.toList());
   }

   /**
    * Send unicast message for specific address
    *
    * @param content content of message
    */
   public void sendToAddressAsync(Address address, Serializable content) throws Exception {
      this.dispatcher.sendMessage(new Message(address, Util.objectToByteBuffer(content)), ASYNC_OPTIONS);
   }

   /**
    * Send unicast message for specific address
    *
    * @param content content of message
    */
   public <T> T sendToAddressSync(Address address, Serializable content) throws Exception {
      return this.dispatcher.sendMessage(new Message(address, Util.objectToByteBuffer(content)), SYNC_OPTIONS);
   }


}
