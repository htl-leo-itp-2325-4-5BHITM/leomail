package at.htlleonding.leomail.contracts;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "keycloak-api")
@Produces(MediaType.APPLICATION_FORM_URLENCODED)
@Consumes(MediaType.APPLICATION_JSON)
public interface KeycloakAuthClient {

    @POST
    @Path("/protocol/openid-connect/token")
    Response login(@FormParam("grant_type") String grantType,
                   @FormParam("client_id") String clientId,
                   @FormParam("client_secret") String clientSecret,
                   @FormParam("username") String username,
                   @FormParam("password") String password,
                   @FormParam("scope") String scope);
}
