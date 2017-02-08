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
package io.silverware.microservices.providers.cluster.internal.message.response;

import java.io.Serializable;

/**
 * Message sent as response for a {@link io.silverware.microservices.MicroserviceMetaData}
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class MicroserviceSearchResponse implements Serializable {
   /**
    * This enum represents result of a search on a cluster
    */
   public enum Result {
      FOUND(true),
      NOT_FOUND(false),
      WRONG_VERSION(false),
      EXCEPTION_THROWN_DURING_LOOKUP(false),
      MULTIPLE_IMPLEMENTATIONS_FOUND(false);
      private boolean usable;

      Result(boolean canBeUsed) {
         this.usable = canBeUsed;
      }

      public boolean canBeUsed() {
         return usable;
      }
   }

   private final Integer handle;
   private final Result result;

   public MicroserviceSearchResponse(final Result result) {
      handle = null;
      this.result = result;
   }

   public MicroserviceSearchResponse(Integer handle, Result result) {
      this.handle = handle;
      this.result = result;
   }

   public Result getResult() {
      return result;
   }

   public Integer getHandle() {
      return handle;
   }

   @Override
   public String toString() {
      return "MicroserviceSearchResponse{" +
            "handle=" + handle +
            ", result=" + result +
            '}';
   }
}
