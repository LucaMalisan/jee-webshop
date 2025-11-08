package src.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;
import src.model.Article;
import src.repository.ArticleRepository;

@Named
@RequestScoped
@SuppressWarnings("unused")
public class ArticleController {

  private ArticleRepository repository;

    public ArticleController() {}

  @Inject
  public ArticleController(ArticleRepository repository) {
    this.repository = repository;
  }

  @Getter private Article articleDetail;
  @Getter private List<Article> articles;
  @Getter private static final double PAGE_SIZE = 12;

  /**
   * Calculates the highest page available based upon the result set of articles
   *
   * @return highest available page
   */
  public int getTotalPageCount(HttpServletRequest request) {
    List<Article> articles = getAllArticlesByRequest(request);
    return articles == null ? 1 : (int) Math.max(Math.ceil(articles.size() / PAGE_SIZE), 1);
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
   * Calculates the page numbers to be shown in the navigation pane, based on given count.
   * The page numbers should cover the spectrum around the given page, but may also return the numbers bigger or
   * lower the given page in order to not exceed min or max page.
   * If the page total count is too small, only give as many numbers as possible
   *
   * @param request: HttpServletRequest
   * @param count: how many page numbers should be shown
   * @return list with calculated page numbers
   */
  public List<Integer> getPageNumbers(HttpServletRequest request, int count) {
    int page = this.getPageByRequest(request);
    int correctCount = Math.min(count, this.getTotalPageCount(request) - 2);

    // lowest page number must at least be 2
    int lowestNumber = Math.max(2, page - 1);

    // highest number should have a distance of <count> to lowest number, but mustn't exceed total
    // page count
    int highestNumber = Math.min(lowestNumber + correctCount, this.getTotalPageCount(request));

    //re-calculation based on calculated highest number
    lowestNumber = highestNumber - correctCount;

    return IntStream.range(lowestNumber, lowestNumber + correctCount).boxed().collect(Collectors.toList());
  }

  public boolean existsPreviousPage(HttpServletRequest request) {
    return this.getPageByRequest(request) > 1;
  }

  public boolean existsNextPage(HttpServletRequest request) {
    return this.getPageByRequest(request) < this.getTotalPageCount(request);
  }

  /**
   * Use to cache article-detail to retrieve it later from JSF
   *
   * @param request: request
   */
  public void setArticleDetail(HttpServletRequest request) {
    long sku = Long.parseLong(request.getParameter("sku"));
    this.articleDetail = repository.findBySku(sku);
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
    } catch (Exception ignored) {
    }

    // return parsed page, but it must be at least 1
    return Math.max(page, 1);
  }

  /**
   * Parses the search form data out of the request
   *
   * @param request: request
   * @return list of matching articles
   */
  List<Article> getAllArticlesByRequest(HttpServletRequest request) {
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
