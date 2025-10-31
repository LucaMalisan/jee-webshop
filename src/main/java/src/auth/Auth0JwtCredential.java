package src.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.security.enterprise.credential.Credential;
import lombok.Getter;

/**
 * Container class for auth-principal
 */

public class Auth0JwtCredential implements Credential {

    @Getter
    private Auth0JwtPrincipal auth0JwtPrincipal;

    Auth0JwtCredential(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        this.auth0JwtPrincipal = new Auth0JwtPrincipal(decodedJWT);
    }
}
