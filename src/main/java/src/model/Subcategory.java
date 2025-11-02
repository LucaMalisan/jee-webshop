package src.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "subcategory")
public class Subcategory implements Serializable {

  @Id
  @Column(name = "uuid", updatable = false, nullable = false)
  private String uuid;

  @Column(name = "root_category_uuid")
  private String rootCategoryUuid;

  @Column(name = "category_name")
  private String categoryName;

  @OneToMany(mappedBy = "subcategory")
  private List<Article> articleList;

  @ManyToOne(cascade =  {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "root_category_uuid", insertable = false, updatable = false)
  private Category rootCategory;
}
