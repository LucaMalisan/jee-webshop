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

  @Test
  void shouldReturnPageNumbersNearTheBeginning() {
    ArticleRepository repository = mock(ArticleRepository.class);
    HttpServletRequest request = generateMockRequest(1);
    ArticleController controller = new ArticleController(repository);

    when(repository.getArticles(null, null, null)).thenReturn(generateArticles(50));

    List<Integer> pageNumbers = controller.getPageNumbers(request, 3);

    assertEquals(
        List.of(2, 3, 4), pageNumbers, "The page numbers near the beginning should be 2, 3 and 4.");
  }


  private List<Article> generateArticles(int count) {
    List<Article> articles = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      articles.add(new Article());
    }
    return articles;
  }

  private HttpServletRequest generateMockRequest(int page) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getParameter("page")).thenReturn(String.valueOf(page));
    return request;
  }

  @Test
  void shouldReturnFirstPageOfArticles() {
    ArticleRepository repository = mock(ArticleRepository.class);
    HttpServletRequest request = generateMockRequest(1);
    ArticleController controller = new ArticleController(repository);

    List<Article> articles = generateArticles(25);
    when(repository.getArticles(null, null, null)).thenReturn(articles);

    List<Article> pagedArticles = controller.getPagedArticles(request);

    assertEquals(12, pagedArticles.size(), "The first page should contain 12 articles.");
  }

  @Test
  void shouldReturnLastPageOfArticles() {
    ArticleRepository repository = mock(ArticleRepository.class);
    HttpServletRequest request = generateMockRequest(3);
    ArticleController controller = new ArticleController(repository);

    List<Article> articles = generateArticles(25);
    when(repository.getArticles(null, null, null)).thenReturn(articles);

    List<Article> pagedArticles = controller.getPagedArticles(request);

    assertEquals(1, pagedArticles.size(), "The last page should contain 1 article.");
  }

  @Test
  void shouldReturnEmptyListWhenNoArticlesExist() {
    ArticleRepository repository = mock(ArticleRepository.class);
    HttpServletRequest request = generateMockRequest(1);
    ArticleController controller = new ArticleController(repository);

    when(repository.getArticles(null, null, null)).thenReturn(new ArrayList<>());

    List<Article> pagedArticles = controller.getPagedArticles(request);

    assertEquals(
        0,
        pagedArticles.size(),
        "The paged articles should be an empty list if no articles exist.");
  }

  @Test
  void shouldHandleInvalidPageParametersGracefully() {
    ArticleRepository repository = mock(ArticleRepository.class);
    HttpServletRequest request = mock(HttpServletRequest.class);
    ArticleController controller = new ArticleController(repository);

    List<Article> articles = generateArticles(25);
    when(repository.getArticles(null, null, null)).thenReturn(articles);
    when(request.getParameter("page")).thenReturn("-3");

    List<Article> pagedArticles = controller.getPagedArticles(request);

    assertEquals(
        12,
        pagedArticles.size(),
        "The method should default to the first page if an invalid page parameter is provided.");
  }
}
