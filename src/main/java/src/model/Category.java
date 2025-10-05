package src.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "category")
public class Category implements Serializable {

  @Id
  @Column(name = "uuid", updatable = false, nullable = false)
  private UUID uuid;

  @Column(name = "root_category_name")
  private String categoryName;
}
