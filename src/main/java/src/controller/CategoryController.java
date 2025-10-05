package src.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.krazo.lifecycle.RequestLifecycle;
import src.model.Category;
import src.model.Subcategory;
import src.repository.CategoryRepository;

@Named
@RequestScoped
@SuppressWarnings("unused")
public class CategoryController {

  @Inject private CategoryRepository repository;
  @Inject private RequestLifecycle requestLifecycle;

  public Map<String, UUID> getCategories() {
    return repository.getCategories().stream()
        .collect(Collectors.toMap(Category::getCategoryName, Category::getUuid));
  }

  public String getSelectedMainCategory(HttpServletRequest request) {
    String categoryUuid = request.getParameter("categoryUuid");

    try {
      return repository.findByUuid(categoryUuid).getCategoryName();
    } catch (Exception e) {
      return "";
    }
  }

  public List<Subcategory> getSubcategories(HttpServletRequest request) {
    String categoryUuid = request.getParameter("categoryUuid");
    return repository.getSubcategoriesByRootCategoryUuid(categoryUuid);
  }
}
