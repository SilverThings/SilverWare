package org.silverware.microservices.util;

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

   private ClassLoaderUtil() {
   }

   /**
    * Get URLs to basicClassLoaders and also to jars from MANIFEST Class-Path directive.
    * @param basicClassLoaders classloaders that we want to examine
    * @return set of URL (not null)
    */
   public static Set<URL> getAlsoNestedClasspathUrls(final List<ClassLoader> basicClassLoaders) throws IOException {
      if (basicClassLoaders == null || basicClassLoaders.isEmpty()) {
         return Collections.emptySet();
      }

      final Set<ClassLoader> classLoaders = getAlsoParentsClassLoaders(basicClassLoaders);
      final Set<URL> result = new LinkedHashSet<>();
      for (final ClassLoader cl : classLoaders) {
         result.addAll(getAlsoNestedClasspathUrls(cl));
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
            result.add(((JarURLConnection) connection).getJarFileURL());
         }
      }

      return result;
   }
}
