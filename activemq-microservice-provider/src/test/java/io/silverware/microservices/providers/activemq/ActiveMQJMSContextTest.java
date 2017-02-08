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

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.JMSContext;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public class ActiveMQJMSContextTest {

   private static final Logger log = LogManager.getLogger(ActiveMQJMSContextTest.class);

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

      Assert.assertTrue(semaphore.tryAcquire(10, TimeUnit.MINUTES), "Timeout: Did not receive all messages in selected timeout");
      Assert.assertTrue(semaphore.tryAcquire(10, TimeUnit.MINUTES), "Timeout: Did not receive all messages in selected timeout");

      platform.interrupt();
      platform.join();
   }

   public static class StartTestEvent {

   }

   @Microservice
   public static class ActiveMQSharedSessionModeMicroservice {

      @Inject
      @MicroserviceReference("defaultContext")
      private JMSContext defaultContext;

      @Inject
      @MicroserviceReference("autoAckContext")
      @JMS(sessionMode = JMSContext.AUTO_ACKNOWLEDGE)
      private JMSContext autoAck;

      @Inject
      @MicroserviceReference("autoAckContext2")
      @JMS(sessionMode = JMSContext.AUTO_ACKNOWLEDGE)
      private JMSContext autoAck2;

      @Inject
      @MicroserviceReference("clientAckContext")
      @JMS(sessionMode = JMSContext.CLIENT_ACKNOWLEDGE)
      private JMSContext clientAck;

      @Inject
      @MicroserviceReference("dupsOkAckContext")
      @JMS(sessionMode = JMSContext.DUPS_OK_ACKNOWLEDGE)
      private JMSContext dupsOkAck;

      @Inject
      @MicroserviceReference("sessionTransContext")
      @JMS(sessionMode = JMSContext.SESSION_TRANSACTED)
      private JMSContext sessionTrans;


      public void observer(@Observes StartTestEvent event) throws Exception {
         log.info("Hello from " + this.getClass().getSimpleName());

         Assert.assertNotNull(defaultContext.getSessionMode());

         Assert.assertEquals(autoAck.getSessionMode(), JMSContext.AUTO_ACKNOWLEDGE, "Invalid session type for shared JMSContext.AUTO_ACKNOWLEDGE");
         Assert.assertEquals(clientAck.getSessionMode(), JMSContext.CLIENT_ACKNOWLEDGE, "Invalid session type for shared JMSContext.CLIENT_ACKNOWLEDGE");
         Assert.assertEquals(dupsOkAck.getSessionMode(), JMSContext.DUPS_OK_ACKNOWLEDGE, "Invalid session type for shared JMSContext.DUPS_OK_ACKNOWLEDGE");
         Assert.assertEquals(sessionTrans.getSessionMode(), JMSContext.SESSION_TRANSACTED, "Invalid session type for shared JMSContext.SESSION_TRANSACTED");

         Assert.assertEquals(autoAck.getSessionMode(), autoAck2.getSessionMode(), "Shared instances differ");

         semaphore.release();
      }
   }

   @Microservice
   public static class ActiveMQNonSharedSessionModeJMSContext {

      @Inject
      @MicroserviceReference("injectionDefaultContext")
      @JMS(connectionType = ConnectionType.INJECTION)
      private JMSContext injectionDefaultContext;

      @Inject
      @MicroserviceReference("injectionAutoAckContext")
      @JMS(connectionType = ConnectionType.INJECTION, sessionMode = JMSContext.AUTO_ACKNOWLEDGE)
      private JMSContext injectionAutoAckContext;

      @Inject
      @MicroserviceReference("injectionClientAckContext")
      @JMS(connectionType = ConnectionType.INJECTION, sessionMode = JMSContext.CLIENT_ACKNOWLEDGE)
      private JMSContext injectionClientAckContext;

      @Inject
      @MicroserviceReference("injectionDupsOkContext")
      @JMS(connectionType = ConnectionType.INJECTION, sessionMode = JMSContext.DUPS_OK_ACKNOWLEDGE)
      private JMSContext injectionDupsOkContext;

      @Inject
      @MicroserviceReference("injectionSessionTransContext")
      @JMS(connectionType = ConnectionType.INJECTION, sessionMode = JMSContext.SESSION_TRANSACTED)
      private JMSContext injectionSessionTransContext;

      public void observer(@Observes StartTestEvent event) throws Exception {
         log.info("Hello from " + this.getClass().getSimpleName());

         Assert.assertNotNull(injectionDefaultContext.getSessionMode());

         Assert.assertEquals(injectionAutoAckContext.getSessionMode(), JMSContext.AUTO_ACKNOWLEDGE, "Invalid session type for non-shared JMSContext.AUTO_ACKNOWLEDGE");
         Assert.assertEquals(injectionClientAckContext.getSessionMode(), JMSContext.CLIENT_ACKNOWLEDGE, "Invalid session type for non-shared JMSContext.CLIENT_ACKNOWLEDGE");
         Assert.assertEquals(injectionDupsOkContext.getSessionMode(), JMSContext.DUPS_OK_ACKNOWLEDGE, "Invalid session type for non-shared JMSContext.DUPS_OK_ACKNOWLEDGE");
         Assert.assertEquals(injectionSessionTransContext.getSessionMode(), JMSContext.SESSION_TRANSACTED, "Invalid session type for non-shared JMSContext.SESSION_TRANSACTED");

         semaphore.release();
      }

   }
}
