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

  private static final double VAT = 0.07;

  public String getPriceExclVat(HttpServletRequest request) {
    String email = new AuthController().extractEmail(request);
    return String.format("%.2f CHF", shoppingCartRepository.getTotalPrice(email) * (1 - VAT));
  }

  public String getVat(HttpServletRequest request) {
    String email = new AuthController().extractEmail(request);
    return String.format("%.2f CHF", shoppingCartRepository.getTotalPrice(email) * (VAT));
  }

  public String getTotal(HttpServletRequest request) {
    String email = new AuthController().extractEmail(request);
    return String.format("%.2f CHF", shoppingCartRepository.getTotalPrice(email));
  }

  public String getDiscount(HttpServletRequest request) {
    String email = new AuthController().extractEmail(request);
    return String.format("%.2f CHF", shoppingCartRepository.getTotalDiscount(email));
  }
}
