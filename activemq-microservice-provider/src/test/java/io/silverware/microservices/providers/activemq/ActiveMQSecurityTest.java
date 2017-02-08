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

package io.silverware.microservices.providers.activemq;

import io.silverware.microservices.annotations.JMS;
import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.util.BootUtil;

import org.apache.activemq.artemis.core.config.impl.SecurityConfiguration;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.apache.activemq.artemis.spi.core.security.ActiveMQJAASSecurityManager;
import org.apache.activemq.artemis.spi.core.security.jaas.InVMLoginModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.weld.environment.se.WeldContainer;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import javax.jms.JMSSecurityException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public class ActiveMQSecurityTest {

   private static final Logger log = LogManager.getLogger(ActiveMQSecurityTest.class);

   private static final Semaphore semaphore = new Semaphore(0);

   private EmbeddedJMS jmsServer;
   private static Set<String> checksPassed = new HashSet<>();

   private static final String JMS_11_PREFIX = "JMS 1.1: ";
   private static final String JMS_20_PREFIX = "JMS 2.0: ";

   @BeforeClass
   public void beforeClass() throws Exception {
      jmsServer = new EmbeddedJMS();

      jmsServer.setConfigResourcePath(ActiveMQMicroserviceProviderTestUtil.getResourcePath("brokers/securityTestBroker/broker.xml"));

      //security configuration
      SecurityConfiguration securityConfig = new SecurityConfiguration();
      securityConfig.addUser("admin", "admin");
      securityConfig.addRole("admin", "admin");
      securityConfig.addRole("admin", "user");
      //needed for configuration
      securityConfig.setDefaultUser("foo");
      ActiveMQJAASSecurityManager securityManager = new ActiveMQJAASSecurityManager(InVMLoginModule.class.getName(), securityConfig);
      jmsServer.setSecurityManager(securityManager);

      jmsServer.start();
      log.info("Started Embedded JMS Server");

   }

   @AfterClass
   public void afterClass() throws Exception {
      jmsServer.stop();
      log.info("Embedded JMS Server stopped");
   }


   @Test
   public void testActiveMQSecurity() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName(),
              CdiMicroserviceProvider.class.getPackage().getName());
      platform.start();

      ActiveMQMicroserviceProviderTestUtil.waitFotPlatformStart(bootUtil);

      final WeldContainer container = (WeldContainer) bootUtil.getContext().getProperties().get(CdiMicroserviceProvider.CDI_CONTAINER);
      container.event().select(StartTestEvent.class).fire(new StartTestEvent());

      Assert.assertTrue(semaphore.tryAcquire(10, TimeUnit.MINUTES), "Timeout: Did not received message");
      Assert.assertTrue(semaphore.tryAcquire(10, TimeUnit.MINUTES), "Timeout: Did not received message");

      Assert.assertEquals(checksPassed.size(), 6, "Not all checks have passed:");

      platform.interrupt();
      platform.join();
   }

   public static class StartTestEvent {

   }

   @Microservice
   public static class ActiveMQSecurity11Microservice {

      @Inject
      @MicroserviceReference("authenticatedConnection")
      @JMS(userName = "admin", password = "admin")
      private Connection connection;

      public void observer(@Observes StartTestEvent event) throws Exception {
         log.info("Hello from " + this.getClass().getSimpleName());

         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

         try {
            log.info(JMS_11_PREFIX + "CreateDurableQueue & Consume");
            Queue durableQueue = session.createQueue("durableQueue");

            //access the queue
            MessageConsumer consumer = session.createConsumer(durableQueue);
            consumer.close();

            checksPassed.add(JMS_11_PREFIX + "create durableQueue");
         } catch (JMSSecurityException se) {
            log.error("Unauthorized access: ", se);
         }

         try {
            log.info(JMS_11_PREFIX + "CreateNonDurableQueue & DeleteNonDurableQueue");
            TemporaryQueue temporaryQueue = session.createTemporaryQueue();
            temporaryQueue.delete();

            checksPassed.add(JMS_11_PREFIX + "delete nonDurableQueue");
         } catch (JMSSecurityException se) {
            log.error("Unauthorized access: ", se);
         }

         try {
            log.info(JMS_11_PREFIX + "CreateDurableQueue & Send");
            Queue sendQueue = session.createQueue("sendQueue");

            MessageProducer producer = session.createProducer(sendQueue);
            producer.send(session.createTextMessage());

            producer.close();
            checksPassed.add(JMS_11_PREFIX + "createDurableQueue & Send");
         } catch (JMSSecurityException se) {
            log.error("Unauthorized access: ", se);
         }

         session.close();

         semaphore.release();
      }
   }

   @Microservice
   public static class ActiveMQSecurity20Microservice {

      @Inject
      @MicroserviceReference("authenticatedJMSContext")
      @JMS(userName = "admin", password = "admin")
      private JMSContext jmsContext;

      public void observer(@Observes StartTestEvent event) throws Exception {
         log.info("Hello from " + this.getClass().getSimpleName());

         try {
            log.info(JMS_20_PREFIX + "CreateDurableQueue & Consume");
            Queue durableQueue = jmsContext.createQueue("durableQueue");

            JMSConsumer consumer = jmsContext.createConsumer(durableQueue);
            consumer.close();

            checksPassed.add(JMS_20_PREFIX + "create durableQueue");
         } catch (JMSRuntimeException jmsre) {
            log.error("Unauthorized access: ", jmsre);
         }

         try {
            log.info(JMS_20_PREFIX + "CreateNonDurableQueue & DeleteNonDurableQueue");
            TemporaryQueue temporaryQueue = jmsContext.createTemporaryQueue();
            temporaryQueue.delete();

            checksPassed.add(JMS_20_PREFIX + "delete nonDurableQueue");
         } catch (JMSRuntimeException jmsre) {
            log.error("Unauthorized access: ", jmsre);
         }

         try {
            log.info(JMS_20_PREFIX + "CreateDurableQueue & Send");
            Queue sendQueue = jmsContext.createQueue("sendQueue");

            JMSProducer producer = jmsContext.createProducer();
            producer.send(sendQueue, "");

            checksPassed.add(JMS_20_PREFIX + "createDurableQueue & Send");
         } catch (JMSRuntimeException jmsre) {
            log.error("Unauthorized access: ", jmsre);
         }

         jmsContext.close();

         semaphore.release();
      }
   }
}
