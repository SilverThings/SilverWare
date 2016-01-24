package io.silverware.microservices.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper methods for work with classloaders.
 */
public final class ClassLoaderUtil {

   /**
    * Logger.
    */
   private static final Logger log = LogManager.getLogger(ClassLoaderUtil.class);

   private ClassLoaderUtil() {
   }

   /**
    * Get URLs to basicClassLoaders and also to jars from MANIFEST Class-Path directive.
    * @param basicClassLoaders Classloaders that we want to examine.
    * @return Set of URL (not null).
    */
   public static Set<URL> getAlsoNestedClasspathUrls(final List<ClassLoader> basicClassLoaders) {
      if (basicClassLoaders == null || basicClassLoaders.isEmpty()) {
         return Collections.emptySet();
      }

      final Set<ClassLoader> classLoaders = getAlsoParentsClassLoaders(basicClassLoaders);
      final Set<URL> result = new LinkedHashSet<>();
      for (final ClassLoader cl : classLoaders) {
         try {
            result.addAll(getAlsoNestedClasspathUrls(cl));
         } catch (IOException ioe) {
            log.warn("Unable to investigate nested classpath of {}: {}", cl, ioe);
         }
      }

      return result;
   }

   private static Set<ClassLoader> getAlsoParentsClassLoaders(final List<ClassLoader> basicClassLoaders) {
      final Set<ClassLoader> result = new LinkedHashSet<>(basicClassLoaders);
      for (final ClassLoader basicClassLoader : basicClassLoaders) {
         ClassLoader parent = basicClassLoader.getParent();
         while (parent != null) {
            result.add(parent);
            parent = parent.getParent();
         }
      }
      return result;
   }

   private static Set<URL> getAlsoNestedClasspathUrls(ClassLoader cl) throws IOException {
      final Set<URL> result = new LinkedHashSet<>();

      //add standard getURLs() urls.
      if (cl instanceof URLClassLoader) {
         final URL[] urls = ((URLClassLoader) cl).getURLs();
         if (urls != null && urls.length > 0) {
            result.addAll(Arrays.asList(urls));
         }
      }

      //add all other nested urls.
      final Enumeration<URL> eResource = cl.getResources("META-INF");
      while (eResource.hasMoreElements()) {
         final URL urlResource = eResource.nextElement();
         final URLConnection connection = urlResource.openConnection();
         if (connection instanceof JarURLConnection) {
            final URL jarFileUrl = ((JarURLConnection) connection).getJarFileURL();
            if (!result.contains(jarFileUrl)) {
               log.debug("Got nested classpath url " + jarFileUrl);
               result.add(jarFileUrl);
            }
         }
      }

      return result;
   }
}
