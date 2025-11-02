package src.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "article_image")
public class ArticleImage {

  @Id
  @Column(name = "uuid", updatable = false, nullable = false)
  private String uuid;

  @Column(name = "article_sku")
  private long articleSku;

  @Column(name = "image_uRL")
  private String imageURL;

  @Column(name = "position")
  private Integer position;

  @ManyToOne()
  @JoinColumn(name = "article_sku", insertable = false, updatable = false)
  private Article article;
}
