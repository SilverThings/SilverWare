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
package io.silverware.microservices.util;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Deployment statistics. Counts the number of discovered, skipped and deployed instances.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class DeployStats implements Serializable {

   private static final long serialVersionUID = 5176533100828970885L;

   /**
    * Discovered instances.
    */
   private final AtomicLong found = new AtomicLong(0);

   /**
    * Skipped instances.
    */
   private final AtomicLong skipped = new AtomicLong(0);

   /**
    * Successfully deployed instances.
    */
   private final AtomicLong deployed = new AtomicLong(0);

   /**
    * Sets the number of discovered instances.
    *
    * @param found The number of discovered instances.
    */
   public void setFound(long found) {
      this.found.set(found);
   }

   /**
    * Increases the number of skipped instances.
    */
   public void incSkipped() {
      skipped.incrementAndGet();
   }

   /**
    * Increases the number of deployed instances.
    */
   public void incDeployed() {
      deployed.incrementAndGet();
   }

   /**
    * Gets the number of discovered instances.
    *
    * @return The number of discovered instances.
    */
   public long getFound() {
      return found.get();
   }

   /**
    * Gets the number of skipped instances.
    *
    * @return The number of skipped instances.
    */
   public long getSkipped() {
      return skipped.get();
   }

   /**
    * Gets the number of deployed instances.
    *
    * @return The number of deployed instances.
    */
   public long getDeployed() {
      return deployed.get();
   }

   /**
    * Gets the string representation of the statistics in a user friendly format.
    *
    * @return Tthe string representation of the statistics in a user friendly format.
    */
   @Override
   public String toString() {
      return String.format("found %d, deployed %d, skipped deployment %d", getFound(), getDeployed(), getSkipped());
   }
}
