/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2015 - 2016 the original author or authors.
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
package io.silverware.microservices.providers.cdi.util;

import static java.util.stream.Collectors.toList;

import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.annotations.MicroserviceVersion;
import io.silverware.microservices.util.Utils;

import com.google.common.base.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.netty.util.internal.StringUtil;

/**
 * This class is used for resolving of api and implementation versions.
 * The resolution process takes the first version which is provided for the microservice from following list
 * <ol>
 * <li>MicroserviceVersion annotation on bean</li>
 * <li>MicroserviceVersion annotation on class</li>
 * <li>MicroserviceVersion annotation on interfaces</li>
 * <li>MicroserviceVersion annotation on parent classes</li>
 * <li>Manifest version</li>
 * </ol>
 * If no version was obtained using this process null will be returned.
 * See @{@link MicroserviceVersion}
 * See @{@link io.silverware.microservices.util.VersionComparator}
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public final class VersionResolver {
   public static final java.util.function.Predicate<Annotation> IS_ANNOTATION_MICROSERVICE_VERSION = annotation -> MicroserviceVersion.class.isAssignableFrom(annotation.getClass());

   /**
    * Logger.
    */
   private static final Logger log = LogManager.getLogger(VersionResolver.class);

   public static final String SPECIFICATION_VERSION = "Specification-Version";
   public static final String IMPLEMENTATION_VERSION = "Implementation-Version";
   private static final VersionResolver versionResolver = new VersionResolver();
   public static final String MORE_MICROSERVICE_VERSIONS_FOUND = "Microservice version annotation present at more interfaces for class %s.";

   private VersionResolver() {
   }

   public static VersionResolver getInstance() {
      return versionResolver;
   }

   /**
    * Create a instance of a Microservice meta data which represents discovered Microservice.
    *
    * @param name
    *       The name of the discovered Microservice.
    * @param type
    *       The type of the discovered Microservice.
    * @param qualifiers
    *       The qualifiers of the discovered Microservice.
    * @param annotations
    *       The annotations of the discovered Microservice.
    */
   public MicroserviceMetaData createMicroserviceMetadataForBeans(final String name, final Class type, final Set<Annotation> qualifiers, final Annotation[] annotations) {
      Set<Annotation> annotationSet = Arrays.stream(annotations).collect(Collectors.toSet());
      String apiVersion = resolveApiVersion(type, annotationSet);
      String implVersion = resolveImplementationVersion(type, annotationSet);
      return new MicroserviceMetaData(name, type, qualifiers, annotationSet, apiVersion, implVersion);

   }

   /**
    * Create a instance of a Microservice meta data which represents discovered Microservice.
    *
    * @param name
    *       The name of the discovered Microservice.
    * @param type
    *       The type of the discovered Microservice.
    * @param qualifiers
    *       The qualifiers of the discovered Microservice.
    * @param annotations
    *       The annotations of the discovered Microservice.
    */
   public MicroserviceMetaData createMicroserviceMetadataForInjectionPoint(final String name, final Class type, final Set<Annotation> qualifiers, final Set<Annotation> annotations) {
      String apiVersion = resolveVersionFromAnnotations(annotations.stream(), MicroserviceVersion::api);
      String implVersion = resolveVersionFromAnnotations(annotations.stream(), MicroserviceVersion::implementation);
      return new MicroserviceMetaData(name, type, qualifiers, annotations, apiVersion, implVersion);

   }

   /**
    * Use the process mentioned in @{@link VersionResolver} to obtain api version.
    *
    * @param clazz
    *       The class I want to obtain version of.
    * @param annotations
    *       list of a annotations from service
    * @return The class specification version from manifest, null if there is no version information present or the manifest file does not exists.
    */
   public String resolveApiVersion(final Class clazz, final Set<Annotation> annotations) {
      return resolveVersion(clazz, annotations, MicroserviceVersion::api, SPECIFICATION_VERSION);
   }

   /**
    * Use the process mentioned in @{@link VersionResolver} to obtain implementation version.
    *
    * @param clazz
    *       The class I want to obtain version of.
    * @param annotations
    *       list of a annotations from service
    * @return The class specification version from manifest, null if there is no version information present or the manifest file does not exists.
    */
   public String resolveImplementationVersion(final Class clazz, final Set<Annotation> annotations) {
      return resolveVersion(clazz, annotations, MicroserviceVersion::implementation, IMPLEMENTATION_VERSION);
   }

   String resolveVersion(final Class clazz, final Set<Annotation> annotations, Function<MicroserviceVersion, String> lambda, final String versionType) {
      String version = null;
      if (annotations != null) {
         version = resolveVersionFromAnnotations(annotations.stream(), lambda);
      }
      if (version == null && clazz.getAnnotations() != null) {
         version = resolveVersionFromAnnotations(Arrays.stream(clazz.getAnnotations()), lambda);
      }
      if (version == null) {
         version = resolveVersionFromInterfacesClasses(clazz, lambda);
      }
      if (version == null) {
         version = resolveVersionFromSuperClasses(clazz, lambda);
      }
      if (version == null) {
         version = getClassVersionFromManifest(clazz, versionType);
      }
      return version;
   }

   String resolveVersionFromSuperClasses(final Class clazz, final Function<MicroserviceVersion, String> lambda) {
      Class classToProcess = clazz.getSuperclass();
      String version = null;
      while (classToProcess != null && !classToProcess.equals(Object.class) && version == null) {
         version = resolveVersionFromAnnotations(Arrays.stream(classToProcess.getAnnotations()), lambda);
         classToProcess = classToProcess.getSuperclass();
      }
      return version;
   }

   String resolveVersionFromInterfacesClasses(final Class clazz, Function<MicroserviceVersion, String> lambda) {
      Class[] interfaces = clazz.getInterfaces();
      List<String> versions = Arrays.stream(interfaces)
                                    .map(i -> resolveVersionFromAnnotations(Arrays.stream(i.getAnnotations()), lambda))
                                    .filter(v -> !StringUtil.isNullOrEmpty(v))
                                    .collect(toList());
      if (versions.size() > 1) {
         throw new IllegalArgumentException(String.format(MORE_MICROSERVICE_VERSIONS_FOUND, clazz.getName()));
      }
      return versions.isEmpty() ? null : versions.get(0);

   }

   String resolveVersionFromAnnotations(Stream<Annotation> annotations, Function<MicroserviceVersion, String> lambda) {
      if (annotations != null) {
         Optional<Annotation> annotation = annotations.filter(IS_ANNOTATION_MICROSERVICE_VERSION).findFirst();
         if (annotation.isPresent()) {
            String version = lambda.apply((MicroserviceVersion) annotation.get());
            return Strings.isNullOrEmpty(version) ? null : version;

         }
      }
      return null;
   }

   /**
    * Gets the class implementation version from manifest.
    *
    * @param clazz
    *       The class I want to obtain version of.
    * @return The class specification version from manifest, null if there is no version information present or the manifest file does not exists.
    */
   String getClassVersionFromManifest(final Class clazz, final String versionType) {
      try {
         return Utils.getManifestEntry(clazz, versionType);
      } catch (IOException ioe) {
         if (log.isDebugEnabled()) {
            log.debug("Cannot obtain version for class {}.", clazz.getName());
         }
      }

      return null;
   }

}
