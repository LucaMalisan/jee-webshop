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

  /**
   * Find all articles
   *
   * @param categoryUuidStr main category uuid
   * @param subcategoryUuidStr subcategory uuid
   * @param query search term
   * @return list of matching articles
   */
  public List<Article> getArticles(
      String categoryUuidStr, String subcategoryUuidStr, String query) {
    CriteriaBuilder cb = entitymanager.getCriteriaBuilder();
    CriteriaQuery<Article> cq = cb.createQuery(Article.class);
    Root<Article> article = cq.from(Article.class);
    List<Predicate> predicates = new ArrayList<>();

    cq.select(article);

    // if main category uuid is provided, join via subcategory to main category
    if (!StringUtils.isEmpty(categoryUuidStr)) {
      Join<Article, Subcategory> subcategoryJoin = article.join("subcategory");
      predicates.add(cb.equal(subcategoryJoin.get("rootCategoryUuid"), categoryUuidStr));
    }

    // filter after subcategory uuid if provided
    if (!StringUtils.isEmpty(subcategoryUuidStr)) {
      predicates.add(cb.equal(article.get("subcategoryUuid"), subcategoryUuidStr));
    }

    // full text search after title
    if (!StringUtils.isEmpty(query)) {
      predicates.add(cb.like(cb.lower(article.get("title")), "%" + query.toLowerCase() + "%"));
    }

    // build where clause out of single predicates
    cq = cq.where(predicates.toArray(new Predicate[0]));
    TypedQuery<Article> q = entitymanager.createQuery(cq);

    return q.getResultList();
  }

  /**
   * Find article by sku
   *
   * @param sku article sku
   * @return matching article
   */
  public Article findBySku(long sku) {
    TypedQuery<Article> query =
        entitymanager.createQuery("SELECT a FROM Article a WHERE a.sku = ?1", Article.class);
    query.setParameter("1", sku);

    try {
      return query.getSingleResult();
    } catch (Exception e) {
      // none or several results found
      return null;
    }
  }
}
