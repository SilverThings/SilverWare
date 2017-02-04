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
package io.silverware.microservices.providers.cdi;

import org.jboss.weld.environment.se.WeldContainer;
import io.silverware.microservices.Context;

import javax.enterprise.inject.spi.BeanManager;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public final class MicroservicesStartedEvent {
   private final Context context;
   private final BeanManager beanManager;
   private final WeldContainer weldContainer;

   public Context getContext() {
      return context;
   }

   public BeanManager getBeanManager() {
      return beanManager;
   }

   public WeldContainer getWeldContainer() {
      return weldContainer;
   }

   public MicroservicesStartedEvent(final Context context, final BeanManager beanManager, final WeldContainer weldContainer) {
      this.context = context;
      this.beanManager = beanManager;
      this.weldContainer = weldContainer;
   }
}
