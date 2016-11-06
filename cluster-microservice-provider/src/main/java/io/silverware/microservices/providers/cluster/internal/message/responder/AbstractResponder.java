/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2010 - 2016 the original author or authors.
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
package io.silverware.microservices.providers.cluster.internal.message.responder;

import io.silverware.microservices.Context;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jgroups.Address;
import org.jgroups.Message;

/**
 * Provides basic functionality and logging of messages.
 *
 * @param <C>
 *       message content type
 * @param <R>
 *       message response type
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public abstract class AbstractResponder<C, R> implements Responder {

   /**
    * Logger.
    */
   protected static Logger log = LogManager.getLogger(AbstractResponder.class);
   protected Context context;

   public AbstractResponder(Context context) {
      this.context = context;

   }

   @Override
   public final R processMessage(Message msg) {
      if (log.isDebugEnabled()) {
         log.debug("Processing msg: " + msg);
      }
      return doProcessMessage(msg.getSrc(), (C) msg.getObject());
   }

   abstract R doProcessMessage(Address source, C content);
}
