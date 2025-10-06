package src.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import src.model.Category;
import src.model.Subcategory;

@ApplicationScoped
public class CategoryRepository {

  @PersistenceContext EntityManager entitymanager;

  public List<Category> getCategories() {
    TypedQuery<Category> query =
        entitymanager.createQuery("SELECT a FROM Category a", Category.class);
    return query.getResultList();
  }

  public Category findByUuid(String uuidStr) {
    UUID uuid;

    try {
      uuid = UUID.fromString(uuidStr);
    } catch (Exception e) {
      return null;
    }

    TypedQuery<Category> query =
        entitymanager.createQuery("SELECT a FROM Category a WHERE a.uuid = ?1", Category.class);
    query.setParameter(1, uuid);
    return query.getSingleResult();
  }

  public Subcategory findSubcategoryByUuid(String uuidStr) {
    UUID uuid;

    try {
      uuid = UUID.fromString(uuidStr);
    } catch (Exception e) {
      return null;
    }

    TypedQuery<Subcategory> query =
            entitymanager.createQuery("SELECT a FROM Subcategory a WHERE a.uuid = ?1", Subcategory.class);
    query.setParameter(1, uuid);
    return query.getSingleResult();
  }

  public List<Subcategory> getSubcategoriesByRootCategoryUuid(String uuidStr) {
    UUID uuid;

    try {
      uuid = UUID.fromString(uuidStr);
    } catch (Exception e) {
      return Collections.emptyList();
    }

    TypedQuery<Subcategory> query =
        entitymanager.createQuery(
            "SELECT a FROM Subcategory a WHERE a.rootCategoryUuid = ?1", Subcategory.class);
    query.setParameter(1, uuid);
    return query.getResultList();
  }
}
