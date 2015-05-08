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
import org.silverware.microservices.Context;
import org.silverware.microservices.MicroserviceMetaData;
import org.silverware.microservices.annotations.Microservice;
import org.silverware.microservices.annotations.MicroserviceReference;
import org.silverware.microservices.providers.cdi.MicroserviceContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.util.AnnotationLiteral;

/**
 * CDI Extension for Microservices.
 *
 * @author Martin Večeřa <marvenec@gmail.com>
 */
@ApplicationScoped
public class MicroservicesCDIExtension implements Extension {

   /**
    * Logger.
    */
   private static Logger log = LogManager.getLogger(MicroservicesCDIExtension.class);

   /**
    * Number of discovered injection points.
    */
   private long injectionPointsCount = 0;

   /**
    * Microservices context.
    */
   private final Context context;

   /**
    * List of created {@link MicroserviceProxyBean} instances.
    */
   private List<MicroserviceProxyBean> microserviceProxyBeans = new ArrayList<>();

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
   public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeEvent, BeanManager beanManager) {
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
      if (bean.getBeanClass().isAnnotationPresent(Microservice.class)) {
         // we do not want to register the injection points
         if (bean.getQualifiers().stream().filter(qualifier -> qualifier.annotationType().isAssignableFrom(MicroserviceReference.class)).count() == 0) {
            Microservice annotation = bean.getBeanClass().getAnnotation(Microservice.class);
            final String microserviceName = annotation.value().length() > 0 ? annotation.value() : bean.getBeanClass().getSimpleName();

            context.registerMicroservice(getMicroserviceMetaData(microserviceName, bean));
         }
      }

      // Create proxies for the corresponding injection points
      final Set<InjectionPoint> injectionPoints = bean.getInjectionPoints();
      for (InjectionPoint injectionPoint : injectionPoints) {
         final Set<Annotation> annotations = injectionPoint.getAnnotated().getAnnotations();
         injectionPoint.getQualifiers().stream().filter(qualifier -> MicroserviceReference.class.isAssignableFrom(qualifier.annotationType())).forEach(qualifier -> {
            final Member member = injectionPoint.getMember();
            if (member instanceof Field) {
               if (log.isTraceEnabled()) {
                  log.trace("Creating proxy bean for injection point: " + injectionPoint.toString());
               }
               addInjectableClientProxyBean((Field) member, (MicroserviceReference) qualifier, preProcessQualifiers(injectionPoint.getQualifiers()), annotations, beanManager);
               injectionPointsCount = injectionPointsCount + 1;
            }
         });
      }
   }

   private Set<Annotation> preProcessQualifiers(final Set<Annotation> qualifiers) {
      Set<Annotation> processed = new HashSet<>(qualifiers);
      List<String> current = qualifiers.stream().map(q -> q.annotationType().getName()).collect(Collectors.toList());


      if (!current.contains(Any.class.getName())) {
         processed.add(new AnnotationLiteral<Any>() {
         });
         current.add(Any.class.getName());
      }

      if (current.size() < 3) {
         processed.add(new AnnotationLiteral<Default>() {
         });
      }

      return processed;
   }

   /**
    * {@link javax.enterprise.inject.spi.ProcessBean} CDI event observer.
    *
    * @param afterEvent
    *       CDI Event instance.
    */
   public void afterBeanDiscovery(@Observes final AfterBeanDiscovery afterEvent) {
      afterEvent.addContext(new MicroserviceContext());

      microserviceProxyBeans.forEach(proxyBean -> {
         if (log.isTraceEnabled()) {
            log.trace(String.format("Registering client proxy bean for bean service %s. Microservice type is %s.", proxyBean.getMicroserviceName(), proxyBean.getServiceInterface().getName()));
         }

         afterEvent.addBean(proxyBean);
      });

      if (log.isDebugEnabled()) {
         log.debug("CDI Bean discovery process completed.");
      }
   }

   private void addInjectableClientProxyBean(final Field injectionPointField, final MicroserviceReference microserviceReference, final Set<Annotation> qualifiers, final Set<Annotation> annotations, final BeanManager beanManager) {
      final String serviceName;

      // first try to use a user defined service name
      if (microserviceReference.value().length() > 0) {
         serviceName = microserviceReference.value();
      } else {
         /*if (injectionPointField.getType().isInterface()) { // in case of interface use the property name
            serviceName = injectionPointField.getName();
         } else { // else use the type name as does CDI*/
            final String tmpName = injectionPointField.getType().getSimpleName();
            serviceName = tmpName.substring(0, 1).toLowerCase() + tmpName.substring(1);
         //}
      }

      addClientProxyBean(serviceName, injectionPointField.getType(), qualifiers, annotations);
   }

   private void addClientProxyBean(final String microserviceName, final Class<?> beanClass, final Set<Annotation> qualifiers, final Set<Annotation> annotations) {
      // Do we already have a proxy with this service name and type?
      for (MicroserviceProxyBean microserviceProxyBean : microserviceProxyBeans) {
         if (microserviceName.equals(microserviceProxyBean.getMicroserviceName()) && beanClass == microserviceProxyBean.getBeanClass()) {
            List<String> required = qualifiers.stream().map(q -> q.annotationType().getName()).collect(Collectors.toList());
            List<String> available = microserviceProxyBean.getQualifiers().stream().map(q -> q.annotationType().getName()).collect(Collectors.toList());

            required.forEach(available::remove);

            if (available.size() == 0) {
               // Yes, we have it!
               return;
            }
         }
      }

      // No, we don't. Give us one please!
      final MicroserviceProxyBean microserviceProxyBean = new MicroserviceProxyBean(microserviceName, beanClass, qualifiers, annotations, context);
      microserviceProxyBeans.add(microserviceProxyBean);
   }

   /**
    * Gets the number of discovered injection points.
    * @return The number of discovered injection points.
    */
   public long getInjectionPointsCount() {
      return injectionPointsCount;
   }

   /**
    * Gets a new {@link MicroserviceMetaData} descriptor based on the provided CDI bean.
    * @param microserviceName The Microservice name.
    * @param bean The CDI Bean.
    * @return Microservice meta-data.
    */
   private MicroserviceMetaData getMicroserviceMetaData(final String microserviceName, final Bean<?> bean) {
      return new MicroserviceMetaData(microserviceName, bean.getBeanClass(), bean.getQualifiers());
   }
}