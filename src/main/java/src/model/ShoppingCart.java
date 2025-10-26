package src.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "shopping_cart")
public class ShoppingCart {

  @Id
  @Column(name = "uuid", updatable = false, nullable = false)
  private String uuid;

  @Column(name = "email")
  private String email;

  @Column(name = "article_sku")
  private long articleSku;

  @Column(name = "amount")
  private long amount;

  @ManyToOne
  @JoinColumn(name = "email", insertable = false, updatable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "article_sku", insertable = false, updatable = false)
  private Article article;
}
