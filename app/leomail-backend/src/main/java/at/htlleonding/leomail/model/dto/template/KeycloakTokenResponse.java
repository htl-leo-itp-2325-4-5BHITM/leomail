package at.htlleonding.leomail.model.dto.template;

public record KeycloakTokenResponse(String access_token,
                                    String refresh_token,
                                    String expires_in,
                                    String refresh_expires_in,
                                    String token_type,
                                    String not_before_policy,
                                    String session_state,
                                    String scope) {
}
