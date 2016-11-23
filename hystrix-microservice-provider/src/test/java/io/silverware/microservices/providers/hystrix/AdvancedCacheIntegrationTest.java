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

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.annotations.hystrix.HystrixConfig;
import io.silverware.microservices.annotations.hystrix.advanced.CacheKey;
import io.silverware.microservices.annotations.hystrix.advanced.DefaultProperties;
import io.silverware.microservices.annotations.hystrix.advanced.HystrixCommand;
import io.silverware.microservices.annotations.hystrix.advanced.HystrixProperty;
import io.silverware.microservices.providers.hystrix.configuration.CommandProperties;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;

public class AdvancedCacheIntegrationTest extends SilverWareHystrixTestBase {

   private static final int VALUE = 42;
   private static final int VALUE_2 = 9;

   private static AtomicBoolean cached = new AtomicBoolean(true);

   private AdvancedCachedMicroservice cachedMicroservice;

   @BeforeClass
   public void lookupResilientMicroservice() {
      AdvancedCacheResilientMicroservice resilientMicroservice = lookupBean(AdvancedCacheResilientMicroservice.class);
      cachedMicroservice = resilientMicroservice.advancedCachedMicroservice;
   }

   @Test
   public void testNoCache() {
      cached.set(true);
      int result = cachedMicroservice.noCache(VALUE);
      assertThat(result).isEqualTo(VALUE);
      assertThat(cached.get()).isFalse();

      cached.set(true);
      result = cachedMicroservice.noCache(VALUE);
      assertThat(result).isEqualTo(VALUE);
      assertThat(cached.get()).isFalse();
   }

   @Test
   public void testVoidCache() {
      cached.set(true);
      cachedMicroservice.voidCache(VALUE);
      assertThat(cached.get()).isFalse();

      cached.set(true);
      cachedMicroservice.voidCache(VALUE);
      assertThat(cached.get()).isTrue();
   }

   @Test
   public void testNoKeyCache() {
      cached.set(true);
      int result = cachedMicroservice.noKey(VALUE, VALUE);
      assertThat(result).isEqualTo(VALUE);
      assertThat(cached.get()).isFalse();

      cached.set(true);
      result = cachedMicroservice.noKey(VALUE, VALUE);
      assertThat(result).isEqualTo(VALUE);
      assertThat(cached.get()).isTrue();
   }

   @Test
   public void testSingleKeyCache() {
      cached.set(true);
      int result = cachedMicroservice.singleKey(VALUE, VALUE);
      assertThat(result).isEqualTo(VALUE);
      assertThat(cached.get()).isFalse();

      cached.set(true);
      result = cachedMicroservice.singleKey(VALUE, VALUE_2);
      assertThat(result).isEqualTo(VALUE_2);
      assertThat(cached.get()).isFalse();

      cached.set(true);
      result = cachedMicroservice.singleKey(VALUE_2, VALUE);
      assertThat(result).isEqualTo(VALUE);
      assertThat(cached.get()).isTrue();
   }

   @Microservice
   public static class AdvancedCacheResilientMicroservice {

      @Inject
      @HystrixConfig(AdvancedCachedMicroserviceHystrixConfig.class)
      @MicroserviceReference
      private AdvancedCachedMicroservice advancedCachedMicroservice;

   }

   @Microservice
   public static class AdvancedCachedMicroservice {

      public int noCache(int number) {
         cached.set(false);
         return number;
      }

      public void voidCache(int number) {
         cached.set(false);
      }

      public int noKey(int number1, int number2) {
         cached.set(false);
         return number1;
      }

      public int singleKey(int number1, int number2) {
         cached.set(false);
         return number2;
      }

   }

   @DefaultProperties(
         commandProperties = {
               @HystrixProperty(name = CommandProperties.REQUEST_CACHE_ENABLED, value = "true")
         }
   )
   public static abstract class AdvancedCachedMicroserviceHystrixConfig extends AdvancedCachedMicroservice {

      @HystrixCommand(
            commandProperties = {
                  @HystrixProperty(name = CommandProperties.REQUEST_CACHE_ENABLED, value = "false")
            }
      )
      public abstract int noCache(final int number);

      public abstract void voidCache(final int number);

      public abstract int noKey(final int number1, final int number2);

      public abstract int singleKey(final int number1, @CacheKey final int number2);
   }

}
