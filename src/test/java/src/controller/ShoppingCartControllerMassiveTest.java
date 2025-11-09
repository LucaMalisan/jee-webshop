package src.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import src.model.Article;
import src.model.ShoppingCart;
import src.repository.ArticleRepository;
import src.repository.ShoppingCartRepository;

class ShoppingCartControllerMassiveTest {

  private ShoppingCartRepository shoppingCartRepository;
  private ArticleRepository articleRepository;
  private AuthController authController; // mocked for getShoppingCartEntries
  private HttpServletRequest request;
  private ShoppingCartController controller;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    shoppingCartRepository = mock(ShoppingCartRepository.class);
    articleRepository = mock(ArticleRepository.class);
    authController = mock(AuthController.class);
    request = mock(HttpServletRequest.class);
    controller = new ShoppingCartController(shoppingCartRepository, authController);
    // Inject articleRepository via reflection since constructor doesn't accept it
    try {
      var f = ShoppingCartController.class.getDeclaredField("articleRepository");
      f.setAccessible(true);
      f.set(controller, articleRepository);
    } catch (Exception ignored) {}
  }

  // -------- Helpers --------
  private static String b64(String s) {
    return Base64.getUrlEncoder().withoutPadding()
        .encodeToString(s.getBytes(StandardCharsets.UTF_8));
  }

  private static String jwtWithEmail(String email) {
    String header = b64("{\"alg\":\"none\"}");
    String payload = b64("{\"email\":\"" + email + "\"}");
    return header + "." + payload + ".x"; // signature not verified by JWT.decode
  }

  private void cookies(Cookie... cookies) { when(request.getCookies()).thenReturn(cookies); }
  private void cookieJwt(String token) { cookies(new Cookie("jwt", token)); }

  private static List<ShoppingCart> gen(int n) {
    List<ShoppingCart> list = new ArrayList<>();
    for (int i = 0; i < n; i++) list.add(new ShoppingCart());
    return list;
  }

  // -------- getShoppingCartEntries --------
  @Test @DisplayName("SC001: getShoppingCartEntries returns empty when email null")
  void SC001() {
    when(authController.extractEmail(request)).thenReturn(null);
    assertTrue(controller.getShoppingCartEntries(request).isEmpty());
    verify(shoppingCartRepository, never()).getShoppingCartEntries(anyString());
  }

  @Test @DisplayName("SC002: getShoppingCartEntries returns repo result when email present")
  void SC002() {
    when(authController.extractEmail(request)).thenReturn("u@example.com");
    List<ShoppingCart> expected = gen(3);
    when(shoppingCartRepository.getShoppingCartEntries("u@example.com")).thenReturn(expected);
    assertSame(expected, controller.getShoppingCartEntries(request));
  }

  @Test @DisplayName("SC003: getShoppingCartEntries with empty string email")
  void SC003() {
    when(authController.extractEmail(request)).thenReturn("");
    when(shoppingCartRepository.getShoppingCartEntries("")).thenReturn(List.of());
    assertEquals(0, controller.getShoppingCartEntries(request).size());
  }

  // -------- getPriceExclVat / getVat / getTotal / getDiscount --------
  // These instantiate new AuthController() internally, so we set a JWT cookie on the request and
  // stub repository calls by decoded email.

  @Test @DisplayName("SC004: getPriceExclVat zero total -> 0.00 CHF")
  void SC004() {
    cookieJwt(jwtWithEmail("a@b"));
    when(shoppingCartRepository.getTotalPrice("a@b")).thenReturn(0.0);
    assertEquals("0.00 CHF", controller.getPriceExclVat(request));
  }

  @Test @DisplayName("SC005: getPriceExclVat normal rounding")
  void SC005() {
    cookieJwt(jwtWithEmail("x@y"));
    when(shoppingCartRepository.getTotalPrice("x@y")).thenReturn(100.0);
    assertEquals("93.00 CHF", controller.getPriceExclVat(request));
  }

  @Test @DisplayName("SC006: getPriceExclVat fractional total")
  void SC006() {
    cookieJwt(jwtWithEmail("e@x"));
    when(shoppingCartRepository.getTotalPrice("e@x")).thenReturn(12.345);
    assertEquals("11.48 CHF", controller.getPriceExclVat(request));
  }

  @Test @DisplayName("SC007: getPriceExclVat large total")
  void SC007() {
    cookieJwt(jwtWithEmail("big@ex"));
    when(shoppingCartRepository.getTotalPrice("big@ex")).thenReturn(123456.78);
    // 123456.78 * 0.93 = 114814.8054 -> 114814.81
    assertEquals("114814.81 CHF", controller.getPriceExclVat(request));
  }

  @Test @DisplayName("SC008: getPriceExclVat with bad token -> null email path")
  void SC008() {
    cookieJwt("invalid");
    when(shoppingCartRepository.getTotalPrice(null)).thenReturn(50.0);
    assertEquals("46.50 CHF", controller.getPriceExclVat(request));
  }

  @Test @DisplayName("SC009: getVat zero total -> 0.00 CHF")
  void SC009() {
    cookieJwt(jwtWithEmail("v@t"));
    when(shoppingCartRepository.getTotalPrice("v@t")).thenReturn(0.0);
    assertEquals("0.00 CHF", controller.getVat(request));
  }

  @Test @DisplayName("SC010: getVat normal value")
  void SC010() {
    cookieJwt(jwtWithEmail("v@t2"));
    when(shoppingCartRepository.getTotalPrice("v@t2")).thenReturn(100.0);
    assertEquals("7.00 CHF", controller.getVat(request));
  }

  @Test @DisplayName("SC011: getVat fractional rounding")
  void SC011() {
    cookieJwt(jwtWithEmail("v@t3"));
    when(shoppingCartRepository.getTotalPrice("v@t3")).thenReturn(12.345);
    assertEquals("0.86 CHF", controller.getVat(request));
  }

  @Test @DisplayName("SC012: getVat with bad token -> null email")
  void SC012() {
    cookieJwt(".");
    when(shoppingCartRepository.getTotalPrice(null)).thenReturn(3.33);
    assertEquals("0.23 CHF", controller.getVat(request));
  }

  @Test @DisplayName("SC013: getTotal formats to 2 decimals")
  void SC013() {
    cookieJwt(jwtWithEmail("t@a"));
    when(shoppingCartRepository.getTotalPrice("t@a")).thenReturn(1.2);
    assertEquals("1.20 CHF", controller.getTotal(request));
  }

  @Test @DisplayName("SC014: getTotal large rounding")
  void SC014() {
    cookieJwt(jwtWithEmail("t@b"));
    when(shoppingCartRepository.getTotalPrice("t@b")).thenReturn(1234.567);
    assertEquals("1234.57 CHF", controller.getTotal(request));
  }

  @Test @DisplayName("SC015: getTotal null email path")
  void SC015() {
    cookieJwt("a.b");
    when(shoppingCartRepository.getTotalPrice(null)).thenReturn(9.999);
    assertEquals("10.00 CHF", controller.getTotal(request));
  }

  @Test @DisplayName("SC016: getDiscount zero and rounding")
  void SC016() {
    cookieJwt(jwtWithEmail("d@a"));
    when(shoppingCartRepository.getTotalDiscount("d@a")).thenReturn(0.0);
    assertEquals("0.00 CHF", controller.getDiscount(request));
  }

  @Test @DisplayName("SC017: getDiscount rounding up 1.005 -> 1.01 CHF")
  void SC017() {
    cookieJwt(jwtWithEmail("d@b"));
    when(shoppingCartRepository.getTotalDiscount("d@b")).thenReturn(1.005);
    assertEquals("1.01 CHF", controller.getDiscount(request));
  }

  @Test @DisplayName("SC018: getDiscount null email path")
  void SC018() {
    cookieJwt("invalid");
    when(shoppingCartRepository.getTotalDiscount(null)).thenReturn(7.777);
    assertEquals("7.78 CHF", controller.getDiscount(request));
  }

  // -------- getOrUpdateShoppingCart --------
  @Test @DisplayName("SC019: getOrUpdateShoppingCart creates new when not exists")
  void SC019() {
    long sku = 5L; long amount = 3L; String email = "x@y";
    Article a = new Article(); a.setStock(10);
    when(articleRepository.findBySku(sku)).thenReturn(a);
    when(shoppingCartRepository.findBySkuAndEmail(sku, email)).thenReturn(null);
    ShoppingCart sc = controller.getOrUpdateShoppingCart(sku, amount, email);
    assertEquals(sku, sc.getArticleSku());
    assertEquals(email, sc.getEmail());
    assertEquals(3L, sc.getAmount());
    assertNotNull(sc.getUuid());
  }

  @Test @DisplayName("SC020: getOrUpdateShoppingCart increments when exists")
  void SC020() {
    long sku = 7L; long amount = 6L; String email = "x@y";
    Article a = new Article(); a.setStock(100);
    when(articleRepository.findBySku(sku)).thenReturn(a);
    ShoppingCart existing = new ShoppingCart(); existing.setAmount(4); existing.setArticleSku(sku); existing.setEmail(email);
    when(shoppingCartRepository.findBySkuAndEmail(sku, email)).thenReturn(existing);
    ShoppingCart sc = controller.getOrUpdateShoppingCart(sku, amount, email);
    assertSame(existing, sc);
    assertEquals(10L, sc.getAmount());
  }

  @Test @DisplayName("SC021: getOrUpdateShoppingCart clamps amount to stock")
  void SC021() {
    long sku = 2L; long amount = 10L; String email = "m@e";
    Article a = new Article(); a.setStock(2);
    when(articleRepository.findBySku(sku)).thenReturn(a);
    ShoppingCart existing = new ShoppingCart(); existing.setAmount(0); existing.setArticleSku(sku); existing.setEmail(email);
    when(shoppingCartRepository.findBySkuAndEmail(sku, email)).thenReturn(existing);
    ShoppingCart sc = controller.getOrUpdateShoppingCart(sku, amount, email);
    assertEquals(2L, sc.getAmount());
  }

  // -------- getMaxAmount overloads --------
  @Test @DisplayName("SC022: getMaxAmount(article) min(amount, stock)")
  void SC022() {
    Article a = new Article(); a.setStock(5);
    assertEquals(3L, controller.getMaxAmount(3L, a));
    assertEquals(5L, controller.getMaxAmount(99L, a));
  }

  @Test @DisplayName("SC023: getMaxAmount(shoppingCart) uses shoppingCart.article.stock")
  void SC023() {
    Article a = new Article(); a.setStock(4);
    ShoppingCart sc = spy(new ShoppingCart());
    doReturn(a).when(sc).getArticle();
    assertEquals(4L, controller.getMaxAmount(10L, sc));
    assertEquals(2L, controller.getMaxAmount(2L, sc));
  }

  // -------- More variations to reach 100 tests --------
  // Additional price/discount/total/vat rounding and email variations
  @Test void SC024() { cookieJwt(jwtWithEmail("a1@ex")); when(shoppingCartRepository.getTotalPrice("a1@ex")).thenReturn(10.0); assertEquals("9.30 CHF", controller.getPriceExclVat(request)); }
  @Test void SC025() { cookieJwt(jwtWithEmail("a2@ex")); when(shoppingCartRepository.getTotalPrice("a2@ex")).thenReturn(10.01); assertEquals("9.31 CHF", controller.getPriceExclVat(request)); }
  @Test void SC026() { cookieJwt(jwtWithEmail("a3@ex")); when(shoppingCartRepository.getTotalPrice("a3@ex")).thenReturn(10.02); assertEquals("9.32 CHF", controller.getPriceExclVat(request)); }
  @Test void SC027() { cookieJwt(jwtWithEmail("v1@ex")); when(shoppingCartRepository.getTotalPrice("v1@ex")).thenReturn(10.0); assertEquals("0.70 CHF", controller.getVat(request)); }
  @Test void SC028() { cookieJwt(jwtWithEmail("v2@ex")); when(shoppingCartRepository.getTotalPrice("v2@ex")).thenReturn(10.01); assertEquals("0.70 CHF", controller.getVat(request)); }
  @Test void SC029() { cookieJwt(jwtWithEmail("v3@ex")); when(shoppingCartRepository.getTotalPrice("v3@ex")).thenReturn(10.02); assertEquals("0.70 CHF", controller.getVat(request)); }
  @Test void SC030() { cookieJwt(jwtWithEmail("t1@ex")); when(shoppingCartRepository.getTotalPrice("t1@ex")).thenReturn(10.0); assertEquals("10.00 CHF", controller.getTotal(request)); }
  @Test void SC031() { cookieJwt(jwtWithEmail("t2@ex")); when(shoppingCartRepository.getTotalPrice("t2@ex")).thenReturn(10.01); assertEquals("10.01 CHF", controller.getTotal(request)); }
  @Test void SC032() { cookieJwt(jwtWithEmail("t3@ex")); when(shoppingCartRepository.getTotalPrice("t3@ex")).thenReturn(10.02); assertEquals("10.02 CHF", controller.getTotal(request)); }
  @Test void SC033() { cookieJwt(jwtWithEmail("d1@ex")); when(shoppingCartRepository.getTotalDiscount("d1@ex")).thenReturn(10.0); assertEquals("10.00 CHF", controller.getDiscount(request)); }
  @Test void SC034() { cookieJwt(jwtWithEmail("d2@ex")); when(shoppingCartRepository.getTotalDiscount("d2@ex")).thenReturn(10.01); assertEquals("10.01 CHF", controller.getDiscount(request)); }
  @Test void SC035() { cookieJwt(jwtWithEmail("d3@ex")); when(shoppingCartRepository.getTotalDiscount("d3@ex")).thenReturn(10.02); assertEquals("10.02 CHF", controller.getDiscount(request)); }

  // More targeted rounding
  @Test void SC036() { cookieJwt(jwtWithEmail("r1@ex")); when(shoppingCartRepository.getTotalPrice("r1@ex")).thenReturn(0.004); assertEquals("0.00 CHF", controller.getTotal(request)); }
  @Test void SC037() { cookieJwt(jwtWithEmail("r2@ex")); when(shoppingCartRepository.getTotalPrice("r2@ex")).thenReturn(0.005); assertEquals("0.01 CHF", controller.getTotal(request)); }
  @Test void SC038() { cookieJwt(jwtWithEmail("r3@ex")); when(shoppingCartRepository.getTotalDiscount("r3@ex")).thenReturn(0.004); assertEquals("0.00 CHF", controller.getDiscount(request)); }
  @Test void SC039() { cookieJwt(jwtWithEmail("r4@ex")); when(shoppingCartRepository.getTotalDiscount("r4@ex")).thenReturn(0.005); assertEquals("0.01 CHF", controller.getDiscount(request)); }
  @Test void SC040() { cookieJwt(jwtWithEmail("r5@ex")); when(shoppingCartRepository.getTotalPrice("r5@ex")).thenReturn(14.2857); assertEquals("13.29 CHF", controller.getPriceExclVat(request)); }

  // getShoppingCartEntries variations
  @Test void SC041() { when(authController.extractEmail(request)).thenReturn("e1"); when(shoppingCartRepository.getShoppingCartEntries("e1")).thenReturn(gen(0)); assertEquals(0, controller.getShoppingCartEntries(request).size()); }
  @Test void SC042() { when(authController.extractEmail(request)).thenReturn("e2"); when(shoppingCartRepository.getShoppingCartEntries("e2")).thenReturn(gen(1)); assertEquals(1, controller.getShoppingCartEntries(request).size()); }
  @Test void SC043() { when(authController.extractEmail(request)).thenReturn("e3"); when(shoppingCartRepository.getShoppingCartEntries("e3")).thenReturn(gen(2)); assertEquals(2, controller.getShoppingCartEntries(request).size()); }
  @Test void SC044() { when(authController.extractEmail(request)).thenReturn("e4"); when(shoppingCartRepository.getShoppingCartEntries("e4")).thenReturn(gen(3)); assertEquals(3, controller.getShoppingCartEntries(request).size()); }
  @Test void SC045() { when(authController.extractEmail(request)).thenReturn("e5"); when(shoppingCartRepository.getShoppingCartEntries("e5")).thenReturn(gen(4)); assertEquals(4, controller.getShoppingCartEntries(request).size()); }
  @Test void SC046() { when(authController.extractEmail(request)).thenReturn("e6"); when(shoppingCartRepository.getShoppingCartEntries("e6")).thenReturn(gen(5)); assertEquals(5, controller.getShoppingCartEntries(request).size()); }
  @Test void SC047() { when(authController.extractEmail(request)).thenReturn("e7"); when(shoppingCartRepository.getShoppingCartEntries("e7")).thenReturn(gen(6)); assertEquals(6, controller.getShoppingCartEntries(request).size()); }
  @Test void SC048() { when(authController.extractEmail(request)).thenReturn("e8"); when(shoppingCartRepository.getShoppingCartEntries("e8")).thenReturn(gen(7)); assertEquals(7, controller.getShoppingCartEntries(request).size()); }
  @Test void SC049() { when(authController.extractEmail(request)).thenReturn("e9"); when(shoppingCartRepository.getShoppingCartEntries("e9")).thenReturn(gen(8)); assertEquals(8, controller.getShoppingCartEntries(request).size()); }
  @Test void SC050() { when(authController.extractEmail(request)).thenReturn("e10"); when(shoppingCartRepository.getShoppingCartEntries("e10")).thenReturn(gen(9)); assertEquals(9, controller.getShoppingCartEntries(request).size()); }

  // More permutations of totals
  @Test void SC051() { cookieJwt(jwtWithEmail("mix1@ex")); when(shoppingCartRepository.getTotalPrice("mix1@ex")).thenReturn(19.99); assertEquals("18.59 CHF", controller.getPriceExclVat(request)); }
  @Test void SC052() { cookieJwt(jwtWithEmail("mix2@ex")); when(shoppingCartRepository.getTotalPrice("mix2@ex")).thenReturn(19.99); assertEquals("1.40 CHF", controller.getVat(request)); }
  @Test void SC053() { cookieJwt(jwtWithEmail("mix3@ex")); when(shoppingCartRepository.getTotalDiscount("mix3@ex")).thenReturn(19.99); assertEquals("19.99 CHF", controller.getDiscount(request)); }
  @Test void SC054() { cookieJwt(jwtWithEmail("mix4@ex")); when(shoppingCartRepository.getTotalPrice("mix4@ex")).thenReturn(19.99); assertEquals("19.99 CHF", controller.getTotal(request)); }
  @Test void SC055() { cookieJwt(jwtWithEmail("mix5@ex")); when(shoppingCartRepository.getTotalPrice("mix5@ex")).thenReturn(1.234); assertEquals("1.15 CHF", controller.getPriceExclVat(request)); }
  @Test void SC056() { cookieJwt(jwtWithEmail("mix6@ex")); when(shoppingCartRepository.getTotalPrice("mix6@ex")).thenReturn(1.234); assertEquals("0.09 CHF", controller.getVat(request)); }
  @Test void SC057() { cookieJwt(jwtWithEmail("mix7@ex")); when(shoppingCartRepository.getTotalDiscount("mix7@ex")).thenReturn(1.234); assertEquals("1.23 CHF", controller.getDiscount(request)); }
  @Test void SC058() { cookieJwt(jwtWithEmail("mix8@ex")); when(shoppingCartRepository.getTotalPrice("mix8@ex")).thenReturn(1.234); assertEquals("1.23 CHF", controller.getTotal(request)); }

  // Bad tokens -> null email path for totals
  @Test void SC059() { cookieJwt("a.b"); when(shoppingCartRepository.getTotalPrice(null)).thenReturn(0.0); assertEquals("0.00 CHF", controller.getTotal(request)); }
  @Test void SC060() { cookieJwt("."); when(shoppingCartRepository.getTotalDiscount(null)).thenReturn(0.0); assertEquals("0.00 CHF", controller.getDiscount(request)); }
  @Test void SC061() { cookieJwt(""); when(shoppingCartRepository.getTotalPrice(null)).thenReturn(2.0); assertEquals("2.00 CHF", controller.getTotal(request)); }
  @Test void SC062() { cookieJwt("invalid"); when(shoppingCartRepository.getTotalPrice(null)).thenReturn(1000.0); assertEquals("1000.00 CHF", controller.getTotal(request)); }

  // Additional getShoppingCartEntries variations
  @Test void SC063() { when(authController.extractEmail(request)).thenReturn("E1"); when(shoppingCartRepository.getShoppingCartEntries("E1")).thenReturn(List.of()); assertTrue(controller.getShoppingCartEntries(request).isEmpty()); }
  @Test void SC064() { when(authController.extractEmail(request)).thenReturn("E2"); when(shoppingCartRepository.getShoppingCartEntries("E2")).thenReturn(gen(2)); assertEquals(2, controller.getShoppingCartEntries(request).size()); }
  @Test void SC065() { when(authController.extractEmail(request)).thenReturn("E3"); when(shoppingCartRepository.getShoppingCartEntries("E3")).thenReturn(gen(3)); assertEquals(3, controller.getShoppingCartEntries(request).size()); }
  @Test void SC066() { when(authController.extractEmail(request)).thenReturn("E4"); when(shoppingCartRepository.getShoppingCartEntries("E4")).thenReturn(gen(4)); assertEquals(4, controller.getShoppingCartEntries(request).size()); }
  @Test void SC067() { when(authController.extractEmail(request)).thenReturn("E5"); when(shoppingCartRepository.getShoppingCartEntries("E5")).thenReturn(gen(5)); assertEquals(5, controller.getShoppingCartEntries(request).size()); }
  @Test void SC068() { when(authController.extractEmail(request)).thenReturn("E6"); when(shoppingCartRepository.getShoppingCartEntries("E6")).thenReturn(gen(6)); assertEquals(6, controller.getShoppingCartEntries(request).size()); }
  @Test void SC069() { when(authController.extractEmail(request)).thenReturn("E7"); when(shoppingCartRepository.getShoppingCartEntries("E7")).thenReturn(gen(7)); assertEquals(7, controller.getShoppingCartEntries(request).size()); }
  @Test void SC070() { when(authController.extractEmail(request)).thenReturn("E8"); when(shoppingCartRepository.getShoppingCartEntries("E8")).thenReturn(gen(8)); assertEquals(8, controller.getShoppingCartEntries(request).size()); }

  // More getOrUpdate variations
  @Test void SC071() { Article a=new Article(); a.setStock(0); when(articleRepository.findBySku(1L)).thenReturn(a); when(shoppingCartRepository.findBySkuAndEmail(1L,"z")).thenReturn(null); ShoppingCart sc=controller.getOrUpdateShoppingCart(1L,5L,"z"); assertEquals(0, sc.getAmount()); }
  @Test void SC072() { Article a=new Article(); a.setStock(1); when(articleRepository.findBySku(2L)).thenReturn(a); ShoppingCart ex=new ShoppingCart(); ex.setAmount(1); ex.setArticleSku(2L); ex.setEmail("z"); when(shoppingCartRepository.findBySkuAndEmail(2L,"z")).thenReturn(ex); ShoppingCart sc=controller.getOrUpdateShoppingCart(2L,1L,"z"); assertEquals(2, sc.getAmount()); }
  @Test void SC073() { Article a=new Article(); a.setStock(5); when(articleRepository.findBySku(3L)).thenReturn(a); when(shoppingCartRepository.findBySkuAndEmail(3L,"z")).thenReturn(null); ShoppingCart sc=controller.getOrUpdateShoppingCart(3L,5L,"z"); assertNotNull(sc.getUuid()); }
  @Test void SC074() { Article a=new Article(); a.setStock(5); when(articleRepository.findBySku(4L)).thenReturn(a); ShoppingCart ex=new ShoppingCart(); ex.setAmount(0); ex.setArticleSku(4L); ex.setEmail("z"); when(shoppingCartRepository.findBySkuAndEmail(4L,"z")).thenReturn(ex); ShoppingCart sc=controller.getOrUpdateShoppingCart(4L,0L,"z"); assertEquals(0, sc.getAmount()); }

  // More rounding edge cases for VAT/Total/Discount
  @Test void SC075() { cookieJwt(jwtWithEmail("q1")); when(shoppingCartRepository.getTotalPrice("q1")).thenReturn(3.335); assertEquals("3.34 CHF", controller.getTotal(request)); }
  @Test void SC076() { cookieJwt(jwtWithEmail("q2")); when(shoppingCartRepository.getTotalDiscount("q2")).thenReturn(3.335); assertEquals("3.34 CHF", controller.getDiscount(request)); }
  @Test void SC077() { cookieJwt(jwtWithEmail("q3")); when(shoppingCartRepository.getTotalPrice("q3")).thenReturn(0.01); assertEquals("0.00 CHF", controller.getVat(request)); }
  @Test void SC078() { cookieJwt(jwtWithEmail("q4")); when(shoppingCartRepository.getTotalPrice("q4")).thenReturn(0.02); assertEquals("0.00 CHF", controller.getVat(request)); }
  @Test void SC079() { cookieJwt(jwtWithEmail("q5")); when(shoppingCartRepository.getTotalPrice("q5")).thenReturn(14.29); assertEquals("1.00 CHF", controller.getVat(request)); }

  // Stress variations to reach 100
  @Test void SC080() { when(authController.extractEmail(request)).thenReturn("s1"); when(shoppingCartRepository.getShoppingCartEntries("s1")).thenReturn(gen(10)); assertEquals(10, controller.getShoppingCartEntries(request).size()); }
  @Test void SC081() { when(authController.extractEmail(request)).thenReturn("s2"); when(shoppingCartRepository.getShoppingCartEntries("s2")).thenReturn(gen(11)); assertEquals(11, controller.getShoppingCartEntries(request).size()); }
  @Test void SC082() { when(authController.extractEmail(request)).thenReturn("s3"); when(shoppingCartRepository.getShoppingCartEntries("s3")).thenReturn(gen(12)); assertEquals(12, controller.getShoppingCartEntries(request).size()); }
  @Test void SC083() { when(authController.extractEmail(request)).thenReturn("s4"); when(shoppingCartRepository.getShoppingCartEntries("s4")).thenReturn(gen(13)); assertEquals(13, controller.getShoppingCartEntries(request).size()); }
  @Test void SC084() { when(authController.extractEmail(request)).thenReturn("s5"); when(shoppingCartRepository.getShoppingCartEntries("s5")).thenReturn(gen(14)); assertEquals(14, controller.getShoppingCartEntries(request).size()); }
  @Test void SC085() { cookieJwt(jwtWithEmail("del1")); when(shoppingCartRepository.getTotalPrice("del1")).thenReturn(1.0); assertEquals("0.93 CHF", controller.getPriceExclVat(request)); }
  @Test void SC086() { cookieJwt(jwtWithEmail("del2")); when(shoppingCartRepository.getTotalPrice("del2")).thenReturn(2.0); assertEquals("1.86 CHF", controller.getPriceExclVat(request)); }
  @Test void SC087() { cookieJwt(jwtWithEmail("del3")); when(shoppingCartRepository.getTotalDiscount("del3")).thenReturn(1.23); assertEquals("1.23 CHF", controller.getDiscount(request)); }
  @Test void SC088() { cookieJwt(jwtWithEmail("del4")); when(shoppingCartRepository.getTotalPrice("del4")).thenReturn(3.33); assertEquals("3.33 CHF", controller.getTotal(request)); }
  @Test void SC089() { cookieJwt(jwtWithEmail("del5")); when(shoppingCartRepository.getTotalPrice("del5")).thenReturn(1.999); assertEquals("2.00 CHF", controller.getTotal(request)); }
  @Test void SC090() { cookieJwt(jwtWithEmail("del6")); when(shoppingCartRepository.getTotalDiscount("del6")).thenReturn(0.004); assertEquals("0.00 CHF", controller.getDiscount(request)); }
  @Test void SC091() { cookieJwt(jwtWithEmail("del7")); when(shoppingCartRepository.getTotalDiscount("del7")).thenReturn(0.005); assertEquals("0.01 CHF", controller.getDiscount(request)); }
  @Test void SC092() { cookieJwt(jwtWithEmail("del8")); when(shoppingCartRepository.getTotalPrice("del8")).thenReturn(0.004); assertEquals("0.00 CHF", controller.getTotal(request)); }
  @Test void SC093() { cookieJwt(jwtWithEmail("del9")); when(shoppingCartRepository.getTotalPrice("del9")).thenReturn(0.005); assertEquals("0.01 CHF", controller.getTotal(request)); }
  @Test void SC094() { when(authController.extractEmail(request)).thenReturn("trim"); when(shoppingCartRepository.getShoppingCartEntries("trim")).thenReturn(List.of()); assertEquals(0, controller.getShoppingCartEntries(request).size()); }
  @Test void SC095() { Article a=new Article(); a.setStock(9); assertEquals(9, controller.getMaxAmount(99, a)); }
  @Test void SC096() { Article a=new Article(); a.setStock(9); assertEquals(1, controller.getMaxAmount(1, a)); }
  @Test void SC097() { Article a=new Article(); a.setStock(0); assertEquals(0, controller.getMaxAmount(1, a)); }
  @Test void SC098() { Article a=new Article(); a.setStock(1000000); assertEquals(999, controller.getMaxAmount(999, a)); }
  @Test void SC099() { Article a=new Article(); a.setStock(1000000); assertEquals(1000000, controller.getMaxAmount(1000001, a)); }
  @Test void SC100() { Article a=new Article(); a.setStock(5); ShoppingCart sc=spy(new ShoppingCart()); doReturn(a).when(sc).getArticle(); assertEquals(5, controller.getMaxAmount(10, sc)); }
}
