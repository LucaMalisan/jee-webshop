package src.bean;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import lombok.extern.java.Log;

import java.io.Serializable;
import src.model.Article;

@Named("articleBean")
@Log
@ViewScoped
public class ArticleBean implements Serializable {

  @PersistenceContext EntityManager entitymanager;

  public List<Article> getArticles() {
    TypedQuery<Article> query = entitymanager.createQuery("SELECT a FROM Article a", Article.class);
    return query.getResultList();
  }

  public String calculatePriceOfArticle(Article article) {
    double discount = article.getDiscountPercent() == null ? 0 : article.getDiscountPercent();
    double netPrice = ((100.0 - discount) / 100.0) * article.getPriceCHF();
    return String.format("%.2f", netPrice);
  }

  public String formatDiscount(Article article) {
    if (article.getDiscountPercent() != null) {
      return String.format(
          "%s was %d CHF", this.calculatePriceOfArticle(article), article.getDiscountPercent());
    }
    return String.format("%s CHF", article.getPriceCHF().toString());
  }
}
