package src.controller;

import com.auth0.AuthenticationController;
import io.github.cromat.JavaxRequest;
import io.github.cromat.JavaxResponse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.extern.java.Log;
import src.auth.Auth0AuthenticationConfig;

@Controller
@RequestScoped
@Log
@Path("/")
public class ServletController {

  @Inject private Auth0AuthenticationConfig config;

  @Inject private AuthenticationController authenticationController;

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
            request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath());

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
  public Response callback(@Context HttpServletRequest request) throws URISyntaxException {
    String callbackPath = String.format("%s/application/", request.getContextPath());
    return Response.temporaryRedirect(new URI(callbackPath)).build();
  }
}
