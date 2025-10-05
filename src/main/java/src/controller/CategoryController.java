package src.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.eclipse.krazo.lifecycle.RequestLifecycle;
import src.model.Article;
import src.model.Category;
import src.repository.ArticleRepository;
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
    return Optional.ofNullable(repository.findByUuid(categoryUuid))
        .map(Category::getCategoryName)
        .orElse("");
  }

  public Map<String, UUID> getSubCategories(HttpServletRequest request) {
    String categoryUuid = request.getParameter("categoryUuid");
    return repository.getSubCategoriesByRootCategory().stream()
            .collect(Collectors.toMap(Category::getCategoryName, Category::getUuid));
  }
}
