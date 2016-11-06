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
package io.silverware.microservices.providers.cluster.internal;

import static io.silverware.microservices.providers.cluster.internal.exception.SilverWareClusteringException.SilverWareClusteringError.PROCESSING_ERROR;
import static io.silverware.microservices.providers.cluster.internal.exception.SilverWareClusteringException.SilverWareClusteringError.RECIPIENT_SAME_AS_SENDER;
import static io.silverware.microservices.providers.cluster.internal.exception.SilverWareClusteringException.SilverWareClusteringError.UNEXPECTED_CONTENT;

import io.silverware.microservices.providers.cluster.internal.exception.SilverWareClusteringException;
import io.silverware.microservices.providers.cluster.internal.message.responder.Responder;
import io.silverware.microservices.silver.cluster.RemoteServiceHandlesStore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.blocks.RequestHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Class responsible for retrieving messages
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class JgroupsMessageReceiver extends ReceiverAdapter implements RequestHandler {

   private static final Logger log = LogManager.getLogger(JgroupsMessageReceiver.class);
   private Map<Class, Responder> responders = new HashMap<>();
   private final RemoteServiceHandlesStore store;
   private Address myAddress;

   public JgroupsMessageReceiver(Map<Class, Responder> responders, RemoteServiceHandlesStore store) {
      if (responders == null || responders.isEmpty()) {
         throw new IllegalArgumentException("responders");
      }
      if (store == null) {
         throw new IllegalArgumentException("store");
      }
      this.store = store;
      this.responders = new HashMap<>(responders);
   }

   public void setMyAddress(Address myAddress) {
      this.myAddress = myAddress;
   }

   public Responder addResponder(Class clazz, Responder responder) {
      return this.responders.put(clazz, responder);
   }

   @Override
   public void receive(final Message msg) {
      log.error("This method is for user option to send messages!");
      try {
         handle(msg);
      } catch (Exception e) {
         throw new SilverWareClusteringException(PROCESSING_ERROR, e);
      }
   }

   @Override
   public void viewAccepted(final View view) {
      Set<String> addresses = view.getMembers().stream().map(Address::toString).collect(Collectors.toSet());
      store.keepHandlesFor(addresses);
      log.info("Cluster view change: " + view);
   }

   @Override
   public Object handle(Message msg) throws Exception {
      if (msg.getSrc() != null && msg.getSrc().equals(myAddress)) {
         log.error("Skipping message sent from this node.");
         throw new SilverWareClusteringException(RECIPIENT_SAME_AS_SENDER);
      }
      Object content = msg.getObject();
      Responder responder = this.responders.get(content.getClass());
      if (responder != null) {
         return responder.processMessage(msg);
      } else {
         log.error("Unexpected content type : {} and object :  {} ", content.getClass(), content);
         throw new SilverWareClusteringException(UNEXPECTED_CONTENT, content.getClass().toString());
      }

   }
}
