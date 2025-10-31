package src.auth;

import com.auth0.AuthenticationController;
import com.auth0.IdentityVerificationException;
import com.auth0.Tokens;
import io.github.cromat.JavaxRequest;
import io.github.cromat.JavaxResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.AutoApplySession;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStoreHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Handling callback workflow
 */

@ApplicationScoped
@AutoApplySession
public class Auth0AuthenticationMechanism implements HttpAuthenticationMechanism {
    private final AuthenticationController authenticationController;
    private final IdentityStoreHandler identityStoreHandler;

    @Inject
    Auth0AuthenticationMechanism(AuthenticationController authenticationController, IdentityStoreHandler identityStoreHandler) {
        this.authenticationController = authenticationController;
        this.identityStoreHandler = identityStoreHandler;
    }

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            HttpMessageContext httpMessageContext) throws AuthenticationException {

        // Exchange the code for the ID token, and notify container of result.
        if (isCallbackRequest(httpServletRequest)) {
            try {
                Tokens tokens = authenticationController.handle( new JavaxRequest(httpServletRequest), new JavaxResponse(httpServletResponse));
                Auth0JwtCredential auth0JwtCredential = new Auth0JwtCredential(tokens.getIdToken());
                CredentialValidationResult result = identityStoreHandler.validate(auth0JwtCredential);
                return httpMessageContext.notifyContainerAboutLogin(result);
            } catch (IdentityVerificationException e) {
                return httpMessageContext.responseUnauthorized();
            }
        }
        return httpMessageContext.doNothing();
    }

    private boolean isCallbackRequest(HttpServletRequest request) {
        return request.getRequestURI().equals("/callback") && request.getParameter("code") != null;
    }
}
