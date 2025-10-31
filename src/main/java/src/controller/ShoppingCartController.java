package src.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import org.eclipse.krazo.lifecycle.RequestLifecycle;
import src.model.Category;
import src.model.ShoppingCart;
import src.model.Subcategory;
import src.repository.CategoryRepository;
import src.repository.ShoppingCartRepository;
import src.utils.StringUtils;

@Named
@RequestScoped
@SuppressWarnings("unused")
@NoArgsConstructor
public class ShoppingCartController {

  @Inject private ShoppingCartRepository repository;
  @Inject private ShoppingCartRepository shoppingCartRepository;
  @Named @Inject private AuthController authController;

  private static final double VAT = 0.07;

  /**
   * Retrieve all shopping cart entires
   *
   * @param request request
   * @return list of shopping cart entries
   */
  public List<ShoppingCart> getShoppingCartEntries(HttpServletRequest request) {
    String email = authController.extractEmail(request);
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
