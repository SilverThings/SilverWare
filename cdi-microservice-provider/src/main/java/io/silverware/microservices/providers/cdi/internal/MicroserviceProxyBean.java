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
import io.silverware.microservices.annotations.MicroserviceScoped;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
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

   /**
    * CDI bean annotations. Can specify invocation strategy, results caching etc.
    */
   private final Set<Annotation> annotations;

   /**
    * The dynamic proxy bean instance created from the supplied {@link #serviceInterface}.
    */
   private final Object proxyBean;

   /**
    * Public constructor.
    *
    * @param microserviceName
    *       The name of the ESB Service being proxied to.
    * @param proxyInterface
    *       The proxy Interface.
    * @param qualifiers
    *       The CDI bean qualifiers. Copied from the injection point.
    * @param annotations
    *       Annotations from the source code at tha injection point.
    * @param context
    *       SilverWare context in which we run.
    */
   public MicroserviceProxyBean(final String microserviceName, final Class<?> proxyInterface, final Set<Annotation> qualifiers, final Set<Annotation> annotations, final Context context) {
      this.microserviceName = microserviceName;
      this.serviceInterface = proxyInterface;
      this.context = context;

      this.qualifiers = new HashSet<>(qualifiers);
      this.annotations = annotations;

      proxyBean = MicroserviceProxyFactory.createProxy(this);
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
      final Set<Type> types = new HashSet<Type>();
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

   public Set<Annotation> getAnnotations() {
      return annotations;
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
      return MicroserviceScoped.class;
   }

   @Override
   public Object create(final CreationalContext creationalContext) {
      return proxyBean;
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
            ", proxyBean=" + proxyBean +
            '}';
   }
}
