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
import src.model.UserEmailConfirmed;
import src.repository.UserEmailConfirmedRepository;

@Named
@RequestScoped
@SuppressWarnings("unused")
public class AuthController {

  @Inject private UserEmailConfirmedRepository repository;

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

  public boolean mailExistsAndIsConfirmed(String idToken) {
    String email = this.extractEmail(idToken);

    if (email == null) {
      return false;
    }

    UserEmailConfirmed userEmailConfirmed = repository.findByEmail(email);

    return userEmailConfirmed.isConfirmed();
  }
}
