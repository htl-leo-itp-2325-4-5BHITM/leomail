package at.htlleonding.leomail.resources;

import at.htlleonding.leomail.services.PermissionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("permission")
public class PermissionResource {

    @Inject
    PermissionService permissionService;

    @Inject
    JsonWebToken jwt;

    @GET
    @Produces("application/json")
    @Path("check")
    public boolean checkPermission(@QueryParam("pid") String projectId) {
        return permissionService.hasPermission(projectId, jwt.getClaim("sub"));
    }
}
