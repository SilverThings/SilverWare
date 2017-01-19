/*
 * -----------------------------------------------------------------------\
 * SilverWare
 *  
 * Copyright (C) 2015 the original author or authors.
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
package io.silverware.microservices.silver.cluster;

import io.silverware.microservices.Context;

import java.io.Serializable;

/**
 * This class represents a handle for a service object
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public interface ServiceHandle extends Serializable {

   /**
    * @return proxy for a given service handle
    */
   Object getProxy();

   String getHost();

   /**
    * This method invokes requested method for a given handle
    * and
    *
    * @param context    of the
    * @param method     name of the method to be called
    * @param params     parameters of method called
    * @param paramTypes classes of parameters
    * @return result of call
    * @throws Exception if exception has occurred during invocation
    */
   Object invoke(Context context, String method, Class[] paramTypes, Object[] params) throws Exception;

}
