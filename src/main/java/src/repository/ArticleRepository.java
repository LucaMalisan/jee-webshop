package src.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

import src.model.Article;

@ApplicationScoped
public class ArticleRepository {

  @PersistenceContext EntityManager entitymanager;

  public List<Article> getArticles() {
    TypedQuery<Article> query = entitymanager.createQuery("SELECT a FROM Article a", Article.class);
    return query.getResultList();
  }
}
