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

import org.jboss.weld.bootstrap.api.helpers.RegistrySingletonProvider;
import org.jboss.weld.context.AbstractManagedContext;
import org.jboss.weld.context.beanstore.BeanStore;
import org.jboss.weld.context.beanstore.HashMapBeanStore;
import org.silverware.microservices.annotations.MicroserviceScoped;

import java.lang.annotation.Annotation;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class MicroserviceContext extends AbstractManagedContext {

   private BeanStore beanStore = new HashMapBeanStore();

   public MicroserviceContext(String contextId) {
      super(contextId, false);
   }

   public MicroserviceContext() {
      this(RegistrySingletonProvider.STATIC_INSTANCE);
   }

   @Override
   protected BeanStore getBeanStore() {
      return beanStore;
   }

   @Override
   public Class<? extends Annotation> getScope() {
      return MicroserviceScoped.class;
   }

   @Override
   public boolean isActive() {
      return true;
   }
}
