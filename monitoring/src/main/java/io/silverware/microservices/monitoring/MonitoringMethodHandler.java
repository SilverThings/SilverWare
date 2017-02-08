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
package io.silverware.microservices.monitoring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Priority;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.math.BigDecimal;

import io.silverware.microservices.monitoring.annotations.NotMonitored;
import io.silverware.microservices.providers.cdi.internal.MicroserviceMethodHandler;
import io.silverware.microservices.providers.cdi.internal.MicroserviceProxyBean;

/**
 * This is a method handler for monitoring. It extends {@link MicroserviceMethodHandler} and its priority is {@link MicroserviceMethodHandler#DEFAULT_PRIORITY} - 1.
 */
@Priority(50)
public class MonitoringMethodHandler extends MicroserviceMethodHandler {

   private static final Logger log = LogManager.getLogger(MonitoringMethodHandler.class);

   private final MicroserviceMethodHandler methodHandler;

   private MBeanServer mbs = null;
   private ObjectName jmxPublisherName = null;

   public MonitoringMethodHandler(MicroserviceMethodHandler microserviceMethodHandler) {
      registerPublisher();
      this.methodHandler = microserviceMethodHandler;
   }

   /**
    * Method by default measures runtime microservice method. If {@link NotMonitored} annotation is present, runtime is not monitored.
    *
    * @param method method to be invoked.
    * @param args method arguments.
    * @return invocation result.
    * @throws Exception throws exception from parent {@link MicroserviceMethodHandler}.
    */
   @Override
   public Object invoke(Method method, Object... args) throws Exception {

      if (method.isAnnotationPresent(NotMonitored.class)) {
         log.info("NotMonitored annotation present. Method: " + method);
         return methodHandler.invoke(method, args);
      }

      final long startTime = System.nanoTime();

      log.info("Microservice is monitored. Method: " + method);

      final Object invokeResult = methodHandler.invoke(method, args);

      final long stopTime = System.nanoTime();

      final BigDecimal bigDecimal = new BigDecimal(stopTime - startTime);

      MetricsAggregator.addMicroserviceMethod(method, bigDecimal);

      return invokeResult;
   }

   @Override
   public MicroserviceProxyBean getProxyBean() {
      return methodHandler.getProxyBean();
   }

   @Override
   public InjectionPoint getInjectionPoint() {
      return methodHandler.getInjectionPoint();
   }

   private void registerPublisher() {

      // Get the platform MBeanServer
      mbs = ManagementFactory.getPlatformMBeanServer();

      // Unique identification of MBeans
      JMXPublisherMBean jmxPublisherMBean = new JMXPublisher();

      try {
         jmxPublisherName = new ObjectName("JMX_Publisher:name=JMXPublisherBean");
         if (!mbs.isRegistered(jmxPublisherName)) {
            mbs.registerMBean(jmxPublisherMBean, jmxPublisherName);
         }
      } catch (Exception e) {
         log.error("Unable to reguster publisher: ", e);
      }
   }
}
