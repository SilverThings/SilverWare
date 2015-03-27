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
package org.silverware.microservices.providers.cdi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.silverware.microservices.Context;
import org.silverware.microservices.MicroserviceMetaData;
import org.silverware.microservices.annotations.Microservice;
import org.silverware.microservices.annotations.MicroserviceReference;
import org.silverware.microservices.providers.MicroserviceProvider;
import org.silverware.microservices.providers.cdi.internal.MicroserviceProxyBean;
import org.silverware.microservices.providers.cdi.internal.MicroservicesCDIExtension;
import org.silverware.microservices.silver.CdiSilverService;
import org.silverware.microservices.util.Utils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Priority;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class CdiMicroserviceProvider implements MicroserviceProvider, CdiSilverService {

   /**
    * Logger.
    */
   private static final Logger log = LogManager.getLogger(CdiMicroserviceProvider.class);

   /**
    * Microservices context.
    */
   private Context context;

   @Override
   public void initialize(final Context context) {
      this.context = context;
   }

   @Override
   public Context getContext() {
      return context;
   }

   @Override
   public void run() {
      try {
         log.info("Hello from CDI microservice provider!");

         final Weld weld = new Weld();
         final MicroservicesCDIExtension microservicesCDIExtension = new MicroservicesCDIExtension(context);
         weld.addExtension(microservicesCDIExtension);

         final WeldContainer container = weld.initialize();
         context.getProperties().put(BEAN_MANAGER, container.getBeanManager());
         context.getProperties().put(CDI_CONTAINER, container);

         log.info("Discovered the following microservice implementations:");
         context.getMicroservices().forEach(metaData -> log.info(" - " + metaData.toString()));

         log.info("Total count of discovered microservice injection points: " + microservicesCDIExtension.getInjectionPointsCount());

         container.event().select(MicroservicesStartedEvent.class).fire(new MicroservicesStartedEvent(context, container.getBeanManager(), container));

         try {
            while (!Thread.currentThread().isInterrupted()) {
               Thread.sleep(1000);
            }
         } catch (InterruptedException ie) {
            Utils.shutdownLog(log, ie);
         } finally {
            weld.shutdown();
         }
      } catch (Exception e) {
         log.error("CDI microservice provider failed: ", e);
      }
   }

   public static Object getMicroserviceInstance(final Context context, final String name, final Class<?> type, final Set<Annotation> qualifiers) {
      final BeanManager beanManager = ((BeanManager) context.getProperties().get(BEAN_MANAGER));
      Set<Bean<?>> beans = beanManager.getBeans(type, qualifiers.toArray(new Annotation[qualifiers.size()]));
      for (Bean<?> bean : beans) {
         if (bean.getBeanClass().isAnnotationPresent(Microservice.class) && !(bean instanceof MicroserviceProxyBean)) {
            final Bean<?> theBean = beanManager.resolve(Collections.singleton(bean));
            return beanManager.getReference(theBean, type, beanManager.createCreationalContext(theBean));
         }
      }

      return null;
   }

   static Object getMicroserviceProxy(final Context context, final Class clazz) {
      return ((WeldContainer) context.getProperties().get(CDI_CONTAINER)).instance().select(clazz).select(new MicroserviceReferenceLiteral("")).get();
   }

   static Object getMicroserviceProxy(final Context context, final Class clazz, final String beanName) {
      return ((WeldContainer) context.getProperties().get(CDI_CONTAINER)).instance().select(clazz).select(new MicroserviceReferenceLiteral(beanName)).get();
   }

   public static Object lookupLocalMicroservice(final Context context, final String name, final Class<?> type, final Set<Annotation> qualifiers) {
      Set<MicroserviceMetaData> microservices = context.getMicroservices();
      List<MicroserviceMetaData> nameMatch = new ArrayList<>();
      List<MicroserviceMetaData> typeMatch = new ArrayList<>();

      microservices.forEach(metaData -> {
         if (name != null) {
            if (metaData.getName() != null && metaData.getName().equals(name)) {
               nameMatch.add(metaData);
            }
         }

         if (type.isAssignableFrom(metaData.getType())) {
            typeMatch.add(metaData);
         }
      });

      return null;
   }

   private static class MicroserviceReferenceLiteral extends AnnotationLiteral<MicroserviceReference> implements MicroserviceReference {

      private final String name;

      public MicroserviceReferenceLiteral(final String name) {
         this.name = name;
      }

      @Override
      public String value() {
         return name;
      }
   }

   @Interceptor()
   @Priority(Interceptor.Priority.APPLICATION)
   public static class LoggingInterceptor {

      @AroundInvoke
      public Object log(final InvocationContext ic) throws Exception {
         log.info("AroundInvoke " + ic.toString());
         return ic.proceed();
      }
   }
}
