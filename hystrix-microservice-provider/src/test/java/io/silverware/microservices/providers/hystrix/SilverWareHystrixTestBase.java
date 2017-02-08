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
package io.silverware.microservices.providers.hystrix;

import io.silverware.microservices.Context;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.providers.http.HttpServerMicroserviceProvider;
import io.silverware.microservices.providers.hystrix.execution.HystrixTestBase;
import io.silverware.microservices.silver.HttpServerSilverService;
import io.silverware.microservices.silver.HystrixSilverService;
import io.silverware.microservices.util.BootUtil;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.lang.annotation.Annotation;
import javax.enterprise.inject.spi.BeanManager;

public abstract class SilverWareHystrixTestBase extends HystrixTestBase {

   private final boolean startPlatform;

   private BootUtil bootUtil;
   private Thread platform;

   protected SilverWareHystrixTestBase() {
      this(true);
   }

   protected SilverWareHystrixTestBase(final boolean startPlatform) {
      this.startPlatform = startPlatform;
   }

   @BeforeClass
   public void setUp() throws Exception {
      if (startPlatform) {
         startSilverWare(null);
      }
   }

   @AfterClass
   public void tearDown() throws Exception {
      if (startPlatform) {
         shutDownSilverWare();
      }
   }

   protected void startSilverWare(Boolean metricsEnabled) throws Exception {
      bootUtil = new BootUtil();

      if (metricsEnabled != null) {
         bootUtil.getContext().getProperties().put(HystrixSilverService.HYSTRIX_METRICS_ENABLED, metricsEnabled.toString());
      }

      platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName(),
            CdiMicroserviceProvider.class.getPackage().getName(),
            HttpServerMicroserviceProvider.class.getPackage().getName());
      platform.start();

      waitForBeanManager();
   }

   protected void shutDownSilverWare() throws Exception {
      if (platform != null) {
         platform.interrupt();
         platform.join();
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

   protected String getMetricsStreamUrl() {
      Context context = bootUtil.getContext();
      Object server = context.getProperties().get(HttpServerSilverService.HTTP_SERVER_ADDRESS);
      Object port = context.getProperties().get(HttpServerSilverService.HTTP_SERVER_PORT);
      Object contextPath = context.getProperties().get(HystrixSilverService.HYSTRIX_METRICS_PATH);
      return String.format("http://%s:%s/%s", server, port, contextPath);
   }

}
