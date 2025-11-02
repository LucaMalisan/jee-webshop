package src.auth;

import io.jsonwebtoken.JwtException;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtApiFilter implements ContainerRequestFilter {

  private final Auth0JwtValidator validator;

  public JwtApiFilter() {
    String issuer = "https://dev-rqhpuzb3altnalx3.us.auth0.com/";
    String audience = "http://localad-9cineg7:8080/jee-webshop/application/api";
    PublicKey publicKey = JwksKeyProvider.getPublicKey(); // siehe unten

    this.validator = new Auth0JwtValidator(issuer, audience, publicKey);
  }

  @Override
  public void filter(ContainerRequestContext request) throws IOException {

    Logger logger = Logger.getLogger(JwtApiFilter.class.getName());
    logger.log(Level.INFO, request.getUriInfo().getPath());

    if (request.getUriInfo().getPath().matches("api/.*")) {
      String authHeader = request.getHeaderString(HttpHeaders.AUTHORIZATION);
      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }

      String token = authHeader.substring(7);

      try {
        validator.validate(token);
      } catch (JwtException e) {
        throw new WebApplicationException(Response.Status.UNAUTHORIZED);
      }
    }
  }
}
