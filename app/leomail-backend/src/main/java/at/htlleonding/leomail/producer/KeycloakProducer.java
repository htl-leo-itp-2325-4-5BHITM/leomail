package at.htlleonding.leomail.producer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class KeycloakProducer {

    @ConfigProperty(name = "quarkus.keycloak.admin-client.server-url")
    String serverUrl;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.realm")
    String realm;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.client-id")
    String clientId;

    @ConfigProperty(name = "quarkus.keycloak.admin-client.client-secret")
    String clientSecret;

    @Produces
    @ApplicationScoped
    public Keycloak produceKeycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .grantType("client_credentials")
                .build();
    }
}
