package src.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
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

  //price with discount
  @Column(name = "sellingPrice")
  private Double sellingPrice;

  //price without discount
  @Column(name = "listPrice")
  private Double listPrice;

  @Column(name = "available")
  private Boolean available;

  @Column(name = "stock")
  private int stock;

  @Column(name = "subcategory_uuid")
  private String subcategoryUuid;

  @ManyToOne
  @JoinColumn(name = "subcategory_uuid", insertable = false, updatable = false)
  private Subcategory subcategory;

  @OneToMany(mappedBy = "article")
  private List<ArticleImage> imageList;

  /**
   * Get discount of article in percent
   * @return formatted discount
   */
  public String getDiscountPercent() {
    if (this.getListPrice() == null) {
      return null;
    }

    //100 - percentage of selling price relative to list price
    return Math.round((100 - (this.getSellingPrice() / this.getListPrice()) * 100)) + "";
  }

  /**
   * Format article price with two decimal places and CHF currency
   * @return formatted price
   */
  public String formatPrice() {
    if (this.getListPrice() != null && this.available) {
      return String.format("%.2f was %.2f CHF", this.getSellingPrice(), this.getListPrice());
    }
    return String.format("%.2f CHF", this.getSellingPrice());
  }

  /**
   * Get url of main article image
   * @return image url
   */

  public String getPrimaryImageURL() {
    Optional<ArticleImage> optImage =
        this.getImageList().stream().filter(e -> e.getPosition() == 1).findAny();
    return optImage.map(ArticleImage::getImageURL).orElse("");
  }
}
