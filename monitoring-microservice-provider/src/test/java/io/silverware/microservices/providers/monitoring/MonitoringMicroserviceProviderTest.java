package io.silverware.microservices.providers.monitoring;

import io.silverware.microservices.providers.http.HttpServerMicroserviceProvider;
import io.silverware.microservices.silver.HttpServerSilverService;
import io.silverware.microservices.silver.MonitoringSilverService;
import io.silverware.microservices.util.BootUtil;
import io.silverware.microservices.util.Utils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class MonitoringMicroserviceProviderTest {

   private MonitoringSilverService monitoringSilverService = null;

   @Test
   public void testMonitoring() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Map<String, Object> platformProperties = bootUtil.getContext().getProperties();
      platformProperties.put(HttpServerSilverService.HTTP_SERVER_PORT, 18080);
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