package src.bean;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import lombok.extern.java.Log;

import java.io.Serializable;
import src.model.Article;
import src.model.ArticleImage;

@Named("articleBean")
@Log
@ViewScoped
public class ArticleBean implements Serializable {

  @PersistenceContext EntityManager entitymanager;

  public List<Article> getArticles() {
    TypedQuery<Article> query = entitymanager.createQuery("SELECT a FROM Article a", Article.class);
    return query.getResultList();
  }

  public String getDiscountPercent(Article article) {
    if (article.getListPrice() == null) {
      return null;
    }

    return Math.round((100 - (article.getSellingPrice() / article.getListPrice()) * 100)) + "";
  }

  public String formatPrice(Article article) {
    if (article.getListPrice() != null) {
      return String.format("%.2f was %.2f CHF", article.getSellingPrice(), article.getListPrice());
    }
    return String.format("%.2f CHF", article.getSellingPrice());
  }

  public String getPrimaryImageURL(Article article) {
    Optional<ArticleImage> optImage =
        article.getImageList().stream().filter(e -> e.getPosition() == 1).findAny();
    return optImage.map(ArticleImage::getImageURL).orElse("");
  }
}
