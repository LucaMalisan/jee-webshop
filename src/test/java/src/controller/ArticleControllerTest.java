package src.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import src.model.Article;
import src.repository.ArticleRepository;

class ArticleControllerTest {

  @BeforeEach
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test class for ArticleController. This class tests various use cases of the getTotalPageCount
   * method, which is responsible for returning the total number of pages containing articles for a
   * given request.
   */
  @Test
  void shouldReturnOnePageWhenNoArticlesAvailable() {
    ArticleRepository repository = mock(ArticleRepository.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    ArticleController controller = new ArticleController(repository);

    when(repository.getArticles(null, null, null)).thenReturn(new ArrayList<>());

    int pageCount = controller.getTotalPageCount(request);

    assertEquals(1, pageCount, "The total page count should be 1 when no articles are available.");
  }

  @Test
  void shouldCalculateOneFullPage() {
    ArticleRepository repository = mock(ArticleRepository.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    ArticleController controller = new ArticleController(repository);

    List<Article> articles = generateArticles(12);
    when(repository.getArticles(null, null, null)).thenReturn(articles);

    int pageCount = controller.getTotalPageCount(request);

    assertEquals(
        1, pageCount, "The total page count should be 1 when there are exactly 12 articles.");
  }

  @Test
  void shouldCalculateMultiplePages() {
    ArticleRepository repository = mock(ArticleRepository.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    ArticleController controller = new ArticleController(repository);

    List<Article> articles = generateArticles(25);
    when(repository.getArticles(null, null, null)).thenReturn(articles);

    int pageCount = controller.getTotalPageCount(request);

    assertEquals(
        3,
        pageCount,
        "The total page count should be 3 for 25 articles with 12 articles per page.");
  }

  private List<Article> generateArticles(int count) {
    List<Article> articles = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      articles.add(new Article());
    }
    return articles;
  }
}
