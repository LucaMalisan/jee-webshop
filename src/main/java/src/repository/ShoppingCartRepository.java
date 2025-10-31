package src.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.util.List;
import src.model.ShoppingCart;

@ApplicationScoped
public class ShoppingCartRepository {

  @PersistenceContext EntityManager entitymanager;

  public List<ShoppingCart> getShoppingCartEntries(String email) {
    return entitymanager
            .createQuery(
                // keep fixed order for usability
                "SELECT c FROM ShoppingCart c WHERE c.email = ?1 ORDER BY c.uuid")
            .setParameter(1, email)
            .getResultList();
  }

  public double getTotalPrice(String email) {
    return (Double) entitymanager
        .createQuery("SELECT CAST(sum(c.amount * c.article.sellingPrice) as float) FROM ShoppingCart c WHERE c.email = ?1")
        .setParameter(1, email)
        .getSingleResult();
  }

  public double getTotalDiscount(String email) {
    return (Double) entitymanager
            .createQuery("SELECT CAST(sum(c.amount * (c.article.listPrice -  c.article.sellingPrice)) as float) FROM ShoppingCart c WHERE c.email = ?1")
            .setParameter(1, email)
            .getSingleResult();
  }

  @Transactional
  public void save(ShoppingCart shoppingCart) {
    entitymanager.persist(shoppingCart);
  }

  @Transactional
  public void merge(ShoppingCart shoppingCart) {
    entitymanager.merge(shoppingCart);
  }

  @Transactional
  public void deleteByUuid(String uuidStr) {
    entitymanager
        .createQuery("DELETE FROM ShoppingCart c WHERE c.uuid = ?1")
        .setParameter(1, uuidStr)
        .executeUpdate();
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

  public ShoppingCart findByUuid(String uuid) {
    try {
      return (ShoppingCart)
          entitymanager
              .createQuery("SELECT c FROM ShoppingCart c WHERE c.uuid = ?1")
              .setParameter(1, uuid)
              .getSingleResult();
    } catch (Exception ex) {
      return null;
    }
  }
}
