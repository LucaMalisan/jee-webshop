package src.controller;

import com.auth0.AuthenticationController;
import com.auth0.IdentityVerificationException;
import com.auth0.Tokens;
import io.github.cromat.JavaxRequest;
import io.github.cromat.JavaxResponse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.mvc.Controller;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;
import src.auth.Auth0AuthenticationConfig;
import src.auth.AuthMailSender;
import src.model.ShoppingCart;
import src.model.User;
import src.repository.ShoppingCartRepository;
import src.repository.UserRepository;

/** Central class for all REST api methods */
@Path("/")
@RequestScoped
@Controller
public class ApiController {

  @Inject private Auth0AuthenticationConfig config;
  @Inject private AuthenticationController authenticationController;
  @Inject private ShoppingCartController shoppingCartController;
  @Inject private UserRepository repository;
  @Inject private ShoppingCartRepository shoppingCartRepository;
  @Context private HttpServletRequest request;
  @Named @Inject private AuthController authController;

  /**
   * Render main page
   *
   * @return rendered main page
   */
  @GET
  public String index() {
    return "list.xhtml";
  }

  /**
   * Render detail page
   *
   * @return rendered detail page
   */
  @GET
  @Path("/detail")
  public String detail() {
    return "detail.xhtml";
  }

  /**
   * Redirect user to auth0 login page
   *
   * @return 303 to auth0 login page
   */
  @GET
  @Path("/login")
  public Response login(@Context HttpServletRequest request, @Context HttpServletResponse response)
      throws URISyntaxException {
    String callbackUrl = String.format("%s/callback", authController.getBaseURL(request));

    // Create the authorization URL to redirect the user to, to begin the authentication flow.
    String authURL =
        authenticationController
            .buildAuthorizeUrl(new JavaxRequest(request), new JavaxResponse(response), callbackUrl)
            .withScope(config.getScope())
            .build();

    return Response.seeOther(new URI(authURL)).build();
  }

  /**
   * Redirect user to auth0 login page
   */
  @GET
  @Path("/logout")
  public void logout(@Context HttpServletRequest request, @Context HttpServletResponse response) {
    String callbackUrl = authController.getBaseURL(request) + "/logout-callback";

    String logoutUrl =
        String.format(
            "https://%s/v2/logout?client_id=%s&returnTo=%s",
            config.getDomain(), config.getClientId(), callbackUrl);

  //  if (request.getSession() != null) {
  //    request.getSession().invalidate();
  //  }

    if (request.getCookies() != null) {
      Arrays.stream(request.getCookies())
          .filter(e -> e != null && Objects.equals(e.getName(), "jwt"))
          .forEach(
              e -> {
                e.setValue("");
                e.setPath("/");
                e.setMaxAge(0);
                response.addCookie(e);
              });
    }

    try {
      response.sendRedirect(logoutUrl);
    } catch (Exception e) {
    }
  }

