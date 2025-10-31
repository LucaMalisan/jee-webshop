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
import src.utils.StringUtils;

@Named
@RequestScoped
@SuppressWarnings("unused")
@NoArgsConstructor
public class CategoryController {

  @Inject private CategoryRepository repository;
  @Inject private RequestLifecycle requestLifecycle;

  /**
   * Find all available categories
   *
   * @return all categories
   */
  public Map<String, String> getCategories() {
    return repository.getCategories().stream()
        .collect(Collectors.toMap(Category::getCategoryName, Category::getUuid));
  }

  /**
   * Find category model for given category or subcategory uuid
   *
   * @param request: request
   * @return category model
   */
  public Category getSelectedMainCategory(HttpServletRequest request) {
    String subcategoryUuid = request.getParameter("subcategoryUuid");
    String categoryUuid = request.getParameter("categoryUuid");

    if (!StringUtils.isEmpty(subcategoryUuid)) {
      return repository.findSubcategoryByUuid(subcategoryUuid).getRootCategory();
    } else if (!StringUtils.isEmpty(categoryUuid)) {
      return repository.findByUuid(categoryUuid);
    } else {
      return null;
    }
  }

  /**
   * Find subcategory for given uuid, else find all subcategories for given main category uuid
   *
   * @param request: request
   * @return list of subcategories
   */
  public List<Subcategory> getSubcategories(HttpServletRequest request) {
    String subcategoryUuid = request.getParameter("subcategoryUuid");

    // if a subcategory is selected, only show this one
    if (!StringUtils.isEmpty(subcategoryUuid)) {
      return Collections.singletonList(repository.findSubcategoryByUuid(subcategoryUuid));
    }

    // only parent category is selected, show all corresponding subcategories
    String categoryUuid = request.getParameter("categoryUuid");
    return repository.getSubcategoriesByRootCategoryUuid(categoryUuid);
  }
}
