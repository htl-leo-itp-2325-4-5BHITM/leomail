package at.htlleonding.leomail.model.dto;

public record KeycloakTokenIntrospectionResponse(boolean active, long exp, long iat, String sub, String username) {
}
