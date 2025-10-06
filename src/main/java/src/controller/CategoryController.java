package src.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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

  public Map<String, UUID> getCategories() {
    return repository.getCategories().stream()
        .collect(Collectors.toMap(Category::getCategoryName, Category::getUuid));
  }

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
