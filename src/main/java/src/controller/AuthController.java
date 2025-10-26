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
import src.model.User;
import src.repository.UserRepository;

@Named
@RequestScoped
@SuppressWarnings("unused")
public class AuthController {

  @Inject private UserRepository repository;

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

  public String extractEmail(String idToken) {
    try {
      DecodedJWT jwt = JWT.decode(idToken);
      Map<String, Claim> claims = jwt.getClaims();
      return claims.get("email").asString();
    } catch (Exception e) {
      return null;
    }
  }

  public String extractEmail(HttpServletRequest request) {
    String idToken = this.getCookieByName(request, "jwt");
    return this.extractEmail(idToken);
  }

  public boolean mailExistsAndIsConfirmed(HttpServletRequest request) {
    String email = this.extractEmail(request);

    if (email == null) {
      return false;
    }

    User user = repository.findByEmail(email);

    return user.isConfirmed();
  }

  public String getBaseURL(HttpServletRequest request) {
    return String.format(
        "%s://%s:%d%s/application",
        request.getScheme(),
        request.getServerName(),
        request.getServerPort(),
        request.getContextPath());
  }
}
