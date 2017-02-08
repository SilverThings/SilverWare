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
package io.silverware.microservices.monitoring;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;

/**
 * @author Tomas Borcin | tborcin@redhat.com | created: 20.12.16.
 */
public class MetricsAgreggatorTest extends MetricsParentTest {

   private Method methodA;
   private Method methodB;

   @BeforeClass
   public void initTest() throws NoSuchMethodException {
      methodA = TestMicroserviceA.class.getMethod("hello");
      methodB = TestMicroserviceB.class.getMethod("world");
   }

   // try to modify read-only register of MetricsAggregator
   @Test(expectedExceptions = UnsupportedOperationException.class)
   public void testNotModifiable() {
      MetricsAggregator.getMicroserviceRegister().clear();
   }

   // try to insert zero value into aggregator
   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testZeroValue() {
      MetricsAggregator.addMicroserviceMethod(methodA, BigDecimal.ZERO);
   }

   // try to insert negative value into aggregator
   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testNegativeValue() {
      MetricsAggregator.addMicroserviceMethod(methodA, getRandomTime().multiply(new BigDecimal(-1)));
   }

   @Test
   public void testInsertion() throws NoSuchMethodException, InterruptedException {

      // test empty Aggregator
      assertEquals(MetricsAggregator.getValuesSize(), "");
      assertEquals(MetricsAggregator.getValues(), "");
      assertEquals(MetricsAggregator.getMetrics(), "");
      assertTrue(MetricsAggregator.getMicroserviceRegister().isEmpty());

      // add some methods with values
      MetricsAggregator.addMicroserviceMethod(methodA, BigDecimal.ONE);
      MetricsAggregator.addMicroserviceMethod(methodB, BigDecimal.ONE);
      MetricsAggregator.addMicroserviceMethod(methodB, BigDecimal.TEN);

      // shouldn't be empty now
      assertTrue(!MetricsAggregator.getMicroserviceRegister().isEmpty());

//      assertEquals(MetricsAggregator.getValuesSize(), "");
//      assertEquals(MetricsAggregator.getValues(), "");
      // should contain exactly one value for method hello
      assertTrue(MetricsAggregator.getValuesSize().contains("TestMicroserviceA::hello: 1"));
      // should contain exactly two values for method world
      assertTrue(MetricsAggregator.getValuesSize().contains("TestMicroserviceB::world: 2"));

      // should contain value 1 for method hello
      assertTrue(MetricsAggregator.getValues().contains("TestMicroserviceA::hello: 1"));
      // should contain value 1 and 10 for method world
      assertTrue(MetricsAggregator.getValues().contains("TestMicroserviceB::world: 1,10"));
   }

   private static class TestMicroserviceA {
      public static String hello() {
         return "Hello";
      }
   }

   private static class TestMicroserviceB {
      public static String world() {
         return "world!";
      }
   }
}
