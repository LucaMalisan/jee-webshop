package src.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import src.model.User;

@ApplicationScoped
public class UserRepository {

  @PersistenceContext EntityManager entitymanager;

  public User findByEmail(String email) {
    TypedQuery<User> query =
        entitymanager.createQuery("SELECT a FROM User a WHERE a.email = ?1", User.class);
    query.setParameter(1, email);

    try {
      return query.getSingleResult();
    } catch (Exception e) {
      return null;
    }
  }

  public User findByConfirmedKey(String confirmKey) {
    TypedQuery<User> query =
        entitymanager.createQuery("SELECT a FROM User a WHERE a.confirmKey = ?1", User.class);
    query.setParameter(1, confirmKey);

    try {
      return query.getSingleResult();
    } catch (Exception e) {
      return null;
    }
  }

  @Transactional
  public void save(User user) {
    entitymanager.persist(user);
  }

  @Transactional
  public void merge(User user) {
    entitymanager.merge(user);
  }
}
