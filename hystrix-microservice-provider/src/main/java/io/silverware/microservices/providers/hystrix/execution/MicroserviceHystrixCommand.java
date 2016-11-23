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
package io.silverware.microservices.providers.hystrix.execution;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.exception.HystrixBadRequestException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Generic Hystrix command for microservice method calls execution.
 *
 * @param <T>
 *       type of the result returned by remote call
 */
class MicroserviceHystrixCommand<T> extends HystrixCommand<T> {

   private final Callable<T> callable;
   private final Callable<T> fallback;
   private final String cacheKey;
   private final Set<Class<? extends Throwable>> ignoredExceptions;

   private MicroserviceHystrixCommand(Builder<T> builder) {
      super(builder.setter);

      callable = builder.callable;
      fallback = builder.fallback;
      cacheKey = builder.cacheKey;
      ignoredExceptions = builder.ignoredExceptions;
   }

   @Override
   protected T run() throws Exception {
      try {
         return callable.call();
      } catch (Exception ex) {
         if (isIgnoredException(ex)) {
            throw new HystrixBadRequestException("Exception ignored by Hystrix", ex);
         }
         throw ex;
      }
   }

   private boolean isIgnoredException(final Exception thrownException) {
      Class<? extends Throwable> exceptionClass = thrownException instanceof InvocationTargetException ? thrownException.getCause().getClass() : thrownException.getClass();
      return ignoredExceptions.stream().anyMatch(exception -> exception.isAssignableFrom(exceptionClass));
   }

   @Override
   protected T getFallback() {
      if (fallback == null) {
         return super.getFallback();
      }

      try {
         return fallback.call();
      } catch (Exception ex) {
         throw new IllegalStateException("Fallback has failed", ex);
      }
   }

   @Override
   protected String getCacheKey() {
      return cacheKey;
   }

   /**
    * The builder of this Hystrix command.
    *
    * @param <T>
    *       return type of service method
    */
   public static class Builder<T> {

      private final Setter setter;
      private final Callable<T> callable;
      private String cacheKey;
      private Callable<T> fallback;
      private Set<Class<? extends Throwable>> ignoredExceptions = new HashSet<>();

      public Builder(Setter setter, Callable<T> callable) {
         this.setter = setter;
         this.callable = callable;
      }

      public Builder<T> cacheKey(String cacheKey) {
         this.cacheKey = cacheKey;
         return this;
      }

      public Builder<T> fallback(Callable<T> fallback) {
         this.fallback = fallback;
         return this;
      }

      public Builder<T> ignoredExceptions(Set<Class<? extends Throwable>> ignoredExceptions) {
         this.ignoredExceptions.addAll(ignoredExceptions);
         return this;
      }

      public MicroserviceHystrixCommand<T> build() {
         return new MicroserviceHystrixCommand<>(this);
      }
   }

}
