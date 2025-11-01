package src.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import src.model.ShoppingCart;
import src.repository.ShoppingCartRepository;

@Getter
@Named
@RequestScoped
@SuppressWarnings("unused")
@NoArgsConstructor
public class ShoppingCartController {

  @Inject private ShoppingCartRepository shoppingCartRepository;
  @Named @Inject private AuthController authController;

  private static final double VAT = 0.07;

  public ShoppingCartController(ShoppingCartRepository shoppingCartRepository, AuthController authController) {
    this.shoppingCartRepository = shoppingCartRepository;
    this.authController = authController;
  }

  /**
   * Retrieve all shopping cart entires
   *
   * @param request request
   * @return list of shopping cart entries
   */
  public List<ShoppingCart> getShoppingCartEntries(HttpServletRequest request) {
    String email = authController.extractEmail(request);

    if(email == null) {
      return new ArrayList<>();
    }

    return shoppingCartRepository.getShoppingCartEntries(email);
  }

  /**
   * Get price without VAT
   *
   * @param request requset
   * @return price without VAT
   */
  public String getPriceExclVat(HttpServletRequest request) {
    String email = new AuthController().extractEmail(request);
    return String.format("%.2f CHF", shoppingCartRepository.getTotalPrice(email) * (1 - VAT));
  }

  /**
   * Get VAT amount of price
   *
   * @param request request
   * @return VAT amount of price
   */
  public String getVat(HttpServletRequest request) {
    String email = new AuthController().extractEmail(request);
    return String.format("%.2f CHF", shoppingCartRepository.getTotalPrice(email) * (VAT));
  }

  /**
   * Get sum of price of all articles
   *
   * @param request request
   * @return sum of price of all articles
   */
  public String getTotal(HttpServletRequest request) {
    String email = new AuthController().extractEmail(request);
    return String.format("%.2f CHF", shoppingCartRepository.getTotalPrice(email));
  }

  /**
   * Get sum of discount of all articles
   *
   * @param request request
   * @return sum of discount of all articles
   */
  public String getDiscount(HttpServletRequest request) {
    String email = new AuthController().extractEmail(request);
    return String.format("%.2f CHF", shoppingCartRepository.getTotalDiscount(email));
  }
}
