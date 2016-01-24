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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.vfs.SystemDir;
import org.reflections.vfs.Vfs;
import org.reflections.vfs.ZipDir;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import io.silverware.microservices.Context;
import io.silverware.microservices.providers.MicroserviceProvider;

/**
 * Scanner of classpath to search for given classes, interface implementations and others.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class DeploymentScanner {

   /**
    * Logger.
    */
   private static final Logger log = LogManager.getLogger(DeploymentScanner.class);

   /**
    * Default singleton instance.
    */
   private static DeploymentScanner defaultScanner = null;

   /**
    * Main scanning utility.
    */
   private final Reflections reflections;

   /**
    * Make it possible to search in WAR files when the platform is deployed in this packaging.
    */
   static {
      Vfs.addDefaultURLTypes(new WarUrlType());
   }

   /**
    * Creates a default instance of the scanner that scans through the whole classpath.
    */
   private DeploymentScanner() {
      final ConfigurationBuilder builder = ConfigurationBuilder.build("");
      addNestedClasspathUrls(builder);
      removeSysLibUrls(builder);
      builder.addScanners(new ResourcesScanner());

      reflections = new Reflections(builder);
   }

   /**
    * Creates an instance of the scanner that scans only in the given packages.
    *
    * @param packages
    *       Packages to limit scanning to.
    */
   private DeploymentScanner(final String... packages) {
      final ConfigurationBuilder builder = ConfigurationBuilder.build((Object[]) packages);
      removeSysLibUrls(builder);
      builder.addScanners(new ResourcesScanner());

      reflections = new Reflections(builder);
   }

   /**
    * Gets the static default instance of the scanner.
    *
    * @return The static default instance of the scanner.
    */
   public static synchronized DeploymentScanner getDefaultInstance() {
      if (defaultScanner == null) {
         defaultScanner = new DeploymentScanner();
      }

      return defaultScanner;
   }

   /**
    * Gets an instance of the scanner that is limited to the given packages.
    *
    * @param packages
    *       Packages to limit scanning to.
    * @return An instance of the scanner that is limited to the given packages.
    */
   public static DeploymentScanner getInstance(final String... packages) {
      return new DeploymentScanner(packages);
   }

   /**
    * Gets an instance of the scanner based on the information already stored in the provided
    * {@link Context}.
    *
    * @param context
    *       A {@link Context} carrying the information needed to create the scanner.
    * @return An instance of the scanner based on the information already stored in the provided
    */
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

   /**
    * Searches for all available Microservice providers.
    *
    * @return All available Microservice provider classes.
    */
   public Set<Class<? extends MicroserviceProvider>> lookupMicroserviceProviders() {
      return reflections.getSubTypesOf(MicroserviceProvider.class);
   }

   /**
    * Searches for all subtypes of the given class.
    *
    * @param clazz
    *       A class to search subtypes of.
    * @return All available classes of the given subtype.
    */
   @SuppressWarnings("unchecked")
   public Set lookupSubtypes(final Class clazz) {
      return reflections.getSubTypesOf(clazz);
   }

   /**
    * Searches for all resources matching the given pattern.
    *
    * @param pattern
    *       The pattern to match.
    * @return All available resources matching the given pattern.
    */
   @SuppressWarnings("unchecked")
   public Set<String> lookupResources(final String pattern) {
      return reflections.getResources(Pattern.compile(pattern));
   }

   /**
    * Creates instances of the given classes using default constructor.
    *
    * @param classes
    *       Classes to create instances of.
    * @param <T>
    *       Common type of the classes.
    * @return Instances of the given classes.
    * @throws NoSuchMethodException
    *       When there was no default constructor.
    * @throws IllegalAccessException
    *       When the default constructor is not visible.
    * @throws InvocationTargetException
    *       When it was not possible to invoke the constructor.
    * @throws InstantiationException
    *       When it was not possible to create an instance.
    */
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

   /**
    * Add ClasspathUrls from MANIFEST Class-Path directive into builder
    * @param builder Reflection ConfigurationBuilder
    */
   private static void addNestedClasspathUrls(final ConfigurationBuilder builder) {
      final ClassLoader[] cls = ClasspathHelper.classLoaders(builder.getClassLoaders());
      builder.addUrls(ClassLoaderUtil.getAlsoNestedClasspathUrls(Arrays.asList(cls)));
   }

   /**
    * Remove ClasspathUrls like *.so or *.dll
    * @param builder Reflection ConfigurationBuilder
    */
   private static void removeSysLibUrls(final ConfigurationBuilder builder) {
      final Pattern sysLibPattern = Pattern.compile(".*[.](so|dll)", Pattern.CASE_INSENSITIVE);

      final Set<URL> urls = builder.getUrls().stream().filter(
            url -> !sysLibPattern.matcher(url.getFile()).matches()
      ).collect(Collectors.toCollection(LinkedHashSet::new));
      builder.setUrls(urls);
   }

   /**
    * {@link org.reflections.vfs.Vfs.UrlType} for WAR files.
    */
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