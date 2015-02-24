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
import org.silverware.microservices.annotations.Microservice;
import org.silverware.microservices.providers.MicroserviceProvider;
import org.silverware.microservices.util.Utils;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionTarget;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class CdiMicroserviceProvider implements MicroserviceProvider {

   private static final Logger log = LogManager.getLogger(CdiMicroserviceProvider.class);

   public static final String BEAN_MANAGER = "silverware.cdi.beanManager";
   public static final String CDI_CONTAINER = "silverware.cdi.container";

   private Context context;

   @Override
   public void initialize(final Context context) {
      this.context = context;
    //  cheatLoggerProviderToWeld();
   }

   @Override
   public void run() {
      try {
         log.info("Hello from CDI microservice provider!");

         final Weld weld = new Weld();
         weld.addExtension(new MicroservicesExtension());

         final WeldContainer container = weld.initialize();
         context.getProperties().put(BEAN_MANAGER, container.getBeanManager());
         context.getProperties().put(CDI_CONTAINER, container);

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

   public static Object getMicroservice(final Context context, final Class clazz) {
      return ((WeldContainer) context.getProperties().get(CDI_CONTAINER)).instance().select(clazz).get();
   }

   public static final class MicroservicesExtension implements Extension {

      public <T> void initializePropertyLoading(final @Observes ProcessInjectionTarget<T> pit) {
         log.info("Observed " + pit.getInjectionTarget().toString());
      }

      public <T, X> void injectionPoint(final @Observes ProcessInjectionPoint<T, X> pip) {
         if (log.isTraceEnabled()) {
            final InjectionPoint injectionPoint = pip.getInjectionPoint();
            final StringBuilder sb = new StringBuilder();

            sb.append("annotated ");
            sb.append(injectionPoint.getAnnotated().toString());
            sb.append("\n");
            sb.append("bean ");
            sb.append(injectionPoint.getBean().toString());
            sb.append("\n");
            sb.append("member ");
            sb.append(injectionPoint.getMember().toString());
            sb.append("\n");
            sb.append("qualifiers ");
            sb.append(injectionPoint.getQualifiers().toString());
            sb.append("\n");
            sb.append("type ");
            sb.append(injectionPoint.getType().toString());
            sb.append("\n");
            sb.append("isDelegate ");
            sb.append(injectionPoint.isDelegate());
            sb.append("\n");
            sb.append("isTransient ");
            sb.append(injectionPoint.isTransient());
            sb.append("\n");

            Bean<?> bean = injectionPoint.getBean();

            sb.append("bean.beanClass ");
            sb.append(bean.getBeanClass().toString());
            sb.append("\n");
            //         System.out.println("bean.injectionPoints " + bean.getInjectionPoints());
            sb.append("bean.name ");
            sb.append(bean.getName());
            sb.append("\n");
            sb.append("bean.qualifiers ");
            sb.append(bean.getQualifiers().toString());
            sb.append("\n");
            sb.append("bean.scope ");
            sb.append(bean.getScope().toString());
            sb.append("\n");
            sb.append("bean.stereotypes ");
            sb.append(bean.getStereotypes().toString());
            sb.append("\n");
            sb.append("bean.types ");
            sb.append(bean.getTypes().toString());
            sb.append("\n");

            Annotated annotated = injectionPoint.getAnnotated();
            sb.append("annotated.annotations ");
            sb.append(annotated.getAnnotations().toString());
            sb.append("\n");
            sb.append("annotated.baseType ");
            sb.append(annotated.getBaseType().toString());
            sb.append("\n");
            sb.append("annotated.typeClosure ");
            sb.append(annotated.getTypeClosure().toString());
            sb.append("\n");

            log.trace(sb.toString());
         }
      }

      public void afterBeanDiscovery(final @Observes AfterBeanDiscovery event, BeanManager manager) {
         event.addContext(new MicroserviceContext());
      }

   }

}