  @GET
  @Path("/logout-callback")
  public Response logoutCallback(
      @Context HttpServletRequest request, @Context HttpServletResponse response) {
    String baseURL = authController.getBaseURL(request);

    Arrays.stream(request.getCookies())
            .filter(e -> e.getName().equals("jwt"))
            .forEach(e -> {
              e.setMaxAge(0);
              response.addCookie(e);
            });

    try {
      return Response.seeOther(new URI(baseURL)).build();
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Route to handle redirect from auth0 login
   *
   * @param request request
   * @param response response
   * @return 303 redirect to base url
   * @throws URISyntaxException URISyntaxException
   * @throws IdentityVerificationException IdentityVerificationException
   */
  @GET
  @Path("/callback")
  public Response callback(
      @Context HttpServletRequest request, @Context HttpServletResponse response)
      throws URISyntaxException, IdentityVerificationException {

    // extract email from given idToken
    Tokens tokens =
        authenticationController.handle(new JavaxRequest(request), new JavaxResponse(response));
    AuthController authController = new AuthController();
    String idToken = tokens.getIdToken(); // JWT mit User-Claims
    String email = authController.extractEmail(idToken);
    String baseURL = authController.getBaseURL(request);

    try {
      User emailConfirmed = repository.findByEmail(email);

      // create entry if it doesn't exist (user is new)
      if (emailConfirmed == null) {
        emailConfirmed = new User();
        emailConfirmed.setEmail(email);
        emailConfirmed.setConfirmKey(UUID.randomUUID().toString());
        repository.save(emailConfirmed);

        // send email with generated confirm key allowing to identify the correct entry on
        // confirmation link
        new AuthMailSender().sendMail(emailConfirmed, baseURL);
      }

      // store cookie with jwt in request to retrieve it from everywhere
      response.addCookie(new Cookie("jwt", idToken));
    } catch (Exception e) {
      Logger logger = Logger.getLogger(ApiController.class.getName());
      logger.log(Level.SEVERE, e.getMessage());
    }

    return Response.seeOther(new URI(baseURL)).build();
  }

  /**
   * Route for confirmation link setting confirmed = true on corresponding entry
   *
   * @param confirmKey: to identify databae entry
   * @return rendered confirmation page
   */
  @GET
  @Path("/confirm-email/{confirm-key}")
  public String confirmEmail(@PathParam("confirm-key") String confirmKey) {
    User emailConfirmed = repository.findByConfirmedKey(confirmKey);
    emailConfirmed.setConfirmed(true);
    repository.merge(emailConfirmed);
    return "emailConfirmed.xhtml";
  }

  /**
   * Render shopping cart page
   *
   * @return rendered shopping cart page
   */
  @GET
  @Path("/shopping-cart")
  public String shoppingCart() {
    return "shoppingCart.xhtml";
  }

  /**
   * Add item to shopping-cart
   *
   * @param skuStr: article sku
   * @param amountStr: amount to add
   * @return re-render detail page
   */
  @POST
  @Path("/add-to-shopping-cart")
  @Transactional
  public Response addToShoppingCart(
      @FormParam("sku") String skuStr, @FormParam("amount") String amountStr) {

    try {
      long sku = Long.parseLong(skuStr);
      long amount = Long.parseLong(amountStr);
      String email = new AuthController().extractEmail(request);

      if (email == null) {
        return Response.status(Response.Status.UNAUTHORIZED).build();
      }

      ShoppingCart shoppingCart =
          shoppingCartController.getOrUpdateShoppingCart(sku, amount, email);
      shoppingCartRepository.merge(shoppingCart);

      return Response.seeOther(
              new URI(
                  String.format("%s/detail?sku=%s", authController.getBaseURL(request), skuStr)))
          .build();
    } catch (Exception ignored) {
    }

    return Response.status(Status.BAD_REQUEST).build();
  }

  /**
   * Change amount in shopping cart
   *
   * @param amountStr: amount to be set
   * @param skuStr: article sku
   * @return 200 when finished
   */
  @POST
  @Path("/shopping-cart/change-amount/{sku}/{amount}")
  public Response changeAmount(
      @PathParam("amount") String amountStr, @PathParam("sku") String skuStr) {
    long sku = Long.parseLong(skuStr);
    String email = new AuthController().extractEmail(request);

    if (email == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    ShoppingCart shoppingCart = shoppingCartRepository.findBySkuAndEmail(sku, email);
    shoppingCart.setAmount(
        shoppingCartController.getMaxAmount(Long.parseLong(amountStr), shoppingCart));
    shoppingCartRepository.merge(shoppingCart);

    return Response.ok().build();
  }

  /**
   * Delete entry from shopping cart
   *
   * @param entryUuidStr uuid of shopping cart database entry
   * @return 200 when finished
   */
  @DELETE
  @Path("/shopping-cart/delete-entry/{uuid}")
  public Response deleteEntry(@PathParam("uuid") String entryUuidStr) {
    String email = new AuthController().extractEmail(request);

    if (email == null) {
      return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    shoppingCartRepository.deleteByUuid(entryUuidStr);

    return Response.ok().build();
  }
}
