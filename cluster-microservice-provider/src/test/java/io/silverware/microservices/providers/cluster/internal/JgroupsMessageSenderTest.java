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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import mockit.Capturing;
import mockit.Expectations;
import mockit.Tested;
import mockit.Verifications;

/**
 * Test for JgroupsMessageSender
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class JgroupsMessageSenderTest {

   @Capturing
   private MessageDispatcher dispatcher;
   @Tested
   private JgroupsMessageSender jgroupsMessageSender;

   private Address address = new org.jgroups.util.UUID();

   @BeforeMethod
   public void setUp() throws Exception {
      jgroupsMessageSender = new JgroupsMessageSender(dispatcher);
   }

   @Test
   public void nullChannelTest() {
      assertThatThrownBy(() -> new JgroupsMessageSender(null)).isInstanceOf(IllegalArgumentException.class).withFailMessage("dispatcher");
   }

   @Test
   public void testSendToClusterSync() throws Exception {
      new Expectations(jgroupsMessageSender) {{
         jgroupsMessageSender.getMembersAddresses();
         result = Collections.emptyList();
      }};
      String id = UUID.randomUUID().toString();
      jgroupsMessageSender.sendToClusterSync(id);
      new Verifications() {{
         Message msg;
         RequestOptions options;
         List<Address> addresses;
         dispatcher.castMessage(addresses = withCapture(), msg = withCapture(), options = withCapture());
         assertThat(msg.getObject()).isEqualTo(id);
         assertThat(msg.getDest()).isNull();
         assertThat(options.getMode()).isEqualTo(ResponseMode.GET_ALL);
         assertThat(addresses).isEmpty();
      }};

   }

   @Test
   public void testSendToAddressAsync() throws Exception {
      String id = UUID.randomUUID().toString();
      jgroupsMessageSender.sendToAddressAsync(address, id);
      new Verifications() {{
         Message msg;
         RequestOptions options;
         dispatcher.sendMessage(msg = withCapture(), options = withCapture());
         assertThat(msg.getObject()).isEqualTo(id);
         assertThat(msg.getDest()).isEqualTo(address);
         assertThat(options.getMode()).isEqualTo(ResponseMode.GET_NONE);
      }};

   }

   @Test
   public void testSendToAddressSync() throws Exception {
      String id = UUID.randomUUID().toString();
      jgroupsMessageSender.sendToAddressSync(address, id);
      new Verifications() {{
         Message msg;
         RequestOptions options;
         dispatcher.sendMessage(msg = withCapture(), options = withCapture());
         assertThat(msg.getObject()).isEqualTo(id);
         assertThat(msg.getDest()).isEqualTo(address);
         assertThat(options.getMode()).isEqualTo(ResponseMode.GET_ALL);
      }};

   }

}
