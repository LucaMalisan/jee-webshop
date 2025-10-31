package src.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;
import org.eclipse.krazo.lifecycle.RequestLifecycle;
import src.model.Article;
import src.model.ShoppingCart;
import src.repository.ArticleRepository;
import src.repository.ShoppingCartRepository;

// TODO use PostConstruct

@Named
@RequestScoped
@SuppressWarnings("unused")
public class ArticleController {

  @Inject private ArticleRepository repository;
  @Inject private ShoppingCartRepository cartRepository;
  @Inject private RequestLifecycle requestLifecycle;

  @Getter private Article articleDetail;
  private List<Article> articles;
  private static final double PAGE_SIZE = 12;
  @Inject private ShoppingCartRepository shoppingCartRepository;
  @Named @Inject private AuthController authController;

  /**
   * Calculates the highest page available based upon the result set of articles
   *
   * @return highest available page
   */
  public int getTotalPageCount(HttpServletRequest request) {
    List<Article> articles = getAllArticlesByRequest(request);
    return (int) Math.ceil(articles.size() / PAGE_SIZE);
  }

  /**
   * Queries articles from the database respecting given filters and applies pagination
   *
   * @param request: HttpServletRequest
   * @return result set of corresponding page
   */
  public List<Article> getPagedArticles(HttpServletRequest request) {
    int page = this.getPageByRequest(request);
    List<Article> articles = getAllArticlesByRequest(request);

    return articles.subList(
        this.calcSublistStartIndex(page), this.calcSublistEndIndex(page, articles.size()));
  }

  /**
   * Calculates the page numbers to be shown in the navigation pane, based on given count. The
   * returned list follows the scheme [page-1, page, page+1, ...)
   *
   * @param request: HttpServletRequest
   * @param count: how many page numbers should be shown
   * @return list with calculated page numbers
   */
  public List<Integer> getPageNumbers(HttpServletRequest request, int count) {
    int page = this.getPageByRequest(request);

    // lowest page number must at least be 2
    int lowestNumber = Math.max(2, page - 1);

    // highest number should have a distance of <count> to lowest number, but mustn't exceed total
    // page count
    int highestNumber = Math.min(lowestNumber + count, this.getTotalPageCount(request));

    return IntStream.range(lowestNumber, highestNumber).boxed().collect(Collectors.toList());
  }

  public boolean existsPreviousPage(HttpServletRequest request) {
    return this.getPageByRequest(request) > 1;
  }

  public boolean existsNextPage(HttpServletRequest request) {
    return this.getPageByRequest(request) < this.getTotalPageCount(request);
  }

  public void setArticleDetail(HttpServletRequest request) {
    long sku = Long.parseLong(request.getParameter("sku"));
    this.articleDetail = repository.findBySku(sku);
  }

  public List<ShoppingCart> getShoppingCartEntries(HttpServletRequest request) {
    String email = authController.extractEmail(request);
    return shoppingCartRepository.getShoppingCartEntries(email);
  }

  /**
   * Parse page out of request and fix invalid values
   *
   * @param request: HttpServletRequest
   * @return parsed page number
   */
  private int getPageByRequest(HttpServletRequest request) {
    int page = 1; // fallback value if parsing fails

    try {
      page = Integer.parseInt(request.getParameter("page"));
    } catch (Exception e) {
    }

    // return parsed page, but it must be at least 1
    return Math.max(page, 1);
  }

  private List<Article> getAllArticlesByRequest(HttpServletRequest request) {
    String categoryUuid = request.getParameter("categoryUuid");
    String subcategoryUuid = request.getParameter("subcategoryUuid");
    String query = request.getParameter("query");

    return repository.getArticles(categoryUuid, subcategoryUuid, query);
  }

  /**
   * Used formula <code> (page - 1) * PAGE_SIZE </code> corresponding to (page=1 -> index=0,
   * page=2-> index=12, ...)
   *
   * @param page: current page
   * @return startIndex for sublist based on page size
   */
  private int calcSublistStartIndex(int page) {
    return (int) ((page - 1) * PAGE_SIZE);
  }

  /**
   * Used formula <code> Math.min(totalListSize, page * PAGE_SIZE) </code>; if calculated page
   * exceeds list size, restrict end index to list size
   *
   * @param page: current page
   * @return endIndex for sublist based on page size
   */
  private int calcSublistEndIndex(int page, int totalListSize) {
    return (int) (Math.min(totalListSize, page * PAGE_SIZE));
  }
}
