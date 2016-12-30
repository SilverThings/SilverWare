package io.silverware.microservices.healthcheck;

import io.silverware.microservices.annotations.Microservice;
import io.silverware.microservices.annotations.MicroserviceReference;
import io.silverware.microservices.providers.cdi.builtin.CurrentContext;
import io.silverware.microservices.silver.CdiSilverService;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Endpoint which exposes status of SilverWare
 * default http://localhost:8080/silverware/rest/health/status
 * Configurable via properties in HttpServerSilverService
 * Alpha version:
 *  just CdiSilverService and HttpServerSilverService status is checked
 *
 * @author Slavomír Krupa (slavomir.krupa@gmail.com)
 */
@Microservice
@Path("health")
public class HealthStatus {

   @Inject
   @MicroserviceReference
   private CurrentContext context;

   @GET
   @Path("status")
   public Response status() {
      final boolean isDeployed = ((CdiSilverService) this.context.getContext().getProvider(CdiSilverService.class)).isDeployed();
      return Response.status(isDeployed ? Response.Status.OK : Response.Status.FORBIDDEN).build();
   }
}