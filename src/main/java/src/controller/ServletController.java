package src.controller;

import com.auth0.AuthenticationController;
import com.auth0.IdentityVerificationException;
import com.auth0.Tokens;
import io.github.cromat.JavaxRequest;
import io.github.cromat.JavaxResponse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.stream.JsonParser;
import jakarta.mvc.Controller;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.logging.Logger;
import javax.transaction.Transactional;
import lombok.extern.java.Log;
import src.auth.Auth0AuthenticationConfig;
import src.auth.AuthMailSender;
import src.model.Article;
import src.model.ShoppingCart;
import src.model.User;
import src.repository.ArticleRepository;
import src.repository.ShoppingCartRepository;
import src.repository.UserRepository;

@Controller
@RequestScoped
@Log
@Path("/")
public class ServletController {

  @Inject private Auth0AuthenticationConfig config;

  @Inject private AuthenticationController authenticationController;

  @Inject private UserRepository repository;

  @Inject private ShoppingCartRepository shoppingCartRepository;

  @Context private HttpServletRequest request;
  @Named @Inject private AuthController authController;
  @Inject private ArticleRepository articleRepository;

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

    return Response.seeOther(new URI(authURL)).build();
  }

  @GET
  @Path("/callback")
  public Response callback(
      @Context HttpServletRequest request, @Context HttpServletResponse response)
      throws URISyntaxException, IdentityVerificationException {

    Tokens tokens =
        authenticationController.handle(new JavaxRequest(request), new JavaxResponse(response));
    AuthController authController = new AuthController();
    String idToken = tokens.getIdToken(); // JWT mit User-Claims
    String email = authController.extractEmail(idToken);
    String baseURL = authController.getBaseURL(request);

    try {
      User emailConfirmed = repository.findByEmail(email);

      if (emailConfirmed == null) {
        // create entry if user is new
        emailConfirmed = new User();
        emailConfirmed.setEmail(email);
        emailConfirmed.setConfirmKey(UUID.randomUUID().toString());
        repository.save(emailConfirmed);

        // send email with generated confirm key
        new AuthMailSender().sendMail(emailConfirmed, baseURL);
      }

      response.addCookie(new Cookie("jwt", idToken));
    } catch (Exception e) {
      log.severe(e.getMessage());
    }

    return Response.seeOther(new URI(baseURL)).build();
  }

  @GET
  @Path("/confirm-email/{confirm-key}")
  public String confirmEmail(@PathParam("confirm-key") String confirmKey) {
    User emailConfirmed = repository.findByConfirmedKey(confirmKey);
    emailConfirmed.setConfirmed(true);
    repository.merge(emailConfirmed);
    return "emailConfirmed.xhtml";
  }

  @GET
  @Path("/shopping-cart")
  public String shoppingCart() {
    return "shoppingCart.xhtml";
  }

  @POST
  @Path("/add-to-shopping-cart")
  @Transactional
  public Response addToShoppingCart(
      @FormParam("sku") String skuStr, @FormParam("amount") String amountStr)
      throws URISyntaxException {

    try {
      long sku = Long.parseLong(skuStr);
      String email = new AuthController().extractEmail(request);
      Article article = articleRepository.findBySku(sku);
      long amount = Math.min(Long.parseLong(amountStr), article.getStock());
      ShoppingCart shoppingCart = shoppingCartRepository.findBySkuAndEmail(sku, email);

      if (shoppingCart == null) {
        shoppingCart = new ShoppingCart();
        shoppingCart.setUuid(UUID.randomUUID().toString());
        shoppingCart.setArticleSku(sku);
        shoppingCart.setEmail(email);
        shoppingCart.setAmount(amount);
        shoppingCartRepository.save(shoppingCart);
      } else {
        shoppingCart.setAmount(shoppingCart.getAmount() + amount);
        shoppingCartRepository.merge(shoppingCart);
      }
    } catch (Exception ignored) {
    }

    return Response.seeOther(
            new URI(String.format("%s/detail?sku=%s", authController.getBaseURL(request), skuStr)))
        .build();
  }

  @POST
  @Path("/shopping-cart/change-amount/{sku}/{amount}")
  public Response changeAmount(
      @PathParam("amount") String amountStr, @PathParam("sku") String skuStr) {
    long sku = Long.parseLong(skuStr);
    String email = new AuthController().extractEmail(request);
    ShoppingCart shoppingCart = shoppingCartRepository.findBySkuAndEmail(sku, email);

    long amount = Math.min(Long.parseLong(amountStr), shoppingCart.getArticle().getStock());
    shoppingCart.setAmount(amount);
    shoppingCartRepository.merge(shoppingCart);

    return Response.ok().build();
  }

  @DELETE
  @Path("/shopping-cart/delete-entry/{uuid}")
  public Response deleteEntry(@PathParam("uuid") String entryUuidStr) {
    shoppingCartRepository.deleteByUuid(entryUuidStr);

    return Response.ok().build();
  }
}
