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
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.silverware.microservices.providers.MicroserviceProvider;
import org.silverware.microservices.silver.ProvidingSilverService;
import org.silverware.microservices.util.DeployStats;
import org.silverware.microservices.util.DeploymentScanner;
import org.silverware.microservices.util.Utils;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The main Microservice provider that starts all other providers.
 *
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class Executor implements MicroserviceProvider, ProvidingSilverService {

   /**
    * Logger.
    */
   private static final Logger log = LogManager.getLogger(Executor.class);

   /**
    * Instances of all discovered Microservice providers.
    */
   private final List<MicroserviceProvider> instances = new ArrayList<>();

   /**
    * Pool of threads - each of the providers runs the main code in its own thread.
    */
   private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool(new DaemonThreadFactory());

   /**
    * Statistics about deployed providers.
    */
   private final DeployStats stats = new DeployStats();

   /**
    * Context for this instance.
    */
   private Context context = null;

   /**
    * A {@link ThreadFactory} to create daemon threads with a nice name and slightly higher priority.
    */
   static class DaemonThreadFactory implements ThreadFactory {

      /**
       * Static counter of thread pools.
       */
      private static final AtomicInteger poolNumber = new AtomicInteger(1);

      /**
       * Thread group of this factory.
       */
      private final ThreadGroup group;

      /**
       * Counter of the threads in this pool.
       */
      private final AtomicInteger threadNumber = new AtomicInteger(1);

      /**
       * Name prefix for the threads in this pool.
       */
      private final String namePrefix;

      /**
       * Creates a new factory with a nice name perfix and group name.
       */
      DaemonThreadFactory() {
         SecurityManager s = System.getSecurityManager();
         group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
         namePrefix = "SilverWare-" + poolNumber.getAndIncrement() + "-microservice-provider-";
      }

      /**
       * Creates new daemon thread with higher priority.
       *
       * @param r Runnable for which the thread should be created-
       * @return The new thread.
       */
      public Thread newThread(final Runnable r) {
         Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
         t.setDaemon(true);
         t.setPriority(8);
         return t;
      }
   }

   /**
    * The main entry point to the platform.
    * This creates a default empty context.
    *
    * @throws InterruptedException If the main thread fails for any reason.
    */
   public static void bootHook() throws InterruptedException {
      final Context context = new Context();
      bootHook(context);
   }

   /**
    * The main entry point to the platform.
    * Uses an already created context.
    *
    * @param initialContext The context associated with this platform instance.
    * @throws InterruptedException If the main thread fails for any reason.
    */
   public static void bootHook(final Context initialContext) throws InterruptedException {
      final Executor executor = new Executor();
      executor.initialize(initialContext);

      final Thread bootThread = new Thread(executor);
      bootThread.setName("SilverWare-boot");
      bootThread.start();
      bootThread.join();
   }

   /**
    * Creates instances of the Microservice provider classes using reflection.
    * Also counts statistics of the created instances.
    *
    * @param microserviceProviders A set of Microservice provider classes to create their instances.
    */
   private void createInstances(final Set<Class<? extends MicroserviceProvider>> microserviceProviders) {
      log.info(String.format("Found %d microservice providers. Starting...", microserviceProviders.size()));
      stats.setFound(microserviceProviders.size());

      for (Class<? extends MicroserviceProvider> clazz : microserviceProviders) {
         if (log.isDebugEnabled()) {
            log.debug("Creating microservice provider: " + clazz.getName());
         }

         if (clazz.getName().equals(this.getClass().getName())) {
            if (log.isDebugEnabled()) {
               log.debug("Skipping myself (Executor microservice provider) as I am already running.");
            }
            stats.incSkipped();
         } else {
            try {
               final Constructor c = clazz.getConstructor();
               final MicroserviceProvider m = (MicroserviceProvider) c.newInstance();
               m.initialize(context);
               instances.add(m);
               stats.incDeployed();
               context.getProvidersRegistry().put(m.getClass().getName(), m);
            } catch (Error | Exception e) {
               log.warn(String.format("Unable to start microservice provider: %s", clazz.getName()), e);
            }
         }
      }
   }

   /**
    * Starts the instances of prepared Microservice providers.
    */
   private void startInstances() {
      log.info("Running microservice providers...");

      for (MicroserviceProvider m : instances) {
         if (log.isDebugEnabled()) {
            log.debug("Running microservice provider: " + m.getClass().getName());
         }
         executor.submit(m);
      }

      log.info("Total microservice providers " + stats.toString() + ".");

      executor.shutdown();
   }

   @Override
   public void initialize(final Context context) {
      this.context = context;

      context.getProvidersRegistry().put(this.getClass().getName(), this);

      Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(executor)));
   }

   /**
    * Searches for Microservice providers, creates their instances and starts them.
    * Continues execution as long as there are any active providers.
    */
   @Override
   public void run() {
      log.info("Looking up microservice providers...");

      createInstances(DeploymentScanner.getContextInstance(context).lookupMicroserviceProviders());
      startInstances();

      try {
         int active;
         do {
            active = executor.getActiveCount();
            log.info("Still here ;-) Microservice providers alive: " + active);

            if (active > 0) {
               executor.awaitTermination(1, TimeUnit.MINUTES);
            }
         } while (active > 0);

         log.info("All work is done. Graceful termination.");
      } catch (InterruptedException ie) {
         Utils.shutdownLog(log, ie);
      }
   }

   @Override
   @SuppressWarnings("unchecked")
   public Set<Object> lookupMicroservice(final MicroserviceMetaData metaData) {
      Set<Object> providers = new HashSet<>();
      context.getAllProviders(metaData.getType()).forEach(providers::add);
      return providers;
   }

   @Override
   public Context getContext() {
      return context;
   }

   public static class ShutdownHook implements Runnable {

      /**
       * Logger.
       */
      private static final Logger log = LogManager.getLogger(ShutdownHook.class);


      private final ThreadPoolExecutor executor;

      public ShutdownHook(final ThreadPoolExecutor executor) {
         this.executor = executor;
      }

      @Override
      public void run() {
         log.info("Terminating SilverWare...");

         executor.shutdownNow();

         int tries = 0;
         try {
            while (!executor.awaitTermination(500, TimeUnit.MILLISECONDS) && tries < 20) {
               tries++;
            }
         } catch (InterruptedException e) {
            log.error("Could not terminate all providers smoothly: ", e);
         } finally {
            int active = executor.getActiveCount();
            if (active > 0) {
               log.error("Could not terminate all providers smoothly. There are still {} providers active.", active);
            }
         }

         log.info("Good Bye!");
         Configurator.shutdown((LoggerContext) LogManager.getContext());
      }
   }
}
