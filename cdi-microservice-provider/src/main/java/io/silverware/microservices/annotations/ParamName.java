package io.silverware.microservices.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by mswiech on 12.1.16.
 * This annotation defines argument name for method parameter.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParamName {

   /**
    * Defined name for method parameter.
    * @return defined name for method parameter
    */
   String value();

}
