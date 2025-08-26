package src.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "article_image")
public class ArticleImage {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "uuid", updatable = false, nullable = false)
  private UUID uuid;

  @Column(name = "articleUuid")
  private UUID articleUuid;

  @Column(name = "imageURL")
  private String imageURL;

  @Column(name = "\"order\"")
  private Integer order;
}
