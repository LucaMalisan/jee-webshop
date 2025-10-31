package src.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import src.model.ShoppingCart;

@ApplicationScoped
public class ShoppingCartRepository {

  @PersistenceContext EntityManager entitymanager;

  public List<ShoppingCart> getShoppingCartEntries(String email) {
    return (List<ShoppingCart>)
        entitymanager
            .createQuery(
                "SELECT c FROM ShoppingCart c WHERE c.email = ?1 ORDER BY c.uuid") // keep fixed
                                                                                   // order for
                                                                                   // usability
            .setParameter(1, email)
            .getResultList();
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
