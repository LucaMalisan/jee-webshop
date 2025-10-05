package src.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@SuppressWarnings("unused")
@Table(name = "article")
public class Article implements Serializable {

  @Id
  @Column(name = "sku", updatable = false, nullable = false)
  private long sku;

  @Column(name = "title")
  private String title;

  @Column(name = "description")
  private String description;

  @Column(name = "sellingPrice")
  private Double sellingPrice;

  @Column(name = "listPrice")
  private Double listPrice;

  @Column(name = "available")
  private Boolean available;

  @Column(name = "category_name")
  private UUID subcategoryUuid;

  @ManyToOne
  @JoinColumn(name = "category_name", insertable = false, updatable = false)
  private Subcategory subcategory;

  @OneToMany(mappedBy = "article")
  private List<ArticleImage> imageList;

  public String getDiscountPercent() {
    if (this.getListPrice() == null) {
      return null;
    }

    return Math.round((100 - (this.getSellingPrice() / this.getListPrice()) * 100)) + "";
  }

  public String formatPrice() {
    if (this.getListPrice() != null) {
      return String.format("%.2f was %.2f CHF", this.getSellingPrice(), this.getListPrice());
    }
    return String.format("%.2f CHF", this.getSellingPrice());
  }

  public String getPrimaryImageURL() {
    Optional<ArticleImage> optImage =
        this.getImageList().stream().filter(e -> e.getPosition() == 1).findAny();
    return optImage.map(ArticleImage::getImageURL).orElse("");
  }
}
