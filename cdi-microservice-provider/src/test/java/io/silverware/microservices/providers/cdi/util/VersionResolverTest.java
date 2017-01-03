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
package io.silverware.microservices.providers.cdi.util;

import static io.silverware.microservices.providers.cdi.util.VersionResolver.IMPLEMENTATION_VERSION;
import static io.silverware.microservices.providers.cdi.util.VersionResolver.MORE_MICROSERVICE_VERSIONS_FOUND;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.annotations.MicroserviceVersion;

import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import mockit.Expectations;

/**
 * This test check if version resolution works as is described in javadoc.
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class VersionResolverTest {

   public static final String API = "API";
   public static final String IMPL = "IMPL";

   private VersionResolver vr;

   @BeforeMethod
   public void init() {
      vr = VersionResolver.getInstance();
   }

   @Test
   public void testCreateMicroserviceMetadata() throws Exception {
      new Expectations(VersionResolver.class) {{
         vr.resolveApiVersion((Class) any, (Set<Annotation>) any);
         result = API;
         vr.resolveImplementationVersion((Class) any, (Set<Annotation>) any);
         result = IMPL;
      }};

      String name = UUID.randomUUID().toString();

      MicroserviceMetaData microserviceMetadata = VersionResolver.getInstance().createMicroserviceMetadataForBeans(name, VersionResolverTest.class, Collections.emptySet(), new Annotation[0]);

      SoftAssertions softly = new SoftAssertions();

      softly.assertThat(microserviceMetadata.getName()).isEqualTo(name);
      softly.assertThat(microserviceMetadata.getApiVersion()).isEqualTo(API);
      softly.assertThat(microserviceMetadata.getImplVersion()).isEqualTo(IMPL);
      softly.assertThat(microserviceMetadata.getType()).isEqualTo(VersionResolverTest.class);
      softly.assertThat(microserviceMetadata.getAnnotations()).isEmpty();
      softly.assertThat(microserviceMetadata.getQualifiers()).isEmpty();

      softly.assertAll();

   }

   @Test
   public void testIsEverythingTriggered() throws Exception {
      new Expectations(VersionResolver.class) {{
         vr.resolveVersionFromAnnotations((Stream<Annotation>) any, (Function<MicroserviceVersion, String>) any);
         times = 2;
         result = null;
         vr.resolveVersionFromInterfacesClasses((Class) any, (Function<MicroserviceVersion, String>) any);
         times = 1;
         result = null;
         vr.resolveVersionFromSuperClasses((Class) any, (Function<MicroserviceVersion, String>) any);
         times = 1;
         result = null;
         vr.getClassVersionFromManifest((Class) any, (String) any);
         times = 1;
         result = null;

      }};
      Class clazz = VersionsHierarchy.WithBothVersionsOnClass.class;
      String result = vr.resolveVersion(clazz, arrayAsSet(clazz.getAnnotations()), MicroserviceVersion::implementation, IMPLEMENTATION_VERSION);
      assertThat(result).isNull();

   }

   @Test
   public void testIsManifestTriggered() throws Exception {
      new Expectations(VersionResolver.class) {{
         vr.getClassVersionFromManifest((Class) any, (String) any);
         times = 1;
         result = API;

      }};
      Class clazz = VersionsHierarchy.WithoutVersions.class;
      String result = vr.resolveApiVersion(clazz, arrayAsSet(clazz.getAnnotations()));
      assertThat(result).isEqualTo(API);

   }

   @Test
   public void testIsManifestTriggeredForInterface() throws Exception {
      new Expectations(VersionResolver.class) {{
         vr.getClassVersionFromManifest((Class) any, (String) any);
         times = 1;
         result = IMPL;

      }};
      Class clazz = VersionsHierarchy.WithoutVersion1.class;
      String result = vr.resolveImplementationVersion(clazz, arrayAsSet(clazz.getAnnotations()));
      assertThat(result).isEqualTo(IMPL);

   }

   @Test
   public void testResolveVersionBasic() throws Exception {
      final Class clazz = VersionsHierarchy.WithBothVersionsOnClass.class;

      final String resultApi = vr.resolveApiVersion(clazz, arrayAsSet(clazz.getAnnotations()));
      assertThat(resultApi).isEqualTo(VersionsHierarchy.WithBothVersionsOnClassApi);

      final String resultImpl = vr.resolveImplementationVersion(clazz, arrayAsSet(clazz.getAnnotations()));
      assertThat(resultImpl).isEqualTo(VersionsHierarchy.WithBothVersionsOnClassImpl);

   }

   @Test
   public void testResolveVersionManually() throws Exception {
      final Class clazz = VersionsHierarchy.WithBothVersionsOnClass.class;

      final String resultApi = vr.resolveApiVersion(clazz, Collections.emptySet());
      assertThat(resultApi).isEqualTo(VersionsHierarchy.WithBothVersionsOnClassApi);

      final String resultImpl = vr.resolveImplementationVersion(clazz, Collections.emptySet());
      assertThat(resultImpl).isEqualTo(VersionsHierarchy.WithBothVersionsOnClassImpl);

   }

   @Test
   public void testResolveApiVersionFromParent() throws Exception {
      final Class clazz = VersionsHierarchy.WithImplVersionOnClassAndApiVersionOnParent.class;

      final String resultApi = vr.resolveApiVersion(clazz, arrayAsSet(clazz.getAnnotations()));
      assertThat(resultApi).isEqualTo(VersionsHierarchy.WithBothVersionsOnClassApi);

      final String resultImpl = vr.resolveImplementationVersion(clazz, arrayAsSet(clazz.getAnnotations()));
      assertThat(resultImpl).isEqualTo(VersionsHierarchy.WithImplVersionOnClassAndApiVersionOnParent);

   }

   @Test
   public void testResolveApiVersionFromInterface() throws Exception {
      final Class clazz = VersionsHierarchy.WithImplVersionOnClassAndApiVersionOnInterface.class;

      final String resultApi = vr.resolveApiVersion(clazz, arrayAsSet(clazz.getAnnotations()));
      assertThat(resultApi).isEqualTo(VersionsHierarchy.WithVersionInterfaceApi);

      final String resultImpl = vr.resolveImplementationVersion(clazz, arrayAsSet(clazz.getAnnotations()));
      assertThat(resultImpl).isEqualTo(VersionsHierarchy.WithImplVersionOnClassAndApiVersionOnInterfaceImpl);
   }

   @Test
   public void testResolveApiVersionFromClassAndImplVersionFromParent() throws Exception {
      final Class clazz = VersionsHierarchy.WithApiVersionOnClassAndImplVersionOnParent.class;

      final String resultApi = vr.resolveApiVersion(clazz, arrayAsSet(clazz.getAnnotations()));
      assertThat(resultApi).isEqualTo(VersionsHierarchy.WithApiVersionOnClassAndImplVersionOnParent);

      final String resultImpl = vr.resolveImplementationVersion(clazz, arrayAsSet(clazz.getAnnotations()));
      assertThat(resultImpl).isEqualTo(VersionsHierarchy.WithBothVersionsOnClassImpl);
   }

   @Test
   public void testResolveApiVersionOnClassAndImplVersionFromInterface() throws Exception {
      final Class clazz = VersionsHierarchy.WithApiVersionOnClassAndImplVersionOnInterface.class;

      final String resultApi = vr.resolveApiVersion(clazz, arrayAsSet(clazz.getAnnotations()));
      assertThat(resultApi).isEqualTo(VersionsHierarchy.WithApiVersionOnClassAndImplVersionOnInterface);

      final String resultImpl = vr.resolveImplementationVersion(clazz, arrayAsSet(clazz.getAnnotations()));
      assertThat(resultImpl).isEqualTo(VersionsHierarchy.WithVersionInterfaceImpl);
   }

   @Test
   public void testResolveBothVersionFromInterface() throws Exception {
      final Class clazz = VersionsHierarchy.WithVersionsOnInterface.class;

      final String resultApi = vr.resolveApiVersion(clazz, arrayAsSet(clazz.getAnnotations()));
      assertThat(resultApi).isEqualTo(VersionsHierarchy.WithVersionInterfaceApi);

      final String resultImpl = vr.resolveImplementationVersion(clazz, arrayAsSet(clazz.getAnnotations()));
      assertThat(resultImpl).isEqualTo(VersionsHierarchy.WithVersionInterfaceImpl);
   }

   @Test
   public void testResolveVersionPresentOnTwoInterfaces() throws Exception {
      final Class clazz = VersionsHierarchy.WithVersionOn2Interfaces.class;

      String versionString = MORE_MICROSERVICE_VERSIONS_FOUND.replace("%s.", "");
      assertThatThrownBy(() -> vr.resolveApiVersion(clazz, arrayAsSet(clazz.getAnnotations())))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(versionString)
            .hasMessageContaining(VersionsHierarchy.WithVersionOn2Interfaces.class.getName());

      assertThatThrownBy(() -> vr.resolveImplementationVersion(clazz, arrayAsSet(clazz.getAnnotations())))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(versionString)
            .hasMessageContaining(VersionsHierarchy.WithVersionOn2Interfaces.class.getName());

   }

   private Set<Annotation> arrayAsSet(final Annotation[] annotations) {
      return new HashSet<>(asList(annotations));
   }

}