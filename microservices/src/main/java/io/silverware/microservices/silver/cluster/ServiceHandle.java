package io.silverware.microservices.silver.cluster;

import io.silverware.microservices.Context;

import java.io.Serializable;

/**
 *  This class represents a handle for a service object
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
public interface ServiceHandle extends Serializable {

   String getHost();

   Object invoke(Context context, String method, Class[] paramTypes, Object[] params) throws Exception;

   @Deprecated
   Object invoke(Context context, String method, Object[] params) throws Exception;
}
