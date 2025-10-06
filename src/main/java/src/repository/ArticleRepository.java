package src.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

import java.util.UUID;
import src.model.Article;
import src.utils.StringUtils;

@ApplicationScoped
public class ArticleRepository {

  @PersistenceContext EntityManager entitymanager;

  public List<Article> getArticles(String categoryUuidStr, String subcategoryUuidStr) {
    TypedQuery<Article> query;

    if (StringUtils.isEmpty(categoryUuidStr) && StringUtils.isEmpty(subcategoryUuidStr)) {
      query = entitymanager.createQuery("SELECT a FROM Article a", Article.class);
    } else if (StringUtils.isEmpty(categoryUuidStr)) {
      UUID subcategoryUuid = UUID.fromString(subcategoryUuidStr);
      query =
          entitymanager.createQuery(
              "SELECT a FROM Article a WHERE a.subcategoryUuid = ?1", Article.class);
      query.setParameter(1, subcategoryUuid);
    } else {
      UUID categoryUuid = UUID.fromString(categoryUuidStr);
      query =
          entitymanager.createQuery(
              "SELECT a FROM Article a WHERE a.subcategory.rootCategoryUuid = ?1", Article.class);
      query.setParameter(1, categoryUuid);
    }

    return query.getResultList();
  }
}
