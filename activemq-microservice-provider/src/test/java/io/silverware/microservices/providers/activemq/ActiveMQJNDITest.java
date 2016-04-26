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
import io.silverware.microservices.enums.ConnectionType;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.util.BootUtil;

import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
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
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public class ActiveMQJNDITest {

   private static final Logger log = LogManager.getLogger(ActiveMQJNDITest.class);

   private static final Semaphore semaphore = new Semaphore(0);

   private EmbeddedJMS jmsServer;

   @BeforeClass
   public void beforeClass() throws Exception {
      jmsServer = new EmbeddedJMS();

      jmsServer.setConfigResourcePath(ActiveMQMicroserviceProviderTestUtil.getResourcePath("brokers/defaultTestBroker/broker.xml"));

      jmsServer.start();
      log.info("Started Embedded JMS Server");
   }

   @AfterClass
   public void afterClass() throws Exception {
      jmsServer.stop();
      log.info("Embedded JMS Server stopped");
   }


   @Test
   public void testActiveMQ() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName(),
              CdiMicroserviceProvider.class.getPackage().getName());
      platform.start();

      ActiveMQMicroserviceProviderTestUtil.waitFotPlatformStart(bootUtil);

      final WeldContainer container = (WeldContainer) bootUtil.getContext().getProperties().get(CdiMicroserviceProvider.CDI_CONTAINER);
      container.event().select(StartTestEvent.class).fire(new StartTestEvent());

      //should receive 2 messages from JMS 1.1
      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES), "Timeout: Did not receive message in the selected timeout");
      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES), "Timeout: Did not receive message in the selected timeout");

      //another 2 from JMS 2.0
      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES), "Timeout: Did not receive message in the selected timeout");
      Assert.assertTrue(semaphore.tryAcquire(1, TimeUnit.MINUTES), "Timeout: Did not receive message in the selected timeout");

      platform.interrupt();
      platform.join();
   }

   public static class StartTestEvent {

   }

   //Test JMS 1.1
   @Microservice
   public static class ActiveMQMicroservice {

      //JNDI default connection
      @Inject
      @MicroserviceReference("connection")
      private Connection connection;

      @Inject
      @MicroserviceReference("connection1")
      @JMS(connectionType = ConnectionType.INJECTION)
      private Connection connection1;

      @Inject
      @MicroserviceReference("connection2")
      @JMS(connectionType = ConnectionType.INJECTION)
      private Connection connection2;


      public void observer(@Observes StartTestEvent event) throws Exception {
         log.info("Hello from " + this.getClass().getSimpleName());

         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         Session session1 = connection1.createSession(false, Session.AUTO_ACKNOWLEDGE);
         Session session2 = connection2.createSession(false, Session.AUTO_ACKNOWLEDGE);
         Queue queue = session.createQueue("jndiQueue");

         MessageProducer producer = session.createProducer(queue);
         MessageProducer producer1 = session1.createProducer(queue);

         MessageConsumer consumer = session2.createConsumer(queue);

         producer.send(session.createTextMessage("HELLO FROM PRODUCER SENT ON " + new Date()));
         producer1.send(session1.createTextMessage("HELLO FROM PRODUCER1 SENT ON " + new Date()));

         receiveMessage11(consumer);
         receiveMessage11(consumer);


         producer.close();
         producer1.close();
         consumer.close();
         session.close();
         session1.close();
         session2.close();

         semaphore.release();

      }

      private void receiveMessage11(MessageConsumer consumer) throws JMSException {
         if (ActiveMQMicroserviceProviderTestUtil.receiveMessage11(consumer)) {
            semaphore.release();
         }
      }
   }

   //Test JMS 2.0
   @Microservice
   public static class ActiveMQJMS20Microservice {

      @Inject
      @MicroserviceReference("context")
      private JMSContext jmsContext;

      @Inject
      @MicroserviceReference("context1")
      @JMS(connectionType = ConnectionType.INJECTION)
      private JMSContext jmsContext1;

      @Inject
      @MicroserviceReference("context2")
      @JMS(connectionType = ConnectionType.INJECTION)
      private JMSContext jmsContext2;


      public void observer(@Observes StartTestEvent event) throws Exception {
         log.info("Hello from " + this.getClass().getSimpleName());

         Queue queue = jmsContext.createQueue("jms20JNDIQueue");

         JMSProducer jmsProducer1 = jmsContext1.createProducer();
         JMSProducer jmsProducer2 = jmsContext2.createProducer();

         JMSConsumer jmsConsumer = jmsContext.createConsumer(queue);

         jmsProducer1.send(queue, "HELLO FROM JMS 2.0 PRODUCER1 SENT ON " + new Date());
         jmsProducer2.send(queue, "HELLO FROM JMS 2.0 PRODUCER2 SENT ON " + new Date());

         receiveMessage20(jmsConsumer);
         receiveMessage20(jmsConsumer);

         jmsConsumer.close();

      }

      private void receiveMessage20(JMSConsumer consumer) throws JMSException {
         if (ActiveMQMicroserviceProviderTestUtil.receiveMessage20(consumer)) {
            semaphore.release();
         }
      }
   }
}
