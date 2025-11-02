package src.auth;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import java.security.PublicKey;

public class JwksKeyProvider {

  private static final JwkProvider provider =
      new UrlJwkProvider("https://dev-rqhpuzb3altnalx3.us.auth0.com");

  public static PublicKey getPublicKey() {
    try {
      return provider.get("I9aDdUdFIEzjEq29f_Gvm").getPublicKey();
    } catch (Exception e) {
      throw new RuntimeException("JWKS Fehler", e);
    }
  }
}
