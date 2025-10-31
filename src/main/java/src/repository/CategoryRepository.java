package src.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import src.model.Category;
import src.model.Subcategory;

@ApplicationScoped
public class CategoryRepository {

  @PersistenceContext EntityManager entitymanager;

  /**
   * Find all categories
   *
   * @return list of all categories
   */
  public List<Category> getCategories() {
    TypedQuery<Category> query =
        entitymanager.createQuery("SELECT a FROM Category a", Category.class);
    return query.getResultList();
  }

  /**
   * Find category by uuid
   *
   * @return matching category
   */
  public Category findByUuid(String uuidStr) {
    TypedQuery<Category> query =
        entitymanager.createQuery("SELECT a FROM Category a WHERE a.uuid = ?1", Category.class);
    query.setParameter(1, uuidStr);

    try {
      return query.getSingleResult();
    } catch (Exception e) {
      // none or several results found
      return null;
    }
  }

  /**
   * Find subcategory by uuid
   *
   * @param uuidStr subcategory uuid
   * @return matching subcategory
   */
  public Subcategory findSubcategoryByUuid(String uuidStr) {
    TypedQuery<Subcategory> query =
        entitymanager.createQuery(
            "SELECT a FROM Subcategory a WHERE a.uuid = ?1", Subcategory.class);
    query.setParameter(1, uuidStr);

    try {
      return query.getSingleResult();
    } catch (Exception e) {
      // none or several results found
      return null;
    }
  }

  /**
   * Find subcategories belonging to main category uuid
   *
   * @param uuidStr main category uuid
   * @return matching subcategories
   */
  public List<Subcategory> getSubcategoriesByRootCategoryUuid(String uuidStr) {
    TypedQuery<Subcategory> query =
        entitymanager.createQuery(
            "SELECT a FROM Subcategory a WHERE a.rootCategoryUuid = ?1", Subcategory.class);
    query.setParameter(1, uuidStr);
    return query.getResultList();
  }
}
