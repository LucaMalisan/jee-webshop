package src.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import src.model.Category;
import src.model.Subcategory;
import src.model.UserEmailConfirmed;

@ApplicationScoped
public class UserEmailConfirmedRepository {

  @PersistenceContext EntityManager entitymanager;

  public UserEmailConfirmed findByEmail(String email) {
    entitymanager.clear();
    TypedQuery<UserEmailConfirmed> query =
        entitymanager.createQuery(
            "SELECT a FROM UserEmailConfirmed a WHERE a.email = ?1", UserEmailConfirmed.class);
    query.setParameter(1, email);

    try {
      return query.getSingleResult();
    } catch (Exception e) {
      return null;
    }
  }

  public UserEmailConfirmed findByConfirmedKey(String confirmKey) {
    entitymanager.clear();

    TypedQuery<UserEmailConfirmed> query =
        entitymanager.createQuery(
            "SELECT a FROM UserEmailConfirmed a WHERE a.confirmKey = ?1", UserEmailConfirmed.class);
    query.setParameter(1, confirmKey);

    try {
      return query.getSingleResult();
    } catch (Exception e) {
      return null;
    }
  }

  @Transactional
  public void save(UserEmailConfirmed userEmailConfirmed) {
    entitymanager.persist(userEmailConfirmed);
  }

  @Transactional
  public void merge(UserEmailConfirmed userEmailConfirmed) {
    entitymanager.merge(userEmailConfirmed);
  }
}
