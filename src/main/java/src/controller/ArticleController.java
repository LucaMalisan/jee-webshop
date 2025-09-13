package src.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;
import src.model.Article;
import src.repository.ArticleRepository;

@Named
@RequestScoped
public class ArticleController {

  @Inject private ArticleRepository repository;

  public List<Article> getArticles() {
    return repository.getArticles();
  }
}
