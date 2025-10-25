package src.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

import src.model.Article;
import src.model.Subcategory;
import src.utils.StringUtils;

@ApplicationScoped
public class ArticleRepository {

  @PersistenceContext EntityManager entitymanager;

  public List<Article> getArticles(
      String categoryUuidStr, String subcategoryUuidStr, String query) {
    CriteriaBuilder cb = entitymanager.getCriteriaBuilder();
    CriteriaQuery<Article> cq = cb.createQuery(Article.class);
    Root<Article> article = cq.from(Article.class);
    List<Predicate> predicates = new ArrayList<>();

    cq.select(article);

    if (!StringUtils.isEmpty(categoryUuidStr)) {
      Join<Article, Subcategory> subcategoryJoin = article.join("subcategory");
      predicates.add(cb.equal(subcategoryJoin.get("rootCategoryUuid"), categoryUuidStr));
    }

    if (!StringUtils.isEmpty(subcategoryUuidStr)) {
      predicates.add(cb.equal(article.get("subcategoryUuid"), subcategoryUuidStr));
    }

    if (!StringUtils.isEmpty(query)) {
      predicates.add(cb.like(cb.lower(article.get("title")), "%" + query + "%"));
    }

    cq = cq.where(predicates.toArray(new Predicate[0]));
    TypedQuery<Article> q = entitymanager.createQuery(cq);

    return q.getResultList();
  }

  public Article findBySku(long sku) {
    TypedQuery<Article> query =
        entitymanager.createQuery("SELECT a FROM Article a WHERE a.sku = ?1", Article.class);
    query.setParameter("1", sku);

    try {
      return query.getSingleResult();
    } catch (Exception e) {
      return null;
    }
  }
}
