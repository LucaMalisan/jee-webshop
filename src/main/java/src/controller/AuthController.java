package src.controller;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import lombok.Getter;
import src.model.User;
import src.repository.UserRepository;

@Getter
@Named
@RequestScoped
@SuppressWarnings("unused")
public class AuthController {

  private UserRepository repository;

  public AuthController() {}

  @Inject
  public AuthController(UserRepository repository) {
    this.repository = repository;
  }

  /**
   * Find cookie in request by name
   *

   * @param request: request
   * @param name: cookie name
   * @return cookie value
   */
  public String getCookieByName(HttpServletRequest request, String name) {
    if (request.getCookies() != null) {
      return Arrays.stream(request.getCookies())
          .filter(e -> Objects.equals(e.getName(), name))
          .map(Cookie::getValue)
          .findFirst()
          .orElse(null);
    }
    return null;
  }

  /**
   * Parse email out of jwt
   *
   * @param idToken id-token obtained from auth0
   * @return parsed email
   */
  public String extractEmail(String idToken) {
    try {
      DecodedJWT jwt = JWT.decode(idToken);
      Map<String, Claim> claims = jwt.getClaims();
      return claims.get("email").asString();
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Parse email out of request
   *
   * @param request: request
   * @return parsed email
   */
  public String extractEmail(HttpServletRequest request) {
    String idToken = this.getCookieByName(request, "jwt");
    return this.extractEmail(idToken);
  }

  /**
   * Parse email out of request and check if it exist and is validated
   *
   * @param request: request
   * @return result of check
   */
  public boolean mailExistsAndIsConfirmed(HttpServletRequest request) {
    String email = this.extractEmail(request);

    if (email == null) {
      return false;
    }

    User user = repository.findByEmail(email);

    return user != null && user.isConfirmed();
  }

  /**
   * Helper-method to dynamically assemble base URL of application by request
   *
   * @param request: requet
   * @return dynamic base URL
   */
  public String getBaseURL(HttpServletRequest request) {
    return String.format(
        "%s://%s:%d%s/application",
        request.getScheme(),
        request.getServerName(),
        request.getServerPort(),
        request.getContextPath());
  }
}
