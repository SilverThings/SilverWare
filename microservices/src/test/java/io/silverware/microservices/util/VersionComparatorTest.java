package io.silverware.microservices.util;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * This test shall demonstrate functionality of version specifying
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public class VersionComparatorTest {
   private static final String VERSION = "1.2.3";

   private static final String MINOR_VERSION_SNAPSHOT = "1.2";
   private static final String MAJOR_VERSION_SNAPSHOT = "1";

   public static final String DESCRIPTION = "Version %s should%sbe satisfied by a condition %s.";
   public static final String SUCCESS_MAJOR_SNAPSHOT = "successMajorSnapshot";
   public static final String SUCCESS_LESS_DIGITS = "successLessDigits";
   public static final String FAIL = "fail";
   public static final String SUCCESS = "success";

   @DataProvider(name = SUCCESS)
   public static Object[][] successVersions() {
      return new Object[][] { { VERSION }, { "1.*" }, { "1.2.x" }, { "1.X" }, { "~1.2" }, { VERSION + "-" + VERSION }, { "^1" }, { "^" + VERSION } };
   }

   @Test(dataProvider = SUCCESS)
   public void testSatisfiesConditionSuccess(String condition) throws Exception {
      assertThat(VersionComparator.forVersion(VERSION).satisfies(condition)).as(DESCRIPTION, VERSION, " ", condition).isTrue();
   }

   @DataProvider(name = FAIL)
   public static Object[][] failVersions() {
      return new Object[][] { { "1.2.5" }, { "1.0.*" }, { "1.1.x" }, { "1.3.X" }, { "~1.3" }, { "1.2.4-1.4.0" }, { "^2" }, { "^1.2.4" } };
   }

   @Test(dataProvider = FAIL)
   public void testSatisfiesConditionFails(String condition) throws Exception {
      assertThat(VersionComparator.forVersion(VERSION).satisfies(condition)).as(DESCRIPTION, VERSION, " not ", condition).isFalse();
   }

   @DataProvider(name = SUCCESS_LESS_DIGITS)
   public static Object[][] successVersionsLessDigits() {
      return new Object[][] { { MINOR_VERSION_SNAPSHOT }, { "~1.2" }, { "1.2.x" }, { "" } };
   }

   @Test(dataProvider = SUCCESS_LESS_DIGITS)
   public void testSatisfiesConditionWithLessDigitsSuccess(String condition) throws Exception {
      assertThat(VersionComparator.forVersion(MINOR_VERSION_SNAPSHOT).satisfies(condition)).as(DESCRIPTION, MINOR_VERSION_SNAPSHOT, " ", condition).isTrue();
   }

   @DataProvider(name = SUCCESS_MAJOR_SNAPSHOT)
   public static Object[][] successVersionsMajorSnapshot() {
      return new Object[][] { { MAJOR_VERSION_SNAPSHOT }, { "~1" }, { "1.x" }, { "" }, { "^1" }, { "1" } };
   }

   @Test(dataProvider = SUCCESS_MAJOR_SNAPSHOT)
   public void testSatisfiesConditionMajorSnapshotSuccess(String condition) throws Exception {
      assertThat(VersionComparator.forVersion(MAJOR_VERSION_SNAPSHOT).satisfies(condition)).as(DESCRIPTION, MAJOR_VERSION_SNAPSHOT, " ", condition).isTrue();
   }

}