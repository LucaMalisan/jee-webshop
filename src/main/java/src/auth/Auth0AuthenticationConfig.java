package src.auth;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Getter;

/**
 * Container class to store required auth0 parameters
 */

@ApplicationScoped
@Getter
public class Auth0AuthenticationConfig {

    private String domain;
    private String clientId;
    private String clientSecret;
    private String scope;

    @PostConstruct
    public void init() {
        // Get authentication config values from env
            this.domain = System.getenv(("AUTH0_DOMAIN"));
            this.clientId = System.getenv(("AUTH0_CLIENTID"));
            this.clientSecret = System.getenv(("AUTH0_CLIENTSECRET"));
            this.scope = System.getenv(("AUTH0_SCOPE"));

        if (this.domain == null || this.clientId == null || this.clientSecret == null || this.scope == null) {
            throw new IllegalArgumentException("domain, clientId, clientSecret, and scope are not set");
        }
    }
}