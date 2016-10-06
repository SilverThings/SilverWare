package io.silverware.microservices.providers.rest.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Provides configuration for the injected {@link io.silverware.microservices.providers.rest.api.RestService}
 *
 * @author Radek Koubsky (radek.koubsky@gmail.com)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceConfiguration {
   /**
    * An endpoint where a service is accessed. The base URL (hostname, port, context etc.) for the Rest services
    * within Silverware is defined in global properties.
    *
    * @return the endpoint of the service
    * @see io.silverware.microservices.silver.HttpServerSilverService
    */
   String endpoint() default "";

   /**
    * Defines what implementation of the {@link io.silverware.microservices.providers.rest.api.RestService}
    * interface is used. Default value is <b>"default"</b> for default implementation.
    * <p><b>NOTE:</b>
    * Currently, there is only default implementation provided.
    * </p>
    *
    * @return type of the rest service implementation
    */
   String type() default "default";
}
