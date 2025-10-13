package src.model;

import jakarta.persistence.*;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "category")
public class Category implements Serializable {

  @Id
  @Column(name = "uuid", updatable = false, nullable = false)
  private String uuid;

  @Column(name = "category_name")
  private String categoryName;
}
