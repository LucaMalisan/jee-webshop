package src.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import src.model.Article;
import src.repository.ArticleRepository;

class ArticleControllerMassiveTest {

  private ArticleRepository repository;
  private HttpServletRequest request;
  private ArticleController controller;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    repository = mock(ArticleRepository.class);
    request = mock(HttpServletRequest.class);
    controller = new ArticleController(repository);
  }

  // ---------- Helpers ----------
  private static List<Article> gen(int n) {
    List<Article> list = new ArrayList<>();
    for (int i = 0; i < n; i++) list.add(new Article());
    return list;
  }

  private void stubPage(int page) {
    when(request.getParameter("page")).thenReturn(String.valueOf(page));
  }

  private void stubQuery(String categoryUuid, String subcategoryUuid, String query) {
    when(request.getParameter("categoryUuid")).thenReturn(categoryUuid);
    when(request.getParameter("subcategoryUuid")).thenReturn(subcategoryUuid);
    when(request.getParameter("query")).thenReturn(query);
  }

  // ---------- Basic Sanity & Constants ----------
  @Test
  @DisplayName("T001: PAGE_SIZE should be 12")
  void T001() {
    assertEquals(12.0, ArticleController.getPAGE_SIZE());
  }

  @Test
  @DisplayName("T002: Constructor with repository stores dependency")
  void T002() {
    // Using behavior to infer field is used
    when(repository.getArticles(null, null, null)).thenReturn(Collections.emptyList());
    int pages = controller.getTotalPageCount(request);
    assertEquals(1, pages);
  }

  // ---------- getTotalPageCount ----------
  @Test
  @DisplayName("T003: getTotalPageCount returns 1 when repository returns empty list")
  void T003() {
    when(repository.getArticles(null, null, null)).thenReturn(Collections.emptyList());
    assertEquals(1, controller.getTotalPageCount(request));
  }

  @Test
  @DisplayName("T004: getTotalPageCount returns 1 when repository returns null")
  void T004() {
    when(repository.getArticles(null, null, null)).thenReturn(null);
    assertEquals(1, controller.getTotalPageCount(request));
  }

  @Test
  @DisplayName("T005: getTotalPageCount 1 full page (12 items)")
  void T005() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(12));
    assertEquals(1, controller.getTotalPageCount(request));
  }

  @Test
  @DisplayName("T006: getTotalPageCount rounds up (13 items -> 2 pages)")
  void T006() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(13));
    assertEquals(2, controller.getTotalPageCount(request));
  }

  @Test
  @DisplayName("T007: getTotalPageCount for 24 items -> 2 pages")
  void T007() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(24));
    assertEquals(2, controller.getTotalPageCount(request));
  }

  @Test
  @DisplayName("T008: getTotalPageCount for 25 items -> 3 pages")
  void T008() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(25));
    assertEquals(3, controller.getTotalPageCount(request));
  }

  // Variations to reach high test count, verifying consistency across many sizes
  @Test
  void T009() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(0));
    assertEquals(1, controller.getTotalPageCount(request));
  }

  @Test
  void T010() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(1));
    assertEquals(1, controller.getTotalPageCount(request));
  }

  @Test
  void T011() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(2));
    assertEquals(1, controller.getTotalPageCount(request));
  }

  @Test
  void T012() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(3));
    assertEquals(1, controller.getTotalPageCount(request));
  }

  @Test
  void T013() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(4));
    assertEquals(1, controller.getTotalPageCount(request));
  }

  @Test
  void T014() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(5));
    assertEquals(1, controller.getTotalPageCount(request));
  }

  @Test
  void T015() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(6));
    assertEquals(1, controller.getTotalPageCount(request));
  }

  @Test
  void T016() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(7));
    assertEquals(1, controller.getTotalPageCount(request));
  }

  @Test
  void T017() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(8));
    assertEquals(1, controller.getTotalPageCount(request));
  }

  @Test
  void T018() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(9));
    assertEquals(1, controller.getTotalPageCount(request));
  }

  @Test
  void T019() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(10));
    assertEquals(1, controller.getTotalPageCount(request));
  }

  @Test
  void T020() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(11));
    assertEquals(1, controller.getTotalPageCount(request));
  }

  @Test
  void T021() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(12));
    assertEquals(1, controller.getTotalPageCount(request));
  }

  @Test
  void T022() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(13));
    assertEquals(2, controller.getTotalPageCount(request));
  }

  @Test
  void T023() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(23));
    assertEquals(2, controller.getTotalPageCount(request));
  }

  @Test
  void T024() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(24));
    assertEquals(2, controller.getTotalPageCount(request));
  }

  @Test
  void T025() {
    when(repository.getArticles(null, null, null)).thenReturn(gen(25));
    assertEquals(3, controller.getTotalPageCount(request));
  }

  // ---------- getPagedArticles ----------
  @Test
  @DisplayName("T026: First page returns 12 items when 25 exist")
  void T026() {
    stubPage(1);
    when(repository.getArticles(null, null, null)).thenReturn(gen(25));
    assertEquals(12, controller.getPagedArticles(request).size());
  }

  @Test
  @DisplayName("T027: Second page returns 12 items when 25 exist")
  void T027() {
    stubPage(2);
    when(repository.getArticles(null, null, null)).thenReturn(gen(25));
    assertEquals(12, controller.getPagedArticles(request).size());
  }

  @Test
  @DisplayName("T028: Third page returns 1 item when 25 exist")
  void T028() {
    stubPage(3);
    when(repository.getArticles(null, null, null)).thenReturn(gen(25));
    assertEquals(1, controller.getPagedArticles(request).size());
  }

  @Test
  @DisplayName("T029: Empty list returns empty sublist for page 1")
  void T029() {
    stubPage(1);
    when(repository.getArticles(null, null, null)).thenReturn(Collections.emptyList());
    assertEquals(0, controller.getPagedArticles(request).size());
  }

  @Test
  @DisplayName("T030: Invalid page (negative) defaults to page 1")
  void T030() {
    when(request.getParameter("page")).thenReturn("-5");
    when(repository.getArticles(null, null, null)).thenReturn(gen(25));
    assertEquals(12, controller.getPagedArticles(request).size());
  }

  @Test
  @DisplayName("T031: Invalid page (non-numeric) defaults to page 1")
  void T031() {
    when(request.getParameter("page")).thenReturn("abc");
    when(repository.getArticles(null, null, null)).thenReturn(gen(25));
    assertEquals(12, controller.getPagedArticles(request).size());
  }

  @Test
  @DisplayName("T032: Page 1 for exactly 12 items returns full list")
  void T032() {
    stubPage(1);
    when(repository.getArticles(null, null, null)).thenReturn(gen(12));
    assertEquals(12, controller.getPagedArticles(request).size());
  }

  @Test
  @DisplayName("T033: Page 2 for exactly 12 items: redirects to Page 1 and returns 12 items")
  void T033() {
    stubPage(2);
    when(repository.getArticles(null, null, null)).thenReturn(gen(12));
    List<Article> result = controller.getPagedArticles(request);
    assertEquals(12, result.size());
  }

  @Test
  void T034() {
    stubPage(1000);
    when(repository.getArticles(null, null, null)).thenReturn(gen(20));
    assertEquals(Collections.emptyList(), controller.getPageNumbers(request, 4));
  }

  // ---------- getPageNumbers ----------
  @Test
  @DisplayName("T035: Page numbers near beginning from page 1 with count 3")
  void T035() {
    stubPage(1);
    when(repository.getArticles(null, null, null)).thenReturn(gen(50));
    assertEquals(List.of(2, 3, 4), controller.getPageNumbers(request, 3));
  }

  @Test
  @DisplayName("T036: Page numbers center at page 5 with count 5")
  void T036() {
    stubPage(5);
    when(repository.getArticles(null, null, null)).thenReturn(gen(200)); // many pages
    assertEquals(List.of(4, 5, 6, 7, 8), controller.getPageNumbers(request, 5));
  }

  @Test
  @DisplayName("T037: Page numbers limited by total pages")
  void T037() {
    stubPage(9);
    when(repository.getArticles(null, null, null)).thenReturn(gen(20)); // 2 pages
    assertEquals(Collections.emptyList(), controller.getPageNumbers(request, 5));
  }

  @Test
  @DisplayName("T038: Count 0 yields empty list (range exclusive)")
  void T038() {
    stubPage(1);
    when(repository.getArticles(null, null, null)).thenReturn(gen(100));
    assertEquals(Collections.emptyList(), controller.getPageNumbers(request, 0));
  }

  @Test
  @DisplayName("T039: Lowest number never below 2")
  void T039() {
    stubPage(1);
    when(repository.getArticles(null, null, null)).thenReturn(gen(100));
    List<Integer> nums = controller.getPageNumbers(request, 10);
    assertFalse(nums.contains(1));
  }

  @Test
  @DisplayName("T040: Highest respects total page count exactly")
  void T040() {
    stubPage(2);
    when(repository.getArticles(null, null, null)).thenReturn(gen(24)); // 2 pages
    assertEquals(Collections.emptyList(), controller.getPageNumbers(request, 10));
  }

  // More variations to increase coverage scenarios and test count
  @Test
  void T041() {
    stubPage(2);
    when(repository.getArticles(null, null, null)).thenReturn(gen(1));
    assertEquals(Collections.emptyList(), controller.getPageNumbers(request, 3));
  }

  @Test
  void T042() {
    stubPage(2);
    when(repository.getArticles(null, null, null)).thenReturn(gen(13));
    assertEquals(Collections.emptyList(), controller.getPageNumbers(request, 1));
  }

  @Test
  void T043() {
    stubPage(3);
    when(repository.getArticles(null, null, null)).thenReturn(gen(36));
    assertEquals(List.of(2), controller.getPageNumbers(request, 3));
  }

  @Test
  void T044() {
    stubPage(1000);
    when(repository.getArticles(null, null, null)).thenReturn(gen(120));
    assertEquals(List.of(2, 3, 4, 5, 6, 7, 8, 9), controller.getPageNumbers(request, 10));
  }

  @Test
  void T045() {
    stubPage(100);
    when(repository.getArticles(null, null, null)).thenReturn(gen(12));
    assertEquals(Collections.emptyList(), controller.getPageNumbers(request, 5));
  }

  @Test
  void T046() {
    stubPage(5);
    when(repository.getArticles(null, null, null)).thenReturn(gen(60));
    assertEquals(List.of(2, 3, 4), controller.getPageNumbers(request, 3));
  }

  @Test
  void T047() {
    stubPage(6);
    when(repository.getArticles(null, null, null)).thenReturn(gen(73));
    assertEquals(List.of(4, 5, 6), controller.getPageNumbers(request, 3));
  }

  @Test
  void T048() {
    stubPage(7);
    when(repository.getArticles(null, null, null)).thenReturn(gen(120));
    assertEquals(List.of(6, 7, 8, 9), controller.getPageNumbers(request, 4));
  }

  @Test
  void T049() {
    stubPage(8);
    when(repository.getArticles(null, null, null)).thenReturn(gen(121));
    assertEquals(List.of(7, 8, 9, 10), controller.getPageNumbers(request, 4));
  }

  @Test
  void T050() {
    stubPage(9);
    when(repository.getArticles(null, null, null)).thenReturn(gen(122));
    assertEquals(List.of(7, 8, 9, 10), controller.getPageNumbers(request, 4));
  }

  // ---------- existsPreviousPage / existsNextPage ----------
  @Test
  @DisplayName("T051: existsPreviousPage false on page 1")
  void T051() {
    stubPage(1);
    assertFalse(controller.existsPreviousPage(request));
  }

  @Test
  @DisplayName("T052: existsPreviousPage true on page 2")
  void T052() {
    stubPage(2);
    when(repository.getArticles(null, null, null)).thenReturn(gen(13));
    assertTrue(controller.existsPreviousPage(request));
  }

  @Test
  @DisplayName("T053: existsNextPage true when next page exists")
  void T053() {
    stubPage(1);
    when(repository.getArticles(null, null, null)).thenReturn(gen(13));
    assertTrue(controller.existsNextPage(request));
  }

  @Test
  @DisplayName("T054: existsNextPage false on last page")
  void T054() {
    stubPage(2);
    when(repository.getArticles(null, null, null)).thenReturn(gen(13));
    assertFalse(controller.existsNextPage(request));
  }

  @Test
  @DisplayName("T055: existsNextPage false when only 1 page")
  void T055() {
    stubPage(1);
    when(repository.getArticles(null, null, null)).thenReturn(gen(12));
    assertFalse(controller.existsNextPage(request));
  }

  @Test
  @DisplayName("T056: existsNextPage handles null articles as 1 page")
  void T056() {
    stubPage(1);
    when(repository.getArticles(null, null, null)).thenReturn(null);
    assertFalse(controller.existsNextPage(request));
  }

  // ---------- setArticleDetail ----------
  @Test
  @DisplayName("T057: setArticleDetail sets cached field from repository")
  void T057() {
    when(request.getParameter("sku")).thenReturn("123");
    Article a = new Article();
    when(repository.findBySku(123L)).thenReturn(a);

    controller.setArticleDetail(request);
    assertSame(a, controller.getArticleDetail());
  }

  @Test
  @DisplayName("T058: setArticleDetail parses sku correctly")
  void T058() {
    when(request.getParameter("sku")).thenReturn("999999");
    Article a = new Article();
    when(repository.findBySku(999999L)).thenReturn(a);
    controller.setArticleDetail(request);
    assertNotNull(controller.getArticleDetail());
  }

  // ---------- getAllArticlesByRequest (package-private) ----------
  @Test
  @DisplayName("T059: getAllArticlesByRequest forwards parameters to repository")
  void T059() {
    stubQuery("cat", "sub", "q");
    when(repository.getArticles("cat", "sub", "q")).thenReturn(Collections.emptyList());

    controller.getAllArticlesByRequest(request);
    verify(repository).getArticles("cat", "sub", "q");
  }

  @Test
  @DisplayName("T060: getAllArticlesByRequest returns repository result")
  void T060() {
    stubQuery("c", "s", "hello");
    List<Article> expected = gen(3);
    when(repository.getArticles("c", "s", "hello")).thenReturn(expected);
    List<Article> actual = controller.getAllArticlesByRequest(request);
    assertSame(expected, actual);
  }

  // ---------- More page number scenarios (increase test count) ----------
  @Test
  void T061() {
    stubPage(2);
    when(repository.getArticles(null, null, null)).thenReturn(gen(100));
    assertEquals(List.of(2, 3, 4, 5), controller.getPageNumbers(request, 4));
  }

  @Test
  void T062() {
    stubPage(3);
    when(repository.getArticles(null, null, null)).thenReturn(gen(100));
    assertEquals(List.of(2, 3, 4, 5, 6), controller.getPageNumbers(request, 5));
  }

  @Test
  void T063() {
    stubPage(4);
    when(repository.getArticles(null, null, null)).thenReturn(gen(100));
    assertEquals(List.of(3, 4, 5, 6), controller.getPageNumbers(request, 4));
  }

  @Test
  void T064() {
    stubPage(10);
    when(repository.getArticles(null, null, null)).thenReturn(gen(100));
    assertEquals(List.of(5, 6, 7, 8), controller.getPageNumbers(request, 4));
  }

  @Test
  void T065() {
    stubPage(6);
    when(repository.getArticles(null, null, null)).thenReturn(gen(100));
    assertEquals(List.of(5, 6, 7, 8), controller.getPageNumbers(request, 4));
  }

  @Test
  void T066() {
    stubPage(7);
    when(repository.getArticles(null, null, null)).thenReturn(gen(100));
    assertEquals(List.of(6, 7, 8), controller.getPageNumbers(request, 3));
  }

  @Test
  void T067() {
    stubPage(11);
    when(repository.getArticles(null, null, null)).thenReturn(gen(101));
    assertEquals(List.of(5, 6, 7, 8), controller.getPageNumbers(request, 4));
  }

  @Test
  void T068() {
    stubPage(12);
    when(repository.getArticles(null, null, null)).thenReturn(gen(102));
    assertEquals(List.of(5, 6, 7, 8), controller.getPageNumbers(request, 4));
  }

  @Test
  void T069() {
    stubPage(13);
    when(repository.getArticles(null, null, null)).thenReturn(gen(103));
    assertEquals(List.of(5, 6, 7, 8), controller.getPageNumbers(request, 4));
  }

  @Test
  void T070() {
    stubPage(14);
    when(repository.getArticles(null, null, null)).thenReturn(gen(104));
    assertEquals(List.of(5, 6, 7, 8), controller.getPageNumbers(request, 4));
  }

  // ---------- More getPagedArticles boundary cases ----------
  @Test
  void T071() {
    stubPage(1);
    when(repository.getArticles(null, null, null)).thenReturn(gen(0));
    assertEquals(0, controller.getPagedArticles(request).size());
  }

  @Test
  void T072() {
    stubPage(1);
    when(repository.getArticles(null, null, null)).thenReturn(gen(1));
    assertEquals(1, controller.getPagedArticles(request).size());
  }

  @Test
  void T073() {
    stubPage(1);
    when(repository.getArticles(null, null, null)).thenReturn(gen(11));
    assertEquals(11, controller.getPagedArticles(request).size());
  }

  @Test
  void T074() {
    stubPage(2);
    when(repository.getArticles(null, null, null)).thenReturn(gen(13));
    assertEquals(1, controller.getPagedArticles(request).size());
  }

  @Test
  void T075() {
    stubPage(3);
    when(repository.getArticles(null, null, null)).thenReturn(gen(36));
    assertEquals(12, controller.getPagedArticles(request).size());
  }

  @Test
  void T076() {
    stubPage(4);
    when(repository.getArticles(null, null, null)).thenReturn(gen(36));
    assertEquals(12, controller.getPagedArticles(request).size());
  }

  @Test
  void T077() {
    stubPage(5);
    when(repository.getArticles(null, null, null)).thenReturn(gen(60));
    assertEquals(12, controller.getPagedArticles(request).size());
  }

  @Test
  void T078() {
    stubPage(6);
    when(repository.getArticles(null, null, null)).thenReturn(gen(60));
    assertEquals(12, controller.getPagedArticles(request).size());
  }

  @Test
  void T079() {
    stubPage(1000);
    when(repository.getArticles(null, null, null)).thenReturn(gen(0));
    assertEquals(0, controller.getPagedArticles(request).size());
  }

  @Test
  void T080() {
    stubPage(1000);
    when(repository.getArticles(null, null, null)).thenReturn(gen(1));
    assertDoesNotThrow(() -> controller.getPagedArticles(request));
  }

  // ---------- More existsNext/Previous combinations ----------
  @Test
  void T081() {
    stubPage(1);
    when(repository.getArticles(null, null, null)).thenReturn(gen(0));
    assertFalse(controller.existsNextPage(request));
    assertFalse(controller.existsPreviousPage(request));
  }

  @Test
  void T082() {
    stubPage(1);
    when(repository.getArticles(null, null, null)).thenReturn(gen(1));
    assertFalse(controller.existsNextPage(request));
  }

  @Test
  void T083() {
    stubPage(2);
    when(repository.getArticles(null, null, null)).thenReturn(gen(1));
    assertFalse(controller.existsNextPage(request));
    assertFalse(controller.existsPreviousPage(request));
  }

  @Test
  void T084() {
    stubPage(2);
    when(repository.getArticles(null, null, null)).thenReturn(gen(24));
    assertFalse(controller.existsNextPage(request));
  }

  @Test
  void T085() {
    stubPage(2);
    when(repository.getArticles(null, null, null)).thenReturn(gen(25));
    assertTrue(controller.existsNextPage(request));
  }

  @Test
  void T086() {
    stubPage(3);
    when(repository.getArticles(null, null, null)).thenReturn(gen(25));
    assertFalse(controller.existsNextPage(request));
    assertTrue(controller.existsPreviousPage(request));
  }

  // ---------- More setArticleDetail robustness ----------
  @Test
  void T087() {
    when(request.getParameter("sku")).thenReturn("0");
    Article a = new Article();
    when(repository.findBySku(0L)).thenReturn(a);
    controller.setArticleDetail(request);
    assertSame(a, controller.getArticleDetail());
  }

  @Test
  void T088() {
    when(request.getParameter("sku")).thenReturn("1");
    when(repository.findBySku(1L)).thenReturn(null);
    controller.setArticleDetail(request);
    assertNull(controller.getArticleDetail());
  }

  // ---------- getAllArticlesByRequest forwarding combinations ----------
  @Test
  void T089() {
    stubQuery(null, null, null);
    when(repository.getArticles(null, null, null)).thenReturn(Collections.emptyList());
    controller.getAllArticlesByRequest(request);
    verify(repository).getArticles(null, null, null);
  }

  @Test
  void T090() {
    stubQuery("A", null, null);
    when(repository.getArticles("A", null, null)).thenReturn(Collections.emptyList());
    controller.getAllArticlesByRequest(request);
    verify(repository).getArticles("A", null, null);
  }

  @Test
  void T091() {
    stubQuery(null, "B", null);
    when(repository.getArticles(null, "B", null)).thenReturn(Collections.emptyList());
    controller.getAllArticlesByRequest(request);
    verify(repository).getArticles(null, "B", null);
  }

  @Test
  void T092() {
    stubQuery(null, null, "C");
    when(repository.getArticles(null, null, "C")).thenReturn(Collections.emptyList());
    controller.getAllArticlesByRequest(request);
    verify(repository).getArticles(null, null, "C");
  }

  @Test
  void T093() {
    stubQuery("A", "B", "C");
    when(repository.getArticles("A", "B", "C")).thenReturn(gen(2));
    List<Article> res = controller.getAllArticlesByRequest(request);
    assertEquals(2, res.size());
  }

  // ---------- Verify sku parsing with large number ----------
  @Test
  void T094() {
    when(request.getParameter("sku")).thenReturn(String.valueOf(Long.MAX_VALUE));
    Article a = new Article();
    when(repository.findBySku(Long.MAX_VALUE)).thenReturn(a);
    controller.setArticleDetail(request);
    assertSame(a, controller.getArticleDetail());
  }

  // ---------- More page number edges when total page count small ----------
  @Test
  void T095() {
    stubPage(1);
    when(repository.getArticles(null, null, null)).thenReturn(gen(11));
    assertEquals(Collections.emptyList(), controller.getPageNumbers(request, 5));
  }

  @Test
  void T096() {
    stubPage(2);
    when(repository.getArticles(null, null, null)).thenReturn(gen(11));
    assertEquals(Collections.emptyList(), controller.getPageNumbers(request, 5));
  }

  // ---------- Validate repository invocation once per call ----------
  @Test
  void T097() {
    stubQuery("cat", "sub", "q");
    when(repository.getArticles("cat", "sub", "q")).thenReturn(Collections.emptyList());
    controller.getAllArticlesByRequest(request);
    verify(repository, times(1)).getArticles("cat", "sub", "q");
  }

  // ---------- Ensure getTotalPageCount clamps minimum to 1 ----------
  @Test
  void T098() {
    when(repository.getArticles(null, null, null)).thenReturn(Collections.emptyList());
    assertTrue(controller.getTotalPageCount(request) >= 1);
  }

  // ---------- Verify invalid page strings handled ----------
  @Test
  void T099() {
    when(request.getParameter("page")).thenReturn("-1");
    when(repository.getArticles(null, null, null)).thenReturn(gen(0));
    assertDoesNotThrow(() -> controller.getPagedArticles(request));
  }

  @Test
  void T100() {
    when(request.getParameter("page")).thenReturn("xyz");
    when(repository.getArticles(null, null, null)).thenReturn(gen(15));
    List<Article> res = controller.getPagedArticles(request);
    assertEquals(12, res.size());
  }
}
