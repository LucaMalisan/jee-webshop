package src.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

import java.util.UUID;
import src.model.Article;

@ApplicationScoped
public class ArticleRepository {

  @PersistenceContext EntityManager entitymanager;

  public List<Article> getArticles(String categoryUuidStr) {
    UUID categoryUuid = UUID.fromString(categoryUuidStr);
    TypedQuery<Article> query =
        entitymanager.createQuery(
            "SELECT a FROM Article a WHERE a.subcategory.rootCategory = ?1", Article.class);
    query.setParameter(1, categoryUuid);
    return query.getResultList();
  }
}
