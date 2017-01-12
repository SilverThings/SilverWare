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

import static org.assertj.core.api.Assertions.assertThat;

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;

import org.kie.api.cdi.KSession;
import org.kie.api.runtime.KieSession;
import org.testng.annotations.Test;

import javax.inject.Inject;

public class DroolsIntegrationTest extends SilverWareTestBase {

   public DroolsIntegrationTest() {
      super(DroolsIntegrationTest.class.getPackage().getName(), CdiMicroserviceProvider.class.getPackage().getName());
   }

   @Test
   public void testDrools() {
      DroolsMicroservice droolsMicroservice = lookupBean(DroolsMicroservice.class);
      KieSession ksession = droolsMicroservice.ksession;

      DroolsCheck droolsCheck = new DroolsCheck();
      ksession.insert(droolsCheck);
      ksession.fireAllRules();

      assertThat(droolsCheck.isRuleExecuted()).isTrue();
   }

   @Microservice
   public static class DroolsMicroservice {

      @Inject
      @KSession
      private KieSession ksession;

   }
}
