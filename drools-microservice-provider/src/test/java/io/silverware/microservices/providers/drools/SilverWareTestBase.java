/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *
 * Copyright (C) 2017 the original author or authors.
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
package io.silverware.microservices.providers.drools;

import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.util.BootUtil;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.lang.annotation.Annotation;
import javax.enterprise.inject.spi.BeanManager;

public abstract class SilverWareTestBase {

   private static final int THREAD_WAIT = 10000; // milliseconds

   private final boolean startPlatform;
   private final String[] packages;

   private BootUtil bootUtil;
   private Thread platform;

   protected SilverWareTestBase(final String... packages) {
      this(true, packages);
   }

   protected SilverWareTestBase(final boolean startPlatform, final String... packages) {
      this.startPlatform = startPlatform;
      this.packages = packages;
   }

   @BeforeClass
   public void setUp() throws Exception {
      if (startPlatform) {
         startSilverWare();
      }
   }

   @AfterClass
   public void tearDown() throws Exception {
      if (startPlatform) {
         shutDownSilverWare();
      }
   }

   protected void startSilverWare() throws Exception {
      bootUtil = new BootUtil();

      platform = bootUtil.getMicroservicePlatform(packages);
      platform.start();

      waitForBeanManager();
   }

   protected void shutDownSilverWare() throws Exception {
      if (platform != null) {
         platform.interrupt();
         platform.join(THREAD_WAIT);
         platform = null;
      }
   }

   private BeanManager waitForBeanManager() throws InterruptedException {
      BeanManager beanManager = null;
      while (beanManager == null) {
         beanManager = (BeanManager) bootUtil.getContext().getProperties().get(CdiMicroserviceProvider.BEAN_MANAGER);
         Thread.sleep(200);
      }

      return beanManager;
   }

   protected <T> T lookupBean(Class<T> type, Annotation... qualifiers) {
      return ((CdiMicroserviceProvider) bootUtil.getContext().getProvider(CdiMicroserviceProvider.class)).lookupBean(type, qualifiers);
   }
}
