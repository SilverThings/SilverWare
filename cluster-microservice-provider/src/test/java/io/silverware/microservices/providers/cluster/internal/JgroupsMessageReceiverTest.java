/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2016 the original author or authors.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.silverware.microservices.providers.cluster.internal.exception.SilverWareClusteringException;
import io.silverware.microservices.providers.cluster.internal.message.responder.Responder;
import io.silverware.microservices.silver.cluster.RemoteServiceHandlesStore;

import org.jgroups.Message;
import org.jgroups.blocks.MessageDispatcher;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.UUID;

import mockit.Capturing;
import mockit.Verifications;

/**
 * Test for a receiver
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class JgroupsMessageReceiverTest {
   @Capturing
   private MessageDispatcher dispatcher;

   @Capturing
   private Responder responder1;
   @Capturing
   private Responder responder2;
   @Capturing
   private RemoteServiceHandlesStore store;
   public static final Message STRING_MSG = new Message(null, null, "hello");
   public static final Message UUID_MSG = new Message(null, null, UUID.randomUUID());

   @BeforeMethod
   public void setUp() throws Exception {

   }

   @Test
   public void testConstructor() throws Exception {
      assertThatThrownBy(() -> new JgroupsMessageReceiver(null, null)).isInstanceOf(IllegalArgumentException.class);
      assertThatThrownBy(() -> new JgroupsMessageReceiver(new HashMap<>(), null)).isInstanceOf(IllegalArgumentException.class);
      assertThatThrownBy(() -> new JgroupsMessageReceiver(null, new RemoteServiceHandlesStore())).isInstanceOf(IllegalArgumentException.class);
      assertThatThrownBy(() -> new JgroupsMessageReceiver(new HashMap<>(), new RemoteServiceHandlesStore())).isInstanceOf(IllegalArgumentException.class);
   }

   @Test
   public void testAddResponder() throws Exception {
      JgroupsMessageReceiver jgroupsMessageReceiver = new JgroupsMessageReceiver(generateRespondersMap(), new RemoteServiceHandlesStore());
      jgroupsMessageReceiver.addResponder(String.class, responder2);
      jgroupsMessageReceiver.receive(STRING_MSG);
      new Verifications() {{
         responder2.processMessage(STRING_MSG);
         times = 1;
         responder1.processMessage((Message) any);
         times = 0;
      }};
   }

   @Test
   public void testReceiveWithUnknownClassThrowsException() throws Exception {
      HashMap<Class, Responder> classResponderHashMap = generateRespondersMap();
      JgroupsMessageReceiver jgroupsMessageReceiver = new JgroupsMessageReceiver(classResponderHashMap, new RemoteServiceHandlesStore());
      assertThatThrownBy(() -> jgroupsMessageReceiver.receive(UUID_MSG)).isInstanceOf(SilverWareClusteringException.class).hasMessageContaining(UUID.class.getName());
      new Verifications() {{
         responder1.processMessage((Message) any);
         times = 0;
         responder2.processMessage((Message) any);
         times = 0;
      }};
   }

   @Test
   public void testAddAfterMapWasPublishedResponder() throws Exception {
      HashMap<Class, Responder> classResponderHashMap = generateRespondersMap();
      JgroupsMessageReceiver jgroupsMessageReceiver = new JgroupsMessageReceiver(classResponderHashMap, new RemoteServiceHandlesStore());
      classResponderHashMap.put(UUID.class, responder2);
      assertThatThrownBy(() -> jgroupsMessageReceiver.receive(UUID_MSG)).isInstanceOf(SilverWareClusteringException.class).hasMessageContaining(UUID.class.getName());
      jgroupsMessageReceiver.receive(STRING_MSG);
      new Verifications() {{
         responder1.processMessage(STRING_MSG);
         times = 1;
      }};
   }

   private HashMap<Class, Responder> generateRespondersMap() {
      HashMap<Class, Responder> responders = new HashMap<>();
      responders.put(String.class, responder1);
      return responders;
   }

   @Test
   public void testReceive() throws Exception {
      HashMap<Class, Responder> classResponderHashMap = generateRespondersMap();
      classResponderHashMap.put(UUID.class, responder2);
      JgroupsMessageReceiver jgroupsMessageReceiver = new JgroupsMessageReceiver(classResponderHashMap, new RemoteServiceHandlesStore());
      jgroupsMessageReceiver.receive(UUID_MSG);
      jgroupsMessageReceiver.receive(UUID_MSG);
      jgroupsMessageReceiver.receive(STRING_MSG);
      new Verifications() {{
         responder2.processMessage(UUID_MSG);
         times = 2;
         responder1.processMessage(STRING_MSG);
         times = 1;
      }};

   }

   @Test
   public void testViewAccepted() throws Exception {

   }
}