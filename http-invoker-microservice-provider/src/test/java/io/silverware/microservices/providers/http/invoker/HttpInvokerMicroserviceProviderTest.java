package io.silverware.microservices.providers.http.invoker;

import io.silverware.microservices.MicroserviceMetaData;
import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.providers.cdi.CdiMicroserviceProvider;
import io.silverware.microservices.providers.http.HttpServerMicroserviceProvider;
import io.silverware.microservices.providers.http.invoker.internal.HttpServiceHandle;
import io.silverware.microservices.silver.CdiSilverService;
import io.silverware.microservices.silver.HttpInvokerSilverService;
import io.silverware.microservices.silver.HttpServerSilverService;
import io.silverware.microservices.silver.cluster.Invocation;
import io.silverware.microservices.util.BootUtil;
import io.silverware.microservices.util.Utils;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class HttpInvokerMicroserviceProviderTest {

   private HttpInvokerSilverService httpInvokerSilverService = null;

   @Test
   public void testHttpInvoker() throws Exception {
      final BootUtil bootUtil = new BootUtil();
      final Map<String, Object> platformProperties = bootUtil.getContext().getProperties();
      final Thread platform = bootUtil.getMicroservicePlatform(this.getClass().getPackage().getName(), HttpServerMicroserviceProvider.class.getPackage().getName(), CdiMicroserviceProvider.class.getPackage().getName());
      platform.start();

      while (httpInvokerSilverService == null) {
         httpInvokerSilverService = (HttpInvokerSilverService) bootUtil.getContext().getProvider(HttpInvokerSilverService.class);
         Thread.sleep(200);
      }

      while (bootUtil.getContext().getProperties().get(CdiSilverService.BEAN_MANAGER) == null) {
         Thread.sleep(200);
      }

      String urlBase = "http://" + platformProperties.get(HttpServerSilverService.HTTP_SERVER_ADDRESS) + ":" + platformProperties.get(HttpServerSilverService.HTTP_SERVER_PORT) + "/" + platformProperties.get(HttpInvokerSilverService.INVOKER_URL) + "/";

      Assert.assertTrue(Utils.waitForHttp(urlBase, 204));

      HttpURLConnection con = (HttpURLConnection) new URL(urlBase + "query").openConnection();
      con.setRequestMethod("POST");
      con.setDoInput(true);
      con.setDoOutput(true);
      con.connect();

      final MicroserviceMetaData metaData = new MicroserviceMetaData("sumService", SumService.class, Collections.emptySet(), Collections.emptySet(), null, null);
      JsonWriter jsonWriter = new JsonWriter(con.getOutputStream());
      jsonWriter.write(metaData);

      Assert.assertEquals(con.getResponseMessage(), "OK");
      JsonReader jsonReader = new JsonReader(con.getInputStream());
      final List<HttpServiceHandle> handles = (List<HttpServiceHandle>) jsonReader.readObject();
      Assert.assertEquals(handles.size(), 1);

      con.disconnect();

      final HttpServiceHandle handle = handles.get(0);
      Utils.waitForCDIProvider(bootUtil.getContext());
      long l = (Long) handle.invoke(bootUtil.getContext(), "sum", new Class[] { short.class, int.class }, new Object[] { (short) 3, 4 });
      Assert.assertEquals(l, 7L);

      con = (HttpURLConnection) new URL(urlBase + "invoke").openConnection();
      con.setRequestMethod("POST");
      con.setDoInput(true);
      con.setDoOutput(true);
      con.connect();

      Invocation invocation = new Invocation(handles.get(0).getHandle(), "sum", new Class[] { short.class, int.class }, new Object[] { (short) 3, 4 });
      jsonWriter = new JsonWriter(con.getOutputStream());
      jsonWriter.write(invocation);
      jsonReader = new JsonReader(con.getInputStream());
      Object response = jsonReader.readObject();

      Assert.assertEquals(response, 7L);

      con.disconnect();

      con = (HttpURLConnection) new URL(urlBase + "invoke").openConnection();
      con.setRequestMethod("POST");
      con.setDoInput(true);
      con.setDoOutput(true);
      con.connect();

      invocation = new Invocation(handles.get(0).getHandle(), "allTypes", new Class[] { byte.class, short.class, int.class, long.class, float.class, double.class, boolean.class, char.class }, new Object[] { Byte.MAX_VALUE, Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MIN_VALUE, Double.MIN_VALUE, true, 'c' });

      jsonWriter = new JsonWriter(con.getOutputStream());
      jsonWriter.write(invocation);
      jsonReader = new JsonReader(con.getInputStream());
      response = jsonReader.readObject();

      Assert.assertEquals(response, "9.223372036854776E18truec");

      con.disconnect();

      con = (HttpURLConnection) new URL(urlBase + "invoke").openConnection();
      con.setRequestMethod("POST");
      con.setDoInput(true);
      con.setDoOutput(true);
      con.connect();

      invocation = new Invocation(handles.get(0).getHandle(), "allTypes2", new Class[] { Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Boolean.class, Character.class }, new Object[] { Byte.MAX_VALUE, Short.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE, Float.MIN_VALUE, Double.MIN_VALUE, true, 'c' });

      jsonWriter = new JsonWriter(con.getOutputStream());
      jsonWriter.write(invocation);
      jsonReader = new JsonReader(con.getInputStream());
      response = jsonReader.readObject();

      Assert.assertEquals(response, "9.223372036854776E18truec");

      con.disconnect();

      con = (HttpURLConnection) new URL(urlBase + "invoke").openConnection();
      con.setRequestMethod("POST");
      con.setDoInput(true);
      con.setDoOutput(true);
      con.connect();

      MagicBox box = new MagicBox();

      invocation = new Invocation(handles.get(0).getHandle(), "doMagic", new Class[] { MagicBox.class }, new Object[] { box });

      jsonWriter = new JsonWriter(con.getOutputStream());
      jsonWriter.write(invocation);
      jsonReader = new JsonReader(con.getInputStream());
      response = jsonReader.readObject();

      Assert.assertTrue(response instanceof MagicBox);
      Assert.assertEquals((short) ((MagicBox) response).getS(), Short.MAX_VALUE);
      Assert.assertEquals((float) ((MagicBox) response).getF(), Float.MAX_VALUE);

      con.disconnect();

      platform.interrupt();
      platform.join();
   }

   @Test
   public void testIpAddresses() throws Exception {
      Enumeration e = NetworkInterface.getNetworkInterfaces();
      while (e.hasMoreElements()) {
         NetworkInterface n = (NetworkInterface) e.nextElement();
         Enumeration ee = n.getInetAddresses();
         while (ee.hasMoreElements()) {
            InetAddress i = (InetAddress) ee.nextElement();
            System.out.println(i.isLoopbackAddress() + " - " + (i instanceof Inet4Address) + " " + i.getHostAddress());
         }
      }
   }

   public static class MagicBox implements Serializable {
      private Short s = Short.MAX_VALUE;
      private Float f = Float.MAX_VALUE;

      public Short getS() {
         return s;
      }

      public void setS(final Short s) {
         this.s = s;
      }

      public Float getF() {
         return f;
      }

      public void setF(final Float f) {
         this.f = f;
      }

      @Override
      public String toString() {
         return "MagicBox{" + "s=" + s + ", f=" + f + '}';
      }
   }

   @Microservice
   public static class SumService {

      public long sum(short a, int b) {
         return a + b;
      }

      public String allTypes(final byte b, short s, int i, long l, float f, double d, boolean o, char c) {
         return (b + s + i + l + f + d) + String.valueOf(o) + c;
      }

      public String allTypes2(Byte b, Short s, Integer i, Long l, Float f, Double d, Boolean o, Character c) {
         return (b + s + i + l + f + d) + String.valueOf(o) + c;
      }

      public MagicBox doMagic(final MagicBox box) {
         return box;
      }
   }

}