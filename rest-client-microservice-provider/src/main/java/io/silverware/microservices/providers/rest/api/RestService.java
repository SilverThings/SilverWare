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
package io.silverware.microservices.providers.rest.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.silverware.microservices.SilverWareException;

/**
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 */
public interface RestService {

   Object call(final String method, final Map<String, Object> params) throws SilverWareException;

   default Object call(final String method, final String[] paramNames, final Object... params) throws SilverWareException {
      final Map<String, Object> p = new HashMap<>();

      for (int i = 0; i < paramNames.length; i++) {
         p.put(paramNames[i], params[i]);
      }

      return call(method, p);
   }

   default Object call(final String method, final String paramName, final Object param) throws SilverWareException {
      return call(method, Collections.singletonMap(paramName, param));
   }
}
