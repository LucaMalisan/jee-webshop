package src.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "subcategory")
public class Subcategory implements Serializable {

  @Id
  @Column(name = "uuid", updatable = false, nullable = false)
  private UUID uuid;

  @Column(name = "root_category")
  private UUID rootCategoryUuid;

  @Column(name = "category_name")
  private String categoryName;

  @OneToMany(mappedBy = "subcategory")
  private List<Article> articleList;

  @ManyToOne
  @JoinColumn(name = "root_category", insertable = false, updatable = false)
  private Category rootCategory;
}
