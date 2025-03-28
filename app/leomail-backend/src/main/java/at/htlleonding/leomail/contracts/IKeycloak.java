package at.htlleonding.leomail.contracts;

import at.htlleonding.leomail.model.dto.KeycloakTokenIntrospectionResponse;
import at.htlleonding.leomail.model.dto.template.KeycloakTokenResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.FormParam;

import java.util.List;
import java.util.Map;

@Path("/realms/2425-5bhitm/protocol/openid-connect")
@RegisterRestClient(configKey = "keycloak-admin-api")
public interface IKeycloak {

    /**
     * Benutzer-Login.
     *
     * @param clientId     Client ID
     * @param clientSecret Client Secret
     * @param grantType    Grant Type, z.B. "password"
     * @param username     Benutzername
     * @param password     Passwort
     * @param scope        Scopes, z.B. "openid"
     * @return Token-Antwort
     */
    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    KeycloakTokenResponse login(
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("grant_type") String grantType,
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("scope") String scope
    );

    /**
     * Token-Refresh.
     *
     * @param clientId     Client ID
     * @param clientSecret Client Secret
     * @param grantType    Grant Type, z.B. "refresh_token"
     * @param refreshToken Refresh Token
     * @return Token-Antwort
     */
    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    KeycloakTokenResponse refreshToken(
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("grant_type") String grantType,
            @FormParam("refresh_token") String refreshToken
    );

    /**
     * Token-Introspektion.
     *
     * @param clientId     Client ID
     * @param clientSecret Client Secret
     * @param token        Token zur Introspektion
     * @return Introspektionsantwort
     */
    @POST
    @Path("/token/introspect")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    KeycloakTokenIntrospectionResponse introspectToken(
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("token") String token
    );

    /**
     * Service Account Login mit client_credentials Grant Type.
     *
     * @param clientId     Client ID
     * @param clientSecret Client Secret
     * @param grantType    Grant Type, z.B. "client_credentials"
     * @return Token-Antwort
     */
    @POST
    @Path("/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    KeycloakTokenResponse serviceAccountLogin(
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("grant_type") String grantType
    );

    /**
     * Service Account Login mit client_credentials Grant Type.
     *
     * @param clientId     Client ID
     * @param clientSecret Client Secret
     * @param grantType    Grant Type, z.B. "client_credentials"
     * @param scope        Scope, z.B. "openid"
     * @return Token-Antwort
     */
    @POST
    @Path("/protocol/openid-connect/token")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    KeycloakTokenResponse serviceAccountLogin(
            @FormParam("client_id") String clientId,
            @FormParam("client_secret") String clientSecret,
            @FormParam("grant_type") String grantType,
            @FormParam("scope") String scope
    );

    /**
     * Sucht Benutzer basierend auf einem Suchbegriff.
     *
     * @param authorization Authorization Header mit Bearer Token
     * @param realm         Realm-Name
     * @param searchTerm    Suchbegriff
     * @param maxResults    Maximale Anzahl der Ergebnisse
     * @return Liste der gefundenen Benutzer
     */
    @GET
    @Path("/admin/realms/{realm}/users")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    List<Map<String, Object>> searchUsers(
            @HeaderParam("Authorization") String authorization,
            @PathParam("realm") String realm,
            @QueryParam("search") String searchTerm,
            @QueryParam("max") int maxResults
    );
}