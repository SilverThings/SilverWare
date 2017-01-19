package io.silverware.microservices.monitoring;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * @author Tomas Borcin | tborcin@redhat.com | created: 19.12.16.
 */
public class MetricsManagerTest extends MetricsParentTest {

   private MetricsManager metricsManager;

   @BeforeMethod
   public void initTest() {
      metricsManager = new MetricsManager();
   }

   @Test
   public void testEmptyMetricsManager() {
      // All values are set to zero by default
      assertEquals(metricsManager.getValues().size(), 0);
      assertEquals(metricsManager.getMinTime(), BigDecimal.ZERO);
      assertEquals(metricsManager.getMaxTime(), BigDecimal.ZERO);
      assertEquals(metricsManager.getAverageTime(), BigDecimal.ZERO);
      assertEquals(metricsManager.getCount(), 0L);
      assertEquals(metricsManager.get90Percentile(), BigDecimal.ZERO);
      assertEquals(metricsManager.get95Percentile(), BigDecimal.ZERO);
      assertEquals(metricsManager.get99Percentile(), BigDecimal.ZERO);
   }

   @Test
   public void testSingleValueExpiration() throws InterruptedException {
      // check if values are empty
      assertTrue(metricsManager.getValues().isEmpty());

      BigDecimal time = new BigDecimal(10);

      // values should contain only added time
      metricsManager.addTime(time);
      assertEquals(metricsManager.getValues().size(), 1);
      assertTrue(metricsManager.getValues().contains(time));

      TimeUnit.SECONDS.sleep(59);

      // after 59 seconds inserted value is still present
      assertEquals(metricsManager.getValues().size(), 1);
      assertTrue(metricsManager.getValues().contains(time));

      TimeUnit.SECONDS.sleep(2);

      // after another 2 seconds (61 total), value expires as it only lasts for 60 seconds
      assertTrue(metricsManager.getValues().isEmpty());
      assertTrue(!metricsManager.getValues().contains(time));
   }

   @Test
   public void testValuesExpiration() throws InterruptedException {

      int expirationTime = 60;
      for (int i = 1; i <= 100; i++) {
         metricsManager.addTime(new BigDecimal(i));
         TimeUnit.SECONDS.sleep(1);
         // after 60 seconds, values should start to expire
         if (i > expirationTime) {
            // assert that n-th value is not present after n + 60 seconds
            assertTrue(!metricsManager.getValues().contains(i - expirationTime));
            // base expirationTime value is 60 and is incremented as every value is inserted with 1 second delay
            expirationTime++;
         }
      }
   }

   // attempt to insert zero time should result in IllegalArgumentException being thrown
   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testZeroTime() {
      metricsManager.addTime(BigDecimal.ZERO);
   }

   // attempt to insert negative time should result in IllegalArgumentException being thrown
   @Test(expectedExceptions = IllegalArgumentException.class)
   public void testNegativeTime() {
      metricsManager.addTime(getRandomTime().multiply(BigDecimal.valueOf(-1)));
   }

   @Test
   public void testRandomValues() {
      for (int i = 0; i < 1000; i++) {
         metricsManager.addTime(getRandomTime());
      }
      // after inserting 1000 random values, check if values contain such amount of values
      assertTrue(metricsManager.getValues().size() == 1000);

      try {
         testNegativeTime();
         // we should not reach this code and test will fail if exception is not thrown
         fail("IllegalArgumentException for negative time not thrown");
      } catch (IllegalArgumentException e) {
         // everything went as expected
      }
      try {
         testZeroTime();
         // we should not reach this code and test will fail if exception is not thrown
         fail("IllegalArgumentException for zero time not thrown");
      } catch (IllegalArgumentException e) {
         // everything went as expected
      }
      // attempt to insert zero or negative values should not be successful and we should still have exactly 1000 values in our list
      assertTrue(metricsManager.getValues().size() == 1000);
   }

   @Test
   public void testMetrics() throws InterruptedException {
      // insert single value of 10 into values
      metricsManager.addTime(BigDecimal.TEN);
      // wait 2 seconds for percentiles to be updated
      TimeUnit.SECONDS.sleep(2);

      // we should have only one value of 10 and this metric should be min, max, avg and all percentiles
      assertEquals(metricsManager.getValues().size(), 1);
      assertEquals(metricsManager.getMinTime(), BigDecimal.TEN);
      assertEquals(metricsManager.getMaxTime(), BigDecimal.TEN);
      assertEquals(metricsManager.getAverageTime(), BigDecimal.TEN);
      assertEquals(metricsManager.getCount(), 1L);
      assertEquals(metricsManager.get90Percentile(), BigDecimal.TEN);
      assertEquals(metricsManager.get95Percentile(), BigDecimal.TEN);
      assertEquals(metricsManager.get99Percentile(), BigDecimal.TEN);

      // fill metrics so we have complete list of 1 to 100 values
      for (int i = 1; i <= 100; i++) {
         // 10 is already present, so skip it
         if (i != 10) {
            metricsManager.addTime(new BigDecimal(i));
         }
      }

      // wait for percentiles to be updated
      TimeUnit.SECONDS.sleep(2);

      // set avgTime to 50.5
      BigDecimal avgTime = (new BigDecimal(1).add(new BigDecimal(100))).divide(new BigDecimal(2), BigDecimal.ROUND_HALF_UP);

      // we should have exactly 100 values, min = 1, max = 100, avg = 50.5, 90 percentile = 91, 95 percentile = 96 and 99 percentile = 100
      assertEquals(metricsManager.getValues().size(), 100);
      assertEquals(metricsManager.getMinTime(), BigDecimal.ONE);
      assertEquals(metricsManager.getMaxTime(), new BigDecimal(100));
      assertEquals(metricsManager.getAverageTime(), avgTime);
      assertEquals(metricsManager.getCount(), 100L);
      assertEquals(metricsManager.get90Percentile(), new BigDecimal(91));
      assertEquals(metricsManager.get95Percentile(), new BigDecimal(96));
      assertEquals(metricsManager.get99Percentile(), new BigDecimal(100));

      // wait 61 seconds, so metrics will delete values
      TimeUnit.SECONDS.sleep(61);

      // values should be empty, but all metrics should persist
      assertTrue(metricsManager.getValues().isEmpty());
      assertEquals(metricsManager.getMinTime(), BigDecimal.ONE);
      assertEquals(metricsManager.getMaxTime(), new BigDecimal(100));
      assertEquals(metricsManager.getAverageTime(), avgTime);
      assertEquals(metricsManager.getCount(), 100L);
      assertEquals(metricsManager.get90Percentile(), new BigDecimal(91));
      assertEquals(metricsManager.get95Percentile(), new BigDecimal(96));
      assertEquals(metricsManager.get99Percentile(), new BigDecimal(100));
   }

   @Test
   public void testPercentilesUpdate() throws InterruptedException {
      BigDecimal time = getRandomTime();

      metricsManager.addTime(time);

      // percentiles are updated every second, so they are still set to default immediately after inserting
      assertEquals(metricsManager.get90Percentile(), BigDecimal.ZERO);
      assertEquals(metricsManager.get95Percentile(), BigDecimal.ZERO);
      assertEquals(metricsManager.get99Percentile(), BigDecimal.ZERO);

      // wait for update of percentiles
      TimeUnit.SECONDS.sleep(2);

      // percentiles should be updated now
      assertEquals(metricsManager.get90Percentile(), time);
      assertEquals(metricsManager.get95Percentile(), time);
      assertEquals(metricsManager.get99Percentile(), time);
   }
}
