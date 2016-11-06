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
package io.silverware.microservices.providers.cluster.internal.util;

import io.silverware.microservices.providers.cluster.internal.message.response.MicroserviceSearchResponse;

import org.jgroups.util.FutureListener;
import org.jgroups.util.RspList;

import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Class that should help you to specify type parameter for your lambda
 *
 * @param <T> type of the future processed in lambda
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class FutureListenerHelper<T> implements FutureListener<RspList<MicroserviceSearchResponse>> {
   private final Consumer<Future<RspList<MicroserviceSearchResponse>>> consumer;

   public FutureListenerHelper(Consumer<Future<RspList<MicroserviceSearchResponse>>> consumer) {
      this.consumer = consumer;
   }

   @Override
   public void futureDone(Future<RspList<MicroserviceSearchResponse>> future) {
      consumer.accept(future);

   }
}