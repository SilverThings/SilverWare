package io.silverware.microservices.monitoring;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Tomas Borcin | tborcin@redhat.com | created: 20.12.16.
 */
public class MetricsParentTest {

   protected BigDecimal getRandomTime() {
      return getRandomTime(1, 1000);
   }

   protected BigDecimal getRandomTime(int min, int max) {
      return new BigDecimal(ThreadLocalRandom.current().nextInt(min, max + 1));
   }
}
