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
package io.silverware.microservices.providers.cdi.internal;

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.annotations.MicroserviceProxy;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.providers.cdi.MicroserviceContext;
import io.silverware.microservices.providers.cdi.util.AnnotationUtil;
import io.silverware.microservices.providers.cdi.util.VersionResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessInjectionPoint;

/**
 * CDI Extension for Microservices.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
@ApplicationScoped
public class MicroservicesCDIExtension implements Extension {

   /**
    * Logger.
    */
   private static Logger log = LogManager.getLogger(MicroservicesCDIExtension.class);

   /**
    * Microservices context.
    */
   private final Context context;

   /**
    * List of created {@link MicroserviceProxyBean} instances.
    */
   private final List<MicroserviceProxyBean> microserviceProxyBeans = new ArrayList<>();

   public MicroservicesCDIExtension(final Context context) {
      this.context = context;
   }

   /**
    * {@link javax.enterprise.inject.spi.BeforeBeanDiscovery} CDI event observer.
    *
    * @param beforeEvent
    *       CDI Event instance.
    * @param beanManager
    *       CDI Bean Manager instance.
    */
   public void beforeBeanDiscovery(@Observes final BeforeBeanDiscovery beforeEvent, final BeanManager beanManager) {
      if (log.isDebugEnabled()) {
         log.debug("CDI Bean discovery process started.");
      }
   }

   /**
    * {@link javax.enterprise.inject.spi.ProcessBean} CDI event observer.
    *
    * @param processBean
    *       CDI Event instance.
    * @param beanManager
    *       CDI Bean Manager instance.
    */
   public void processBean(@Observes final ProcessBean processBean, final BeanManager beanManager) {
      final Bean<?> bean = processBean.getBean();

      // Create a registry of Microservices
      if (isMicroserviceBean(bean)) {
         final Microservice annotation = bean.getBeanClass().getAnnotation(Microservice.class);
         final String microserviceName = !annotation.value().isEmpty() ? annotation.value() : bean.getBeanClass().getSimpleName();

         this.context.registerMicroservice(getMicroserviceMetaData(microserviceName, bean));
      }

      // Create proxies for the corresponding injection points
      bean.getInjectionPoints().stream().filter(MicroservicesCDIExtension::hasMicroserviceReference).forEach(injectionPoint -> {
         if (log.isTraceEnabled()) {
            log.trace("Creating proxy bean for injection point: " + injectionPoint.toString());
         }

         MicroserviceProxy microserviceProxy = new MicroserviceProxyAnnotationLiteral(injectionPoint);
         injectionPoint.getQualifiers().add(microserviceProxy);

         MicroserviceProxyBean proxyBean = new MicroserviceProxyBean(injectionPoint, context);
         microserviceProxyBeans.add(proxyBean);
      });
   }

   private static boolean isMicroserviceBean(final Bean<?> bean) {
      // we do not want to register the injection points
      return bean.getBeanClass().isAnnotationPresent(Microservice.class) && !AnnotationUtil.containsAnnotation(bean.getQualifiers(), MicroserviceReference.class);
   }

   private static boolean hasMicroserviceReference(final InjectionPoint injectionPoint) {
      return AnnotationUtil.containsAnnotation(injectionPoint.getQualifiers(), MicroserviceReference.class);
   }

   /**
    * {@link javax.enterprise.inject.spi.ProcessBean} CDI event observer.
    *
    * @param afterEvent
    *       CDI Event instance.
    */
   public void afterBeanDiscovery(@Observes final AfterBeanDiscovery afterEvent) {
      afterEvent.addContext(new MicroserviceContext());

      this.microserviceProxyBeans.forEach(proxyBean -> {
         if (log.isTraceEnabled()) {
            log.trace(String.format("Registering client proxy bean for bean service %s. Microservice type is %s.", proxyBean.getMicroserviceName(), proxyBean.getServiceInterface().getName()));
         }

         afterEvent.addBean(proxyBean);
      });

      if (log.isDebugEnabled()) {
         log.debug("CDI Bean discovery process completed.");
      }
   }

   /**
    * Observes injection points and wrap those with {@link io.silverware.microservices.annotations.MicroserviceReference} annotations
    * in {@link io.silverware.microservices.providers.cdi.internal.MicroserviceInjectionPoint}.
    *
    * @param processInjectionPoint
    *       CDI event after injection point discovery
    */
   public void processInjectionPoint(@Observes final ProcessInjectionPoint processInjectionPoint) {
      InjectionPoint injectionPoint = processInjectionPoint.getInjectionPoint();
      if (AnnotationUtil.containsAnnotation(injectionPoint.getQualifiers(), MicroserviceReference.class)) {
         processInjectionPoint.setInjectionPoint(new MicroserviceInjectionPoint(injectionPoint));
      }
   }

   /**
    * Gets the number of discovered injection points.
    *
    * @return The number of discovered injection points.
    */
   public long getInjectionPointsCount() {
      return microserviceProxyBeans.size();
   }

   /**
    * Gets a new {@link MicroserviceMetaData} descriptor based on the provided CDI bean.
    *
    * @param microserviceName
    *       The Microservice name.
    * @param bean
    *       The CDI Bean.
    * @return Microservice meta-data.
    */
   private MicroserviceMetaData getMicroserviceMetaData(final String microserviceName, final Bean<?> bean) {
      return VersionResolver.createMicroserviceMetadata(microserviceName, bean.getBeanClass(), bean.getQualifiers(), new HashSet<>(
            Arrays.asList(bean.getBeanClass().getAnnotations())));
   }

}