package src.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import java.security.PublicKey;

public class Auth0JwtValidator {

    private final JwtParser jwtParser;

    public Auth0JwtValidator(String issuer, String audience, PublicKey publicKey) {
        this.jwtParser = Jwts.parser()
                .verifyWith(publicKey)           // Öffentlicher RSA-Schlüssel
                .requireIssuer(issuer)           // z. B. https://deine-domain.auth0.com/
                .requireAudience(audience)       // z. B. https://api.meinefirma.com
                .clockSkewSeconds(30)            // Toleranz für Zeitabweichung
                .build();
    }

    public Jws<Claims> validate(String token) {
        return jwtParser.parseSignedClaims(token); // Wirft Exception bei Fehler
    }
}
