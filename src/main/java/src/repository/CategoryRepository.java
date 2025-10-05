package src.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import src.model.Category;

@ApplicationScoped
public class CategoryRepository {

  @PersistenceContext EntityManager entitymanager;

  public List<Category> getCategories() {
    TypedQuery<Category> query =
        entitymanager.createQuery("SELECT a FROM Category a", Category.class);
    return query.getResultList();
  }

  public Category findByUuid(String uuid) {
    TypedQuery<Category> query =
        entitymanager.createQuery("SELECT a FROM Category a WHERE a.uuid = ?1", Category.class);
    query.setParameter(1, uuid);
    return query.getSingleResult();
  }
}
