/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2015 - 2017 the original author or authors.
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

import static io.silverware.microservices.providers.cdi.util.AnnotationUtil.matches;

import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.providers.cdi.util.VersionResolver;
import io.silverware.microservices.silver.services.LookupStrategy;
import io.silverware.microservices.silver.services.LookupStrategyFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Priority;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Default microservice method handler which is invoked as the last one and makes an actual call on the service instance.
 */
@Priority(Integer.MAX_VALUE)
public class DefaultMethodHandler extends MicroserviceMethodHandler {

   private static final Logger log = LogManager.getLogger(DefaultMethodHandler.class);

   private final MicroserviceProxyBean proxyBean;
   private final InjectionPoint injectionPoint;

   private final LookupStrategy lookupStrategy;

   protected DefaultMethodHandler(final MicroserviceProxyBean proxyBean, final InjectionPoint injectionPoint) throws Exception {
      this.proxyBean = proxyBean;
      this.injectionPoint = injectionPoint;
      final Set<Annotation> qualifiers = proxyBean.getQualifiers().stream()
                                                  .filter(qualifier -> !matches(qualifier, MicroserviceReference.class))
                                                  .collect(Collectors.toSet());
      final MicroserviceMetaData metaData = VersionResolver.getInstance()
                                                           .createMicroserviceMetadataForInjectionPoint(proxyBean.getMicroserviceName(), proxyBean.getServiceInterface(), qualifiers, injectionPoint.getAnnotated().getAnnotations());
      this.lookupStrategy = LookupStrategyFactory.getStrategy(proxyBean.getContext(), metaData, injectionPoint.getAnnotated().getAnnotations());
   }

   private synchronized Object getService() {
      final Object service = lookupStrategy.getService();

      log.debug("Proxy {} matched with service implementation {}.", this, service);

      return service;
   }

   @Override
   public Object invoke(final Method method, final Object... args) throws Exception {
      if (method.getDeclaringClass() == Object.class) {
         final String methodName = method.getName();
         final int paramCount = method.getParameterTypes().length;

         if ("toString".equals(methodName) && paramCount == 0) {
            return "Microservices proxy for " + proxyBean.getServiceInterface().getName();
         } else if ("equals".equals(methodName) && paramCount == 1) {
            return this.equals(args[0]);
         } else if ("hashCode".equals(methodName) && paramCount == 0) {
            return this.hashCode();
         } else if ("getClass".equals(methodName) && paramCount == 0) {
            return proxyBean.getServiceInterface();
         }
      }

      log.debug("Invocation of {}", method);

      Object service = getService();
      return method.invoke(service, args);
   }

   @Override
   public MicroserviceProxyBean getProxyBean() {
      return proxyBean;
   }

   @Override
   public InjectionPoint getInjectionPoint() {
      return injectionPoint;
   }
}
