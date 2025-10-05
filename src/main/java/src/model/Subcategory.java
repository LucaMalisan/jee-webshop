package src.model;

import jakarta.persistence.*;
import java.io.Serializable;
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
  private UUID rootCategory;

  @Column(name = "category_name")
  private String categoryName;
}
