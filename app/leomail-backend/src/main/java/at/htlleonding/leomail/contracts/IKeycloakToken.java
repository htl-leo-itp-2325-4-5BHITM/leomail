package at.htlleonding.leomail.contracts;

import at.htlleonding.leomail.model.dto.KeycloakTokenIntrospectionResponse;
import at.htlleonding.leomail.model.dto.template.KeycloakTokenResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.FormParam;

@RegisterRestClient(configKey = "keycloak-token-api")
@Path("/realms/{realm}/protocol/openid-connect")
public interface IKeycloakToken {

    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    KeycloakTokenResponse serviceAccountLogin(
            @PathParam("realm") String realm,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("grant_type") String grantType,
            @FormParam("scope") String scope
    );

    @POST
    @Path("/token/introspect")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    KeycloakTokenIntrospectionResponse introspectToken(
            @PathParam("realm") String realm,
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("token") String token
    );
}
