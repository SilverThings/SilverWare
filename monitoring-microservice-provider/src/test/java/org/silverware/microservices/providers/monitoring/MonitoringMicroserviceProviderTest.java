package org.silverware.microservices.providers.monitoring;

import org.silverware.microservices.providers.http.HttpServerMicroserviceProvider;
import org.silverware.microservices.silver.HttpServerSilverService;
import org.silverware.microservices.silver.MonitoringSilverService;
import org.silverware.microservices.util.BootUtil;
import org.silverware.microservices.util.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author Martin Večeřa <marvenec@gmail.com>
 */
public class MonitoringMicroserviceProviderTest {

   private MonitoringSilverService monitoringSilverService = null;

   @Test
   public void testMonitoring() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Map<String, Object> platformProperties = bootUtil.getContext().getProperties();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName(), HttpServerMicroserviceProvider.class.getPackage().getName());
      platform.start();

      while (monitoringSilverService == null) {
         monitoringSilverService = (MonitoringSilverService) bootUtil.getContext().getProvider(MonitoringSilverService.class);
         Thread.sleep(200);
      }

      Assert.assertTrue(Utils.waitForHttp("http://" + platformProperties.get(HttpServerSilverService.HTTP_SERVER_ADDRESS) + ":" +
            platformProperties.get(HttpServerSilverService.HTTP_SERVER_PORT) + "/" +
            platformProperties.get(MonitoringSilverService.MONITORING_URL) + "/", 200));

      platform.interrupt();
      platform.join();
   }

}