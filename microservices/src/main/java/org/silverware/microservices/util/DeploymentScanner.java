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
package org.silverware.microservices.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.vfs.SystemDir;
import org.reflections.vfs.Vfs;
import org.reflections.vfs.ZipDir;
import org.silverware.microservices.Context;
import org.silverware.microservices.providers.MicroserviceProvider;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class DeploymentScanner {

   private static final Logger log = LogManager.getLogger(DeploymentScanner.class);

   private static DeploymentScanner defaultScanner = null;

   private final Reflections reflections;

   static {
      Vfs.addDefaultURLTypes(new WarUrlType());
   }

   private DeploymentScanner() {
      reflections = new Reflections("");
   }

   private DeploymentScanner(final String... packages) {
      reflections = new Reflections((Object[]) packages);
   }

   public static synchronized DeploymentScanner getDefaultInstance() {
      if (defaultScanner == null) {
         defaultScanner = new DeploymentScanner();
      }

      return defaultScanner;
   }

   public static DeploymentScanner getInstance(final String... packages) {
      return new DeploymentScanner(packages);
   }

   public static DeploymentScanner getContextInstance(final Context context) {
      final String packages = (String) context.getProperties().get(Context.DEPLOYMENT_PACKAGES);
      if (packages != null) {
         if (log.isDebugEnabled()) {
            log.debug("Limited deployment packages: " + packages);
         }
         return getInstance(packages.split("[ ]*,[ ]*"));
      } else {
         return DeploymentScanner.getDefaultInstance();
      }
   }

   public Set<Class<? extends MicroserviceProvider>> lookupMicroserviceProviders() {
      return reflections.getSubTypesOf(MicroserviceProvider.class);
   }

   @SuppressWarnings("unchecked")
   public Set lookupSubtypes(final Class clazz) {
      return reflections.getSubTypesOf(clazz);
   }

   public static <T> List<T> instantiate(final Set<Class<T>> classes) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
      final List<T> instances = new ArrayList<>();

      for (Class<T> clazz : classes) {
         if (log.isDebugEnabled()) {
            log.debug("Creating instance of " + clazz.getName());
         }

         final Constructor c = clazz.getConstructor();

         @SuppressWarnings("unchecked")
         final T t = (T) c.newInstance();

         instances.add(t);
      }

      return instances;
   }

   public static class WarUrlType implements Vfs.UrlType {

      @Override
      public boolean matches(final URL url) {
         return url.getProtocol().equals("file") && url.toExternalForm().endsWith(".war");
      }

      @Override
      public Vfs.Dir createDir(final URL url) throws IOException, URISyntaxException {
         final File file = new File(url.toURI());
         if (file.isDirectory()) {
            return new SystemDir(file);
         } else {
            return new ZipDir(new JarFile(file));
         }
      }
   }
}