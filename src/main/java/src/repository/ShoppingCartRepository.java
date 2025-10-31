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

  /**
   * Find all shopping cart entries belonging to user
   *
   * @param email user email
   * @return shopping cart entries
   */
  public List<ShoppingCart> getShoppingCartEntries(String email) {
    return entitymanager
        .createQuery(
            // keep fixed order for usability
            "SELECT c FROM ShoppingCart c WHERE c.email = ?1 ORDER BY c.uuid")
        .setParameter(1, email)
        .getResultList();
  }

  /**
   * Get sum of all prices of articles in users shopping cart
   *
   * @param email user email
   * @return total price
   */
  public double getTotalPrice(String email) {
    // (quantity) * (price per quantity) of all articles added up
    return (Double)
        entitymanager
            .createQuery(
                "SELECT CAST(sum(c.amount * c.article.sellingPrice) as float) FROM ShoppingCart c WHERE c.email = ?1")
            .setParameter(1, email)
            .getSingleResult();
  }

  /**
   * Get sum of all discounts of articles in users shopping cart
   *
   * @param email user email
   * @return total discount
   */
  public double getTotalDiscount(String email) {
    // (quantity) * (difference between list price and selling price) of all articles added up
    return (Double)
        entitymanager
            .createQuery(
                "SELECT CAST(sum(c.amount * (c.article.listPrice -  c.article.sellingPrice)) as float) FROM ShoppingCart c WHERE c.email = ?1")
            .setParameter(1, email)
            .getSingleResult();
  }

  /**
   * Save entity
   *
   * @param shoppingCart entity
   */
  @Transactional
  public void save(ShoppingCart shoppingCart) {
    entitymanager.persist(shoppingCart);
  }

  /**
   * Update entity
   *
   * @param shoppingCart entity
   */
  @Transactional
  public void merge(ShoppingCart shoppingCart) {
    entitymanager.merge(shoppingCart);
  }

  /**
   * Delete entity
   *
   * @param uuidStr uuid of entity
   */
  @Transactional
  public void deleteByUuid(String uuidStr) {
    entitymanager
        .createQuery("DELETE FROM ShoppingCart c WHERE c.uuid = ?1")
        .setParameter(1, uuidStr)
        .executeUpdate();
  }

  /**
   * Find specific entry with article and user email
   *
   * @param sku article sku
   * @param email user email
   * @return shopping cart entry
   */
  public ShoppingCart findBySkuAndEmail(Long sku, String email) {
    try {
      return (ShoppingCart)
          entitymanager
              .createQuery("SELECT c FROM ShoppingCart c WHERE c.email = ?1 and c.articleSku = ?2")
              .setParameter(1, email)
              .setParameter(2, sku)
              .getSingleResult();
    } catch (Exception ex) {
      // none or several results found
      return null;
    }
  }
}
