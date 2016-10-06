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

import javax.ws.rs.client.WebTarget;

import io.silverware.microservices.SilverWareException;

/**
 * Represents a client of a Rest service endpoint to which request are sent.
 *
 * @author <a href="mailto:marvenec@gmail.com">Martin Večeřa</a>
 * @author Radek Koubsky (radek.koubsky@gmail.com)
 */
public interface RestService {

   /**
    * Returns {@link WebTarget} that is used to create requests
    * to the service endpoint defined by {@link io.silverware.microservices.providers.rest.annotation.ServiceConfiguration}
    */
   WebTarget target();
}
