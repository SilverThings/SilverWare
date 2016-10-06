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
package io.silverware.microservices.providers.cdi.internal;

import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Priority;

import io.silverware.microservices.util.DeploymentScanner;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

/**
 * Creates a proxy for invoking microservice methods.
 *
 * A classpath is searched for additional method handlers which are executed before DefaultMethodHandler ordered by their priority.
 *
 * @see io.silverware.microservices.providers.cdi.internal.MicroserviceMethodHandler
 * @see io.silverware.microservices.providers.cdi.internal.DefaultMethodHandler
 */
public class MicroserviceProxyFactory {

   private static final List<Class<? extends MicroserviceMethodHandler>> HANDLER_CLASSES;

   static {
      Set<Class<? extends MicroserviceMethodHandler>> handlerClasses = DeploymentScanner.getDefaultInstance().lookupSubtypes(MicroserviceMethodHandler.class);

      HANDLER_CLASSES = handlerClasses.stream()
                                      .filter(handler -> handler != DefaultMethodHandler.class)
                                      .sorted(new MethodHandlerPrioritizer())
                                      .collect(Collectors.toList());
   }

   private MicroserviceProxyFactory() {
   }

   /**
    * Creates a proxy for a given proxy bean.
    *
    * @param proxyBean
    *       proxy bean for which a proxy is being created
    * @param <T>
    *       provided service type
    * @return proxy
    */
   public static <T> T createProxy(final MicroserviceProxyBean proxyBean) {
      try {
         ProxyFactory factory = new ProxyFactory();
         if (proxyBean.getServiceInterface().isInterface()) {
            factory.setInterfaces(new Class[] { proxyBean.getServiceInterface() });
         } else {
            factory.setSuperclass(proxyBean.getServiceInterface());
         }
         MethodHandler methodHandler = createMethodHandler(proxyBean);
         return (T) factory.create(new Class[0], new Object[0], methodHandler);
      } catch (Exception e) {
         throw new IllegalStateException("Cannot create proxy for class " + proxyBean.getServiceInterface().getName() + ": ", e);
      }
   }

   private static MethodHandler createMethodHandler(MicroserviceProxyBean parentBean) throws Exception {
      MicroserviceMethodHandler methodHandler = new DefaultMethodHandler(parentBean);
      for (Class<? extends MicroserviceMethodHandler> handlerClass : HANDLER_CLASSES) {
         final Constructor c = handlerClass.getConstructor(MicroserviceMethodHandler.class);
         methodHandler = (MicroserviceMethodHandler) c.newInstance(methodHandler);
      }
      return methodHandler;
   }

   private static class MethodHandlerPrioritizer implements Comparator<Class<? extends MicroserviceMethodHandler>> {

      @Override
      public int compare(final Class<? extends MicroserviceMethodHandler> class1, final Class<? extends MicroserviceMethodHandler> class2) {
         return getPriority(class2) - getPriority(class1);
      }

      private int getPriority(Class<? extends MicroserviceMethodHandler> methodHandlerClass) {
         Priority priority = methodHandlerClass.getAnnotation(Priority.class);
         return priority != null ? priority.value() : MicroserviceMethodHandler.DEFAULT_PRIORITY;
      }

   }

}
