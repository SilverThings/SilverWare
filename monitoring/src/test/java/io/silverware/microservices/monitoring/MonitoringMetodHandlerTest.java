package io.silverware.microservices.monitoring;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;

import io.silverware.microservices.monitoring.annotations.NotMonitored;
import io.silverware.microservices.providers.cdi.MicroservicesStartedEvent;
import io.silverware.microservices.providers.cdi.internal.MicroserviceMethodHandler;
import io.silverware.microservices.providers.cdi.internal.MicroserviceProxyBean;

/**
 * @author Tomas Borcin | tborcin@redhat.com | created: 19.12.16.
 */
public class MonitoringMetodHandlerTest {

   private static final String TEST_MICROSERVICE_NAME = "TestMicroservice";

   private MicroserviceProxyBean microserviceProxyBean;
   private MicroserviceMethodHandler parentMethodHandler;

   @BeforeMethod
   public void setUpMethodHandlers() {
      microserviceProxyBean = Mockito.mock(MicroserviceProxyBean.class);
      Mockito.when(microserviceProxyBean.getMicroserviceName()).thenReturn(TEST_MICROSERVICE_NAME);

      parentMethodHandler = Mockito.mock(MicroserviceMethodHandler.class);
      Mockito.when(parentMethodHandler.getProxyBean()).thenReturn(microserviceProxyBean);
   }

   @Test
   public void testGetProxyBean() {
      MonitoringMethodHandler monitoringMethodHandler = new MonitoringMethodHandler(parentMethodHandler);
      assertEquals(monitoringMethodHandler.getProxyBean(), microserviceProxyBean);
   }

   @Test
   public void testMethodInvocation() throws Exception {
      MonitoringMethodHandler monitoringMethodHandler = new MonitoringMethodHandler(parentMethodHandler);

      // mock TestMicroserviceA
      Method methodA = TestMicroserviceA.class.getMethod("hello");
      Mockito.when(parentMethodHandler.invoke(methodA)).thenReturn("InvokedA");

      // invoke method of TestMicroserviceA
      assertEquals(monitoringMethodHandler.invoke(methodA), "InvokedA");

      // MetricsAggregator should register method
      assertEquals(MetricsAggregator.getMicroserviceRegister().size(), 1);

      // mock TestMicroserviceB
      Method methodB = TestMicroserviceB.class.getMethod("hello");
      Mockito.when(parentMethodHandler.invoke(methodB)).thenReturn("InvokedB");

      // invoke method of TestMicroserviceB
      assertEquals(monitoringMethodHandler.invoke(methodB), "InvokedB");

      // MetricsAgregator should not register method of TestMicroserviceB and its register size should remain 1
      assertEquals(MetricsAggregator.getMicroserviceRegister().size(), 1);
   }

   @Test
   public void testMBeanIsRegistered() throws Exception {
      MonitoringMethodHandler monitoringMethodHandler = new MonitoringMethodHandler(parentMethodHandler);

      // mock TestMicroserviceA
      Method methodA = TestMicroserviceA.class.getMethod("hello");
      Mockito.when(parentMethodHandler.invoke(methodA)).thenReturn("InvokedA");

      // invoke method of TestMicroserviceA
      assertEquals(monitoringMethodHandler.invoke(methodA), "InvokedA");

      MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

      // test if MBean is registered
      assertTrue(mbs.isRegistered(new ObjectName("JMX_Publisher:name=JMXPublisherBean")));
   }

   // Sample microservice with monitored method
   private static class TestMicroserviceA {
      public TestMicroserviceA() {
         System.out.println("TestMicroserviceA microservice constructor");
      }

      @PostConstruct
      public void onInit() {
         System.out.println("TestMicroserviceA microservice PostConstruct " + this.getClass().getName());
      }

      public String hello() {
         return "HelloA";
      }

      public void eventObserver(@Observes MicroservicesStartedEvent event) {
         System.out.println("TestMicroserviceA microservice MicroservicesStartedEvent");
      }
   }

   // Sample microservice with not monitored method
   private static class TestMicroserviceB {
      public TestMicroserviceB() {
         System.out.println("TestMicroserviceB microservice constructor");
      }

      @PostConstruct
      public void onInit() {
         System.out.println("TestMicroserviceB microservice PostConstruct " + this.getClass().getName());
      }

      @NotMonitored
      public String hello() {
         return "HelloB";
      }

      public void eventObserver(@Observes MicroservicesStartedEvent event) {
         System.out.println("TestMicroserviceB microservice MicroservicesStartedEvent");
      }
   }
}
