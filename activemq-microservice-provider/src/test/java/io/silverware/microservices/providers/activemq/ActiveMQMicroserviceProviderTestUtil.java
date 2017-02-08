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

import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.util.BootUtil;

import java.net.URISyntaxException;

import javax.jms.JMSConsumer;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;

/**
 * @author <a href="mailto:stefankomartin6@gmail.com">Martin Štefanko</a>
 */
public class ActiveMQMicroserviceProviderTestUtil {

   public static void waitFotPlatformStart(final BootUtil bootUtil) throws InterruptedException {

      while(bootUtil.getContext().getProperties().get(CdiMicroserviceProvider.BEAN_MANAGER) == null) {
         Thread.sleep(200);
      }
   }

   public static String getResourcePath(String uriString) throws URISyntaxException {
      return ClassLoader.getSystemResource(uriString).toURI().toString();
   }

   public static boolean receiveMessage11(MessageConsumer consumer) throws JMSException {
      TextMessage message = (TextMessage) consumer.receive(1000);
      if (message != null) {
         System.out.println("RECEIVED MESSAGE: " + message.getText());
         return true;
      }

      return false;
   }

   public static boolean receiveMessage20(JMSConsumer consumer) throws JMSException {
      String textMessage = consumer.receiveBody(String.class, 1000);
      if (textMessage != null) {
         System.out.println("RECEIVED MESSAGE: " + textMessage);
         return true;
      }

      return false;
   }
}
