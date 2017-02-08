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

import io.silverware.microservices.Context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Client Proxy CDI Bean.
 *
 * CDI bean for injecting into consumer beans where the {@link io.silverware.microservices.annotations.MicroserviceReference} is used.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class MicroserviceProxyBean implements Bean {

   /**
    * The target Service.
    */
   private final String microserviceName;

   /**
    * Microservices context.
    */
   private final Context context;

   /**
    * The bean proxy Interface {@link Class} of the bean being proxied.  This class
    * must be one of the Service
    * interfaces implemented by the actual Service bean component.
    */
   private final Class<?> serviceInterface;

   /**
    * CDI bean qualifiers. See CDI Specification.
    */
   private final Set<Annotation> qualifiers;

   private final BeanManager beanManager;

   /**
    * Public constructor.
    *
    * @param beanManager
    *       bean manager
    * @param context
    *       SilverWare context in which we run.
    */
   public MicroserviceProxyBean(final String microserviceName, final Class<?> serviceInterface, final Set<Annotation> qualifiers, final BeanManager beanManager, final Context context) {
      this.microserviceName = microserviceName;
      this.serviceInterface = serviceInterface;
      this.qualifiers = qualifiers;
      this.beanManager = beanManager;
      this.context = context;
   }

   /**
    * Get the name of the proxied Microservice.
    *
    * @return The Service name.
    */
   public String getMicroserviceName() {
      return microserviceName;
   }

   /**
    * Get the Service interface.
    *
    * @return The service interface.
    */
   public Class<?> getServiceInterface() {
      return serviceInterface;
   }

   @Override
   public Set<Type> getTypes() {
      final Set<Type> types = new HashSet<>();
      types.add(serviceInterface);

      if (!serviceInterface.isInterface()) {
         Collections.addAll(types, serviceInterface.getInterfaces());
      }

      types.add(Object.class);

      return types;
   }

   @Override
   public Set<Annotation> getQualifiers() {
      return qualifiers;
   }

   @Override
   public String getName() {
      return null;
   }

   @Override
   public Set<Class<? extends Annotation>> getStereotypes() {
      return Collections.emptySet();
   }

   @Override
   public Class<?> getBeanClass() {
      return serviceInterface;
   }

   @Override
   public boolean isAlternative() {
      return false;
   }

   @Override
   public boolean isNullable() {
      return false;
   }

   @Override
   public Set<InjectionPoint> getInjectionPoints() {
      return Collections.emptySet();
   }

   @Override
   public Class<? extends Annotation> getScope() {
      return Dependent.class;
   }

   @Override
   public Object create(final CreationalContext creationalContext) {
      InjectionPoint injectionPoint = (InjectionPoint) beanManager.getInjectableReference(new MetadataInjectionPoint(this), creationalContext);
      return MicroserviceProxyFactory.createProxy(this, injectionPoint);
   }

   @Override
   public void destroy(final Object instance, final CreationalContext creationalContext) {

   }

   /**
    * Gets Microservices context.
    *
    * @return The Microservices context.
    */
   public Context getContext() {
      return context;
   }

   @Override
   public String toString() {
      return "MicroserviceProxyBean{" +
            "microserviceName='" + microserviceName + '\'' +
            ", context=" + context +
            ", serviceInterface=" + serviceInterface +
            ", qualifiers=" + qualifiers +
            '}';
   }

}
