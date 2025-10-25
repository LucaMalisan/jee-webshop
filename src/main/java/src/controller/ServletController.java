package src.controller;

import com.auth0.AuthenticationController;
import com.auth0.IdentityVerificationException;
import com.auth0.Tokens;
import com.auth0.json.auth.UserInfo;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.mailjet.client.errors.MailjetException;
import io.github.cromat.JavaxRequest;
import io.github.cromat.JavaxResponse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.mvc.Controller;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import javax.naming.NamingException;
import lombok.extern.java.Log;
import src.auth.Auth0AuthenticationConfig;
import src.auth.AuthMailSender;
import src.model.UserEmailConfirmed;
import src.repository.UserEmailConfirmedRepository;

@Controller
@RequestScoped
@Log
@Path("/")
public class ServletController {

  @Inject private Auth0AuthenticationConfig config;

  @Inject private AuthenticationController authenticationController;

  @Inject private UserEmailConfirmedRepository repository;
  @Inject private UserEmailConfirmedRepository userEmailConfirmedRepository;

  @GET
  public String index() {
    return "list.xhtml";
  }

  @GET
  @Path("/detail")
  public String detail() {
    return "detail.xhtml";
  }

  @GET
  @Path("/login")
  public Response login(@Context HttpServletRequest request, @Context HttpServletResponse response)
      throws URISyntaxException {
    HttpSession session = request.getSession(true);

    // URL where the application will receive the authorization code (e.g.,
    // http://localhost:3000/callback)
    String callbackUrl =
        String.format(
            "%s://%s:%s%s/application/callback",
            request.getScheme(),
            request.getServerName(),
            request.getServerPort(),
            request.getContextPath());

    // Create the authorization URL to redirect the user to, to begin the authentication flow.
    String authURL =
        authenticationController
            .buildAuthorizeUrl(new JavaxRequest(request), new JavaxResponse(response), callbackUrl)
            .withScope(config.getScope())
            .build();

    return Response.temporaryRedirect(new URI(authURL)).build();
  }

  @GET
  @Path("/callback")
  public Response callback(
      @Context HttpServletRequest request, @Context HttpServletResponse response)
      throws URISyntaxException, IdentityVerificationException, NamingException, MailjetException {

    Tokens tokens =
        authenticationController.handle(new JavaxRequest(request), new JavaxResponse(response));
    String idToken = tokens.getIdToken(); // JWT mit User-Claims

    String baseURL =
        String.format(
            "%s://%s:%d%s/application/", // /jee-webshop wird durch getContextPath() ersetzt
            request.getScheme(),
            request.getServerName(),
            request.getServerPort(),
            request.getContextPath());

    try {
      // extract email from generated JWT
      DecodedJWT jwt = JWT.decode(idToken);
      Map<String, Claim> claims = jwt.getClaims();
      String email = claims.get("email").asString();

      UserEmailConfirmed emailConfirmed = repository.findByEmail(email);
      if (emailConfirmed == null) {

        // create entry if user is new
        emailConfirmed = new UserEmailConfirmed();
        emailConfirmed.setEmail(email);
        emailConfirmed.setConfirmKey(UUID.randomUUID().toString());
        repository.save(emailConfirmed);

        // send email with generated confirm key
        new AuthMailSender().sendMail(emailConfirmed, baseURL);
      }

      response.addCookie(new Cookie("name", email));
    } catch (Exception e) {
      log.severe(e.getMessage());
    }

    return Response.temporaryRedirect(new URI(baseURL)).build();
  }
}
