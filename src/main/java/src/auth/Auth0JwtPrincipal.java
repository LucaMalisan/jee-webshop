package src.auth;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.security.enterprise.CallerPrincipal;
import lombok.Getter;

@Getter
public class Auth0JwtPrincipal extends CallerPrincipal {

    private final DecodedJWT idToken;

    Auth0JwtPrincipal(DecodedJWT idToken) {
        super(idToken.getClaim("name").asString());
        this.idToken = idToken;
    }

}
