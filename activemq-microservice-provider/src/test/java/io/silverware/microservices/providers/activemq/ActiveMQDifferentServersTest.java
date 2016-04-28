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

package io.silverware.microservices.providers.activemq;

import io.silverware.microservices.annotations.JMS;
import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.util.BootUtil;

import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.apache.activemq.broker.BrokerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.weld.environment.se.WeldContainer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public class ActiveMQDifferentServersTest {

   private static final Logger log = LogManager.getLogger(ActiveMQDifferentServersTest.class);

   private static final Semaphore semaphore = new Semaphore(0);

   private EmbeddedJMS artemisServer;
   private BrokerService activeMQServer;

   @BeforeClass
   public void beforeClass() throws Exception {
      artemisServer = new EmbeddedJMS();

      artemisServer.setConfigResourcePath(ActiveMQMicroserviceProviderTestUtil.getResourcePath("brokers/defaultTestBroker/broker.xml"));

      artemisServer.start();
      log.info("Started Embedded Artemis JMS Server");

      activeMQServer = new BrokerService();
      activeMQServer.setBrokerName("ActiveMQ Embedded Broker");
      activeMQServer.addConnector("vm://1");

      activeMQServer.start();
      log.info("Started Embedded ActiveMQ JMS Server");
   }

   @AfterClass
   public void afterClass() throws Exception {
      artemisServer.stop();
      log.info("Embedded Artemis JMS Server stopped");

      activeMQServer.stop();
      log.info("Embedded ActiveMQ JMS Server stopped");
   }


   @Test
   public void testActiveMQ() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName(),
              CdiMicroserviceProvider.class.getPackage().getName());
      platform.start();

      ActiveMQMicroserviceProviderTestUtil.waitFotPlatformStart(bootUtil);

      final WeldContainer container = (WeldContainer) bootUtil.getContext().getProperties().get(CdiMicroserviceProvider.CDI_CONTAINER);
      container.event().select(MyEvent.class).fire(new MyEvent());

      Assert.assertTrue(semaphore.tryAcquire(10, TimeUnit.MINUTES), "Timeout: Did not receive all messages in selected timeout");
      Assert.assertTrue(semaphore.tryAcquire(10, TimeUnit.MINUTES), "Timeout: Did not receive all messages in selected timeout");

      platform.interrupt();
      platform.join();
   }

   public static class MyEvent {

   }

   @Microservice
   public static class DifferentServersMicroservice {

      @Inject
      @MicroserviceReference("artemisConnection")
      @JMS(serverUri = "vm://0", initialContextFactory = org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory.class)
      private Connection artemisConnection;

      @Inject
      @MicroserviceReference("activeMQConnection")
      @JMS(serverUri = "vm://1", initialContextFactory = org.apache.activemq.jndi.ActiveMQInitialContextFactory.class)
      private Connection activeMQConnection;


      public void observer(@Observes MyEvent event) throws Exception {
         log.info("Hello from " + this.getClass().getSimpleName());

         //Artemis
         Session artemisConnectionSession = artemisConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

         Queue artemisQueue = artemisConnectionSession.createQueue("artemisQueue");

         MessageProducer artemisProducer = artemisConnectionSession.createProducer(artemisQueue);

         MessageConsumer artemisConsumer = artemisConnectionSession.createConsumer(artemisQueue);

         artemisProducer.send(artemisConnectionSession.createTextMessage("HELLO FROM ARTEMIS PRODUCER SENT ON " + new Date()));

         receiveMessage11(artemisConsumer);

         artemisProducer.close();
         artemisConsumer.close();
         artemisConnectionSession.close();

         //AtiveMQ
         Session activeMQConnectionSession = activeMQConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);

         Queue activeMQQueue = activeMQConnectionSession.createQueue("activeMQQueue");

         MessageProducer activeMQProducer = activeMQConnectionSession.createProducer(activeMQQueue);

         MessageConsumer activeMQConsumer = activeMQConnectionSession.createConsumer(activeMQQueue);

         activeMQProducer.send(activeMQConnectionSession.createTextMessage("HELLO FROM ACTIVE MQ PRODUCER SENT ON " + new Date()));

         receiveMessage11(activeMQConsumer);

         activeMQProducer.close();
         activeMQConsumer.close();
         activeMQConnectionSession.close();

      }

      private void receiveMessage11(MessageConsumer consumer) throws JMSException {
         if (ActiveMQMicroserviceProviderTestUtil.receiveMessage11(consumer)) {
            semaphore.release();
         }
      }
   }
}
