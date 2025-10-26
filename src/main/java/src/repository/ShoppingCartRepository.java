package src.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import src.controller.ArticleController;
import src.model.Article;
import src.model.ShoppingCart;

@ApplicationScoped
public class ShoppingCartRepository {

  @PersistenceContext EntityManager entitymanager;

  @Inject ArticleRepository articleRepository;

  public List<Article> getShoppingCartArticles(String email) {
    return (List<Article>)
        entitymanager
            .createQuery("SELECT c FROM ShoppingCart c WHERE c.email = ?1")
            .setParameter(1, email)
            .getResultList()
            .stream()
            .map(e -> articleRepository.findBySku(((ShoppingCart) e).getArticleSku()))
            .collect(Collectors.toList());
  }

  @Transactional
  public void save(ShoppingCart shoppingCart) {
    entitymanager.persist(shoppingCart);
  }

  @Transactional
  public void merge(ShoppingCart shoppingCart) {
    entitymanager.merge(shoppingCart);
  }

  public ShoppingCart findBySkuAndEmail(Long sku, String email) {
    try {
      return (ShoppingCart)
          entitymanager
              .createQuery("SELECT c FROM ShoppingCart c WHERE c.email = ?1 and c.articleSku = ?2")
              .setParameter(1, email)
              .setParameter(2, sku)
              .getSingleResult();
    } catch (Exception ex) {
      return null;
    }
  }
}
