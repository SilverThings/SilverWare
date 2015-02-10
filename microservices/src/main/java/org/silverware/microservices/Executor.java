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
package org.silverware.microservices;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.silverware.microservices.util.DeploymentScanner;
import org.silverware.microservices.util.Utils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class Executor implements Runnable {

   private static final Logger log = LogManager.getLogger(Executor.class);

   private final List<Microservice> instances = new ArrayList<>();
   private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(new DaemonThreadFactory());

   static class DaemonThreadFactory implements ThreadFactory {
      private static final AtomicInteger poolNumber = new AtomicInteger(1);
      private final ThreadGroup group;
      private final AtomicInteger threadNumber = new AtomicInteger(1);
      private final String namePrefix;

      DaemonThreadFactory() {
         SecurityManager s = System.getSecurityManager();
         group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
         namePrefix = "SilverWare-" + poolNumber.getAndIncrement() + "-microservice-";
      }

      public Thread newThread(final Runnable r) {
         Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
         t.setDaemon(true);
         t.setPriority(8);
         return t;
      }
   }

   private void createInstances(final Set<Class<? extends Microservice>> microservices) {
      log.info(String.format("Found %d microservices. Starting...", microservices.size()));

      for (Class<? extends Microservice> clazz : microservices) {
         if (log.isDebugEnabled()) {
            log.debug("Creating microservice: " + clazz.getName());
         }

         try {
            final Constructor c = clazz.getConstructor();
            final Microservice m = (Microservice) c.newInstance();
            m.initialize(System.getProperties());
            instances.add(m);
         } catch (Exception e) {
            log.warn(String.format("Unable to start service: %s", clazz.getName()), e);
         }
      }
   }

   private void startInstances() {
      log.info("Running microservices...");

      for (Microservice m : instances) {
         if (log.isDebugEnabled()) {
            log.debug("Running microservice: " + m.getClass().getName());
         }
         executor.submit(m);
      }

      executor.shutdown();
   }

   public void run() {
      log.info("Looking up microservices...");

      createInstances(DeploymentScanner.lookupMicroservices());
      startInstances();

      try {
         int active;
         do {
            active = executor.getActiveCount();
            log.info("Still here ;-) Microservices alive: " + active);

            if (active > 0) {
               executor.awaitTermination(1, TimeUnit.MINUTES);
            }
         } while (active > 0);

         log.info("All work is done. Graceful termination.");
      } catch (InterruptedException ie) {
         Utils.shutdownLog(log, ie);
      }
   }
}
