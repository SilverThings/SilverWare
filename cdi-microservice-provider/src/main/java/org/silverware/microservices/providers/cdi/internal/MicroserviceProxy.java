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
package org.silverware.microservices.providers.cdi.internal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silverware.microservices.MicroserviceMetaData;
import org.silverware.microservices.annotations.MicroserviceReference;
import org.silverware.microservices.providers.cdi.CdiMicroserviceProvider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class MicroserviceProxy implements MethodHandler {

   private static final Logger log = LogManager.getLogger(MicroserviceProxy.class);

   private Object service;

   private MicroserviceProxyBean parentBean;

   private MicroserviceProxy(final MicroserviceProxyBean parentBean) throws Exception {
      this.parentBean = parentBean;
   }

   private synchronized Object getService() {
      if (service == null) {
         Set<Annotation> qualifiers = parentBean.getQualifiers().stream().filter(qualifier -> !qualifier.annotationType().getName().equals(MicroserviceReference.class.getName())).collect(Collectors.toSet());

         service = CdiMicroserviceProvider.getMicroserviceInstance(parentBean.getContext(), new MicroserviceMetaData(parentBean.getMicroserviceName(), parentBean.getServiceInterface(), qualifiers));

         if (log.isDebugEnabled()) {
            log.info(String.format("Proxy %s matched with service implementation %s.", this.toString(), service));
         }

         if (service == null) {
            throw new IllegalStateException(String.format("Cannot lookup any implementation for microservice %s.", new MicroserviceMetaData(parentBean.getMicroserviceName(), parentBean.getServiceInterface(), qualifiers)));
         }
      }

      return service;
   }

   @SuppressWarnings("unchecked")
   public static <T> T getProxy(final MicroserviceProxyBean parentBean) {
      try {
         ProxyFactory factory = new ProxyFactory();
         if (parentBean.getServiceInterface().isInterface()) {
            factory.setInterfaces(new Class[] { parentBean.getServiceInterface() });
         } else {
            factory.setSuperclass(parentBean.getServiceInterface());
         }
         return (T) factory.create(new Class[0], new Object[0], new MicroserviceProxy(parentBean));
      } catch (Exception e) {
         throw new IllegalStateException("Cannot create proxy for class " + parentBean.getServiceInterface().getName() + ": ", e);
      }
   }

   @Override
   public Object invoke(final Object o, final Method thisMethod, final Method proceed, final Object[] args) throws Throwable {
      if (thisMethod.getDeclaringClass() == Object.class) {
         final String methodName = thisMethod.getName();
         final int paramCount = thisMethod.getParameterTypes().length;

         if ("toString".equals(methodName) && paramCount == 0) {
            return "Microservices proxy for " + parentBean.getServiceInterface().getName();
         } else if ("equals".equals(methodName) && paramCount == 1) {
            return this.equals(args[0]);
         } else if ("hashCode".equals(methodName) && paramCount == 0) {
            return this.hashCode();
         } else if ("getClass".equals(methodName) && paramCount == 0) {
            return parentBean.getServiceInterface();
         }
      }

      log.info("Invocation of " + thisMethod + ", proceed " + proceed);
      return thisMethod.invoke(getService(), args);
   }
}
