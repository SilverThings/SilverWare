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
package io.silverware.microservices.providers.cluster.internal.exception;

import java.util.UUID;

/**
 * This runtime exception should cover all errors that could possibly happen in clustering of a Silverware
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class SilverWareClusteringException extends RuntimeException {
   /**
    * Errors that can possibly happen in clustering
    */
   public enum SilverWareClusteringError {
      JGROUPS_ERROR,
      UNEXPECTED_CONTENT,
      INVOCATION_EXCEPTION,
      RECIPIENT_SAME_AS_SENDER,
      MULTIPLE_IMPLEMENTATIONS_FOUND,
      INITIALIZATION_ERROR,
      PROCESSING_ERROR

   }

   private final SilverWareClusteringError reason;
   private final UUID id = UUID.randomUUID();

   public SilverWareClusteringException(SilverWareClusteringError reason) {
      this.reason = reason;
   }

   public SilverWareClusteringException(SilverWareClusteringError reason, String message) {
      super(message);
      this.reason = reason;
   }

   public SilverWareClusteringException(SilverWareClusteringError reason, String message, Throwable cause) {
      super(message, cause);
      this.reason = reason;
   }

   public SilverWareClusteringException(SilverWareClusteringError reason, Throwable cause) {
      super(cause);
      this.reason = reason;
   }

   public SilverWareClusteringError getReason() {
      return reason;
   }

   public UUID getId() {
      return id;
   }

   @Override
   public String toString() {
      return "SilverWareClusteringException{" +
            "reason=" + reason +
            ", id=" + id +
            ", super=" + super.toString() +
            '}';
   }
}
