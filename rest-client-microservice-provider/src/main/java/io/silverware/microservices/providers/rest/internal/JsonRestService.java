/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2014 - 2016 the original author or authors.
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
package io.silverware.microservices.providers.rest.internal;

import io.silverware.microservices.SilverWareException;
import io.silverware.microservices.providers.rest.api.RestService;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public class JsonRestService implements RestService {

   private static final Logger log = LogManager.getLogger(JsonRestService.class);

   private String endpoint;
   private String httpMethod;

   public JsonRestService(final String endpoint, final String httpMethod) {
      this.endpoint = endpoint;
      this.httpMethod = httpMethod;
   }

   @Override
   public Object call(final String method, final Map<String, Object> params) throws SilverWareException {
      try {
         HttpURLConnection con = (HttpURLConnection) new URL(endpoint + "/" + method).openConnection();
         con.setRequestMethod(httpMethod);
         con.setDoInput(true);
         con.setDoOutput(true);
         con.connect();

         JsonWriter jsonWriter = new JsonWriter(con.getOutputStream(), Collections.singletonMap(JsonWriter.TYPE, false));
         jsonWriter.write(params);
         JsonReader jsonReader = new JsonReader(con.getInputStream());
         JsonObject jsonResponse = (JsonObject) jsonReader.readObject();
         Object response = JsonReader.jsonToJava((String) jsonResponse.get("resultPlain"));

         con.disconnect();

         return response;
      } catch (IOException e) {
         throw new SilverWareException(String.format("Cannot call method '%s': ", method), e);
      }
   }
}
