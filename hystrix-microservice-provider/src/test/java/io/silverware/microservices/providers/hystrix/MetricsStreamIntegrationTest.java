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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.annotations.hystrix.basic.CircuitBreaker;

import com.netflix.hystrix.exception.HystrixRuntimeException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class MetricsStreamIntegrationTest extends SilverWareHystrixTestBase {

   public MetricsStreamIntegrationTest() {
      super(false);
   }

   @AfterMethod
   public void stopSilverWare() throws Exception {
      shutDownSilverWare();
   }

   @Test
   public void testMetricsStreamDefault() throws Exception {
      startSilverWare(null);
      assertMetricsDeployed(false);
   }

   @Test
   public void testMetricsStreamEnabled() throws Exception {
      startSilverWare(true);
      assertMetricsDeployed(true);
   }

   @Test
   public void testMetricsStreamDisabled() throws Exception {
      startSilverWare(false);
      assertMetricsDeployed(false);
   }

   private void assertMetricsDeployed(boolean metricsEnabled) throws Exception {
      ProviderMicroservice providerMicroservice = lookupBean(ProviderMicroservice.class);
      FailingMicroservice failingMicroservice = providerMicroservice.failingMicroservice;

      assertThatThrownBy(failingMicroservice::call)
            .isInstanceOf(HystrixRuntimeException.class);

      String url = getMetricsStreamUrl();
      Response response = ClientBuilder.newClient().target(url).request().get();
      assertThat(response.getStatusInfo()).isEqualTo(metricsEnabled ? Status.OK : Status.NOT_FOUND);
   }

   @Microservice
   public static class ProviderMicroservice {

      @Inject
      @CircuitBreaker
      @MicroserviceReference
      private FailingMicroservice failingMicroservice;

   }

   @Microservice
   public static class FailingMicroservice {

      public void call() {
         throw new RuntimeException("failing");
      }
   }

}
