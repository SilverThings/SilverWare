package io.silverware.microservices.providers.rest.internal;

import javax.ws.rs.client.WebTarget;

import io.silverware.microservices.providers.rest.api.RestService;

/**
 * Default implementation of the Rest service.
 *
 * @author Radek Koubsky (radek.koubsky@gmail.com)
 */
public class DefaultRestService implements RestService {
   private final WebTarget target;

   public DefaultRestService(final WebTarget target) {
      this.target = target;
   }

   @Override
   public WebTarget target() {
      return this.target;
   }
}
