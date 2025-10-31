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

  /**
   * find user by login email
   *
   * @param email email
   * @return matching user
   */
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

  /**
   * find user by confirmation key
   *
   * @param confirmKey confirmation key
   * @return matching user
   */
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

  /**
   * Save model
   *
   * @param user model
   */
  @Transactional
  public void save(User user) {
    entitymanager.persist(user);
  }

  /**
   * Update model
   *
   * @param user model
   */
  @Transactional
  public void merge(User user) {
    entitymanager.merge(user);
  }
}
