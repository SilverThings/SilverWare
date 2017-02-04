/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2015 - 2017 the original author or authors.
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

import static com.google.common.base.Strings.isNullOrEmpty;

import io.silverware.microservices.MicroserviceMetaData;

import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.SemverException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Adapter for a Java Sem-Ver
 * Created because the project is in beta and the api can change.
 * This implementation is using <a href="https://github.com/npm/node-semver">NPM versioning rules.</a>
 * implemented in library <a href="https://github.com/vdurmont/semver4j">Semantic Versioning home page.</a>
 *
 * Be aware:
 * If a version has a prerelease tag (for example, 1.2.3-alpha.3) then it will only be allowed
 * to satisfy comparator sets if at least one comparator with the same [major, minor, patch] tuple also has a prerelease tag.
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class VersionComparator {

   private static final Logger log = LogManager.getLogger(VersionComparator.class);

   private final Semver semVersion;

   private VersionComparator(String version) {
      if (isNullOrEmpty(version)) {
         this.semVersion = null;
      } else {
            /*
              build version must be appended to x.y.z format number otherwise the comparision wont work.
              here we are creating correct string for comparision.
             */
         Semver builtVersion = new Semver(version, Semver.SemverType.NPM);
         if (builtVersion.getMinor() == null && builtVersion.getPatch() == null) {
            int indexOfMinorVersion = ("" + builtVersion.getMajor()).length();
            version = buildVersionWithPatch(version, indexOfMinorVersion, ".0.0");
         } else if (builtVersion.getPatch() == null) {
            int indexOfPatchVersion = ("." + builtVersion.getMinor() + builtVersion.getMajor()).length();
            version = buildVersionWithPatch(version, indexOfPatchVersion, ".0");
         }
         this.semVersion = new Semver(version, Semver.SemverType.NPM);
      }
   }

   /**
    * Factory method which creates an instance of this class.
    *
    * @param version
    *       semVersion for which object should be created when or empty provided then it won't satisfy any expression
    * @param metaData
    *       metadata which are compared - used just for logging
    * @return created object
    * @throws IllegalArgumentException
    *       when the format of the semVersion is wrong
    */
   public static VersionComparator forVersion(MicroserviceMetaData metaData, String version) {
      try {
         return new VersionComparator(version);
      } catch (SemverException e) {
         //         log.error("Wrong version in metadata: " + metaData, e);
         return new VersionComparator(null);
      }
   }

   /**
    * Compare semVersion of a object and the expression.
    *
    * @param expression
    *       the expression specifying supported versions
    * @return true when expression is null, false when semVersion in object is null and result of a @{@link Semver}.satisfies() otherwise.
    * @throws IllegalArgumentException
    *       when the format of a expression is wrong
    */
   public boolean satisfies(String expression) {
      if (isNullOrEmpty(expression) || semVersion == null) {
         return true;
      }
      try {
         return semVersion.satisfies(expression);
      } catch (SemverException e) {
         throw new IllegalArgumentException(e);
      }
   }

   private static String buildVersionWithPatch(String originalValue, int index, String missingVersions) {
      return originalValue.substring(0, index) + missingVersions + originalValue.substring(index, originalValue.length());
   }

}
