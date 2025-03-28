package at.htlleonding.leomail.model.dto.auth;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public record JwtTest(Set<String> audience, List<JwtClaimTest> claims, Long expirationTime, Set<String> groups,
                      Long issuedAtTime,
                      String issuer, String name, String rawToken, String subject,
                      String tokenID) implements Serializable {
}
