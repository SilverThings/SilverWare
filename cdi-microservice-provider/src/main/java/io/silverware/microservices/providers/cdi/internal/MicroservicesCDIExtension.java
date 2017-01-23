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

import io.silverware.microservices.Context;
import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.providers.cdi.MicroserviceContext;
import io.silverware.microservices.providers.cdi.util.AnnotationUtil;
import io.silverware.microservices.providers.cdi.util.VersionResolver;
import io.silverware.microservices.util.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
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
   private final String weldName;

   /**
    * List of created {@link MicroserviceProxyBean} instances.
    */
   private final List<MicroserviceProxyBean> microserviceProxyBeans = new ArrayList<>();

   public MicroservicesCDIExtension(final Context context) {
      this.context = context;
      this.weldName = String.valueOf(context.getProperties().get(Context.WELD_NAME));
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
      log.debug("CDI Bean discovery process started.");
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

      // Create a registry of microservices
      if (isMicroserviceBean(bean)) {
         final Microservice annotation = bean.getBeanClass().getAnnotation(Microservice.class);
         final String microserviceName = !annotation.value().isEmpty() ? annotation.value() : Utils.toLowerCamelCase(bean.getBeanClass().getSimpleName());

         this.context.registerMicroservice(getMicroserviceMetaData(microserviceName, bean));
      }

      // Create proxies for the corresponding injection points
      bean.getInjectionPoints().stream().filter(MicroservicesCDIExtension::hasMicroserviceReference).forEach(injectionPoint -> {
         log.trace("Creating proxy bean for injection point: {}", injectionPoint);
         addMicroserviceProxyBean(injectionPoint, beanManager);
         injectionPointsCount++;
      });
   }

   private static boolean isMicroserviceBean(final Bean<?> bean) {
      // we do not want to register the injection points
      return bean.getBeanClass().isAnnotationPresent(Microservice.class) && !AnnotationUtil.containsAnnotation(bean.getQualifiers(), MicroserviceReference.class);
   }

   private static boolean hasMicroserviceReference(final InjectionPoint injectionPoint) {
      return AnnotationUtil.containsAnnotation(injectionPoint.getQualifiers(), MicroserviceReference.class);
   }

   private void addMicroserviceProxyBean(final InjectionPoint injectionPoint, final BeanManager beanManager) {
      final String serviceName = createMicroserviceName(injectionPoint);
      final Class<?> serviceInterface = ((Field) injectionPoint.getMember()).getType();
      final Set<Annotation> qualifiers = prepareQualifiers(injectionPoint.getQualifiers());

      boolean beanExists = microserviceProxyBeans.stream()
                                                 .filter(bean -> bean.getMicroserviceName().equals(serviceName))
                                                 .filter(bean -> bean.getServiceInterface().equals(serviceInterface))
                                                 .anyMatch(bean -> bean.getQualifiers().equals(qualifiers));

      if (!beanExists) {
         MicroserviceProxyBean proxyBean = new MicroserviceProxyBean(serviceName, serviceInterface, qualifiers, beanManager, context);
         microserviceProxyBeans.add(proxyBean);
      }
   }

   private static String createMicroserviceName(final InjectionPoint injectionPoint) {
      final MicroserviceReference microserviceReference = AnnotationUtil.findAnnotation(injectionPoint.getQualifiers(), MicroserviceReference.class).get();

      // first try to use a user defined service name
      if (microserviceReference.value().length() > 0) {
         return microserviceReference.value();
      } else {
         Class<?> serviceInterface = ((Field) injectionPoint.getMember()).getType();
         return toLowerCamelCase(serviceInterface.getSimpleName());
      }
   }

   private static String toLowerCamelCase(String upperCamelCase) {
      return upperCamelCase.substring(0, 1).toLowerCase() + upperCamelCase.substring(1);
   }

   private static Set<Annotation> prepareQualifiers(final Set<Annotation> beanQualifiers) {
      Set<Annotation> qualifiers = new HashSet<>(beanQualifiers);

      if (!AnnotationUtil.containsAnnotation(qualifiers, Any.class)) {
         qualifiers.add(new AnyAnnotationLiteral());
      }

      // add @Default if contains only @Any and @MicroserviceReference
      if (qualifiers.size() < 3) {
         qualifiers.add(new DefaultAnnotationLiteral());
      }

      return Collections.unmodifiableSet(qualifiers);
   }

   /**
    * {@link javax.enterprise.inject.spi.ProcessBean} CDI event observer.
    *
    * @param afterEvent
    *       CDI Event instance.
    */
   public void afterBeanDiscovery(@Observes final AfterBeanDiscovery afterEvent) {
      afterEvent.addContext(new MicroserviceContext(weldName));

      this.microserviceProxyBeans.forEach(proxyBean -> {
         log.trace("Registering client proxy bean for bean service {}. Microservice type is {}.", proxyBean.getMicroserviceName(), proxyBean.getServiceInterface().getName());

         afterEvent.addBean(proxyBean);
      });

      log.debug("CDI Bean discovery process completed.");
   }

   /**
    * Gets the number of discovered injection points.
    *
    * @return The number of discovered injection points.
    */
   public long getInjectionPointsCount() {
      return injectionPointsCount;
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
      return VersionResolver.getInstance().createMicroserviceMetadataForBeans(microserviceName, bean.getBeanClass(), bean.getQualifiers(), bean.getBeanClass().getAnnotations());
   }

   /**
    * Annotation literal for type Any.
    */
   private static class AnyAnnotationLiteral extends AnnotationLiteral<Any> {

   }

   /**
    * Annotation literal for type Default that can be serialized.
    */
   private static class DefaultAnnotationLiteral extends AnnotationLiteral<Default> {

   }

}