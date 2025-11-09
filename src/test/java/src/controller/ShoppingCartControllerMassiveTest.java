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
import src.model.ShoppingCart;
import src.repository.ShoppingCartRepository;

class ShoppingCartControllerMassiveTest {

  private ShoppingCartRepository shoppingCartRepository;
  private AuthController authController; // mocked for getShoppingCartEntries
  private HttpServletRequest request;
  private ShoppingCartController controller;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    shoppingCartRepository = mock(ShoppingCartRepository.class);
    authController = mock(AuthController.class);
    request = mock(HttpServletRequest.class);
    controller = new ShoppingCartController(shoppingCartRepository, authController);
  }

  // -------- Helpers --------
  private static String b64(String s) {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(s.getBytes(StandardCharsets.UTF_8));
  }

  private static String jwtWithEmail(String email) {
    String header = b64("{\"alg\":\"none\"}");
    String payload = b64("{\"email\":\"" + email + "\"}");
    return header + "." + payload + ".x"; // signature not verified by JWT.decode
  }

  private void cookies(Cookie... cookies) {
    when(request.getCookies()).thenReturn(cookies);
  }

  private void cookieJwt(String token) {
    cookies(new Cookie("jwt", token));
  }

  private static List<ShoppingCart> gen(int n) {
    List<ShoppingCart> list = new ArrayList<>();
    for (int i = 0; i < n; i++) list.add(new ShoppingCart());
    return list;
  }

  // -------- getShoppingCartEntries --------
  @Test
  @DisplayName("S001: getShoppingCartEntries returns empty when email null")
  void S001() {
    when(authController.extractEmail(request)).thenReturn(null);
    assertTrue(controller.getShoppingCartEntries(request).isEmpty());
    verify(shoppingCartRepository, never()).getShoppingCartEntries(anyString());
  }

  @Test
  @DisplayName("S002: getShoppingCartEntries returns repo result when email present")
  void S002() {
    when(authController.extractEmail(request)).thenReturn("u@example.com");
    List<ShoppingCart> expected = gen(3);
    when(shoppingCartRepository.getShoppingCartEntries("u@example.com")).thenReturn(expected);
    assertSame(expected, controller.getShoppingCartEntries(request));
    verify(shoppingCartRepository).getShoppingCartEntries("u@example.com");
  }

  @Test
  @DisplayName("S003: getShoppingCartEntries propagates empty list from repo")
  void S003() {
    when(authController.extractEmail(request)).thenReturn("e");
    when(shoppingCartRepository.getShoppingCartEntries("e")).thenReturn(List.of());
    assertEquals(0, controller.getShoppingCartEntries(request).size());
  }

  // -------- Price formatting helpers covered by controller methods --------
  // For price-based methods, controller constructs new AuthController internally.
  // We therefore provide a real JWT cookie on request and stub repository returns.

  // getPriceExclVat = total * 0.93, 2 decimals, with " CHF"
  @Test
  @DisplayName("S004: getPriceExclVat with 0 total -> 0.00 CHF")
  void S004() {
    cookieJwt(jwtWithEmail("a@b.c"));
    when(shoppingCartRepository.getTotalPrice("a@b.c")).thenReturn(0.0);
    assertEquals("0.00 CHF", controller.getPriceExclVat(request));
  }

  @Test
  @DisplayName("S005: getPriceExclVat rounds to 2 decimals")
  void S005() {
    cookieJwt(jwtWithEmail("x@y.z"));
    when(shoppingCartRepository.getTotalPrice("x@y.z")).thenReturn(100.0);
    // 100 * 0.93 = 93.00
    assertEquals("93.00 CHF", controller.getPriceExclVat(request));
  }

  @Test
  @DisplayName("S006: getPriceExclVat with fractional total")
  void S006() {
    cookieJwt(jwtWithEmail("e@x.com"));
    when(shoppingCartRepository.getTotalPrice("e@x.com")).thenReturn(12.345);
    // 12.345 * 0.93 = 11.48085 -> 11.48
    assertEquals("11.48 CHF", controller.getPriceExclVat(request));
  }

  @Test
  @DisplayName("S007: getPriceExclVat handles large values")
  void S007() {
    cookieJwt(jwtWithEmail("big@ex.com"));
    when(shoppingCartRepository.getTotalPrice("big@ex.com")).thenReturn(123456.78);
    assertEquals("114814.81 CHF", controller.getPriceExclVat(request));
  }

  @Test
  @DisplayName("S008: getPriceExclVat when JWT email missing -> repo called with null")
  void S008() {
    // malformed token -> extractEmail returns null
    cookieJwt("invalid");
    when(shoppingCartRepository.getTotalPrice(null)).thenReturn(50.0);
    assertEquals("46.50 CHF", controller.getPriceExclVat(request));
  }

  // getVat = total * 0.07
  @Test
  @DisplayName("S009: getVat with 0 total -> 0.00 CHF")
  void S009() {
    cookieJwt(jwtWithEmail("v@t.a"));
    when(shoppingCartRepository.getTotalPrice("v@t.a")).thenReturn(0.0);
    assertEquals("0.00 CHF", controller.getVat(request));
  }

  @Test
  @DisplayName("S010: getVat with 100 total -> 7.00 CHF")
  void S010() {
    cookieJwt(jwtWithEmail("v@t.b"));
    when(shoppingCartRepository.getTotalPrice("v@t.b")).thenReturn(100.0);
    assertEquals("7.00 CHF", controller.getVat(request));
  }

  @Test
  @DisplayName("S011: getVat rounds correctly")
  void S011() {
    cookieJwt(jwtWithEmail("v@t.c"));
    when(shoppingCartRepository.getTotalPrice("v@t.c")).thenReturn(12.345);
    // 12.345 * 0.07 = 0.86415 -> 0.86
    assertEquals("0.86 CHF", controller.getVat(request));
  }

  @Test
  @DisplayName("S012: getVat with large numbers")
  void S012() {
    cookieJwt(jwtWithEmail("large@ex.com"));
    when(shoppingCartRepository.getTotalPrice("large@ex.com")).thenReturn(98765.43);
    assertEquals("6913.58 CHF", controller.getVat(request));
  }

  @Test
  @DisplayName("S013: getVat with null email from bad token")
  void S013() {
    cookieJwt("bad.token");
    when(shoppingCartRepository.getTotalPrice(null)).thenReturn(3.33);
    // 3.33 * 0.07 = 0.2331 -> 0.23
    assertEquals("0.23 CHF", controller.getVat(request));
  }

  // getTotal = total price as formatted
  @Test
  @DisplayName("S014: getTotal with zero")
  void S014() {
    cookieJwt(jwtWithEmail("t@a"));
    when(shoppingCartRepository.getTotalPrice("t@a")).thenReturn(0.0);
    assertEquals("0.00 CHF", controller.getTotal(request));
  }

  @Test
  @DisplayName("S015: getTotal formats two decimals")
  void S015() {
    cookieJwt(jwtWithEmail("t@b"));
    when(shoppingCartRepository.getTotalPrice("t@b")).thenReturn(1.2);
    assertEquals("1.20 CHF", controller.getTotal(request));
  }

  @Test
  @DisplayName("S016: getTotal large")
  void S016() {
    cookieJwt(jwtWithEmail("t@c"));
    when(shoppingCartRepository.getTotalPrice("t@c")).thenReturn(1234.567);
    assertEquals("1234.57 CHF", controller.getTotal(request));
  }

  @Test
  @DisplayName("S017: getTotal null email path")
  void S017() {
    cookieJwt(".");
    when(shoppingCartRepository.getTotalPrice(null)).thenReturn(9.999);
    assertEquals("10.00 CHF", controller.getTotal(request));
  }

  // getDiscount uses getTotalDiscount(email)
  @Test
  @DisplayName("S018: getDiscount zero")
  void S018() {
    cookieJwt(jwtWithEmail("d@a"));
    when(shoppingCartRepository.getTotalDiscount("d@a")).thenReturn(0.0);
    assertEquals("0.00 CHF", controller.getDiscount(request));
  }

  @Test
  @DisplayName("S019: getDiscount rounds")
  void S019() {
    cookieJwt(jwtWithEmail("d@b"));
    when(shoppingCartRepository.getTotalDiscount("d@b")).thenReturn(1.005);
    assertEquals("1.01 CHF", controller.getDiscount(request));
  }

  @Test
  @DisplayName("S020: getDiscount two decimals")
  void S020() {
    cookieJwt(jwtWithEmail("d@c"));
    when(shoppingCartRepository.getTotalDiscount("d@c")).thenReturn(2.5);
    assertEquals("2.50 CHF", controller.getDiscount(request));
  }

  @Test
  @DisplayName("S021: getDiscount null email path")
  void S021() {
    cookieJwt("invalid");
    when(shoppingCartRepository.getTotalDiscount(null)).thenReturn(7.777);
    assertEquals("7.78 CHF", controller.getDiscount(request));
  }

  // -------- Extra scenarios to reach 100 tests and cover branches/edge cases --------
  @Test
  void S022() {
    when(authController.extractEmail(request)).thenReturn("");
    when(shoppingCartRepository.getShoppingCartEntries("")).thenReturn(List.of());
    assertEquals(0, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S023() {
    when(authController.extractEmail(request)).thenReturn("user@EXAMPLE.COM");
    when(shoppingCartRepository.getShoppingCartEntries("user@EXAMPLE.COM")).thenReturn(gen(1));
    assertEquals(1, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S024() {
    when(authController.extractEmail(request)).thenReturn(" ");
    when(shoppingCartRepository.getShoppingCartEntries(" ")).thenReturn(gen(2));
    assertEquals(2, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S025() {
    when(authController.extractEmail(request)).thenReturn("null@example.com");
    when(shoppingCartRepository.getShoppingCartEntries("null@example.com"))
        .thenReturn(new ArrayList<>());
    assertTrue(controller.getShoppingCartEntries(request).isEmpty());
  }

  // Many small variations for price-based methods using different emails/values
  @Test
  void S026() {
    cookieJwt(jwtWithEmail("a1@ex.com"));
    when(shoppingCartRepository.getTotalPrice("a1@ex.com")).thenReturn(10.0);
    assertEquals("9.30 CHF", controller.getPriceExclVat(request));
  }

  @Test
  void S027() {
    cookieJwt(jwtWithEmail("a2@ex.com"));
    when(shoppingCartRepository.getTotalPrice("a2@ex.com")).thenReturn(10.01);
    assertEquals("9.31 CHF", controller.getPriceExclVat(request));
  }

  @Test
  void S028() {
    cookieJwt(jwtWithEmail("a3@ex.com"));
    when(shoppingCartRepository.getTotalPrice("a3@ex.com")).thenReturn(10.02);
    assertEquals("9.32 CHF", controller.getPriceExclVat(request));
  }

  @Test
  void S029() {
    cookieJwt(jwtWithEmail("a4@ex.com"));
    when(shoppingCartRepository.getTotalPrice("a4@ex.com")).thenReturn(1.0);
    assertEquals("0.93 CHF", controller.getPriceExclVat(request));
  }

  @Test
  void S030() {
    cookieJwt(jwtWithEmail("a5@ex.com"));
    when(shoppingCartRepository.getTotalPrice("a5@ex.com")).thenReturn(1.01);
    assertEquals("0.94 CHF", controller.getPriceExclVat(request));
  }

  @Test
  void S031() {
    cookieJwt(jwtWithEmail("a6@ex.com"));
    when(shoppingCartRepository.getTotalPrice("a6@ex.com")).thenReturn(1.02);
    assertEquals("0.95 CHF", controller.getPriceExclVat(request));
  }

  @Test
  void S032() {
    cookieJwt(jwtWithEmail("v1@ex.com"));
    when(shoppingCartRepository.getTotalPrice("v1@ex.com")).thenReturn(10.0);
    assertEquals("0.70 CHF", controller.getVat(request));
  }

  @Test
  void S033() {
    cookieJwt(jwtWithEmail("v2@ex.com"));
    when(shoppingCartRepository.getTotalPrice("v2@ex.com")).thenReturn(10.01);
    assertEquals("0.70 CHF", controller.getVat(request));
  }

  @Test
  void S034() {
    cookieJwt(jwtWithEmail("v3@ex.com"));
    when(shoppingCartRepository.getTotalPrice("v3@ex.com")).thenReturn(10.02);
    assertEquals("0.70 CHF", controller.getVat(request));
  }

  @Test
  void S035() {
    cookieJwt(jwtWithEmail("t1@ex.com"));
    when(shoppingCartRepository.getTotalPrice("t1@ex.com")).thenReturn(10.0);
    assertEquals("10.00 CHF", controller.getTotal(request));
  }

  @Test
  void S036() {
    cookieJwt(jwtWithEmail("t2@ex.com"));
    when(shoppingCartRepository.getTotalPrice("t2@ex.com")).thenReturn(10.01);
    assertEquals("10.01 CHF", controller.getTotal(request));
  }

  @Test
  void S037() {
    cookieJwt(jwtWithEmail("t3@ex.com"));
    when(shoppingCartRepository.getTotalPrice("t3@ex.com")).thenReturn(10.02);
    assertEquals("10.02 CHF", controller.getTotal(request));
  }

  @Test
  void S038() {
    cookieJwt(jwtWithEmail("d1@ex.com"));
    when(shoppingCartRepository.getTotalDiscount("d1@ex.com")).thenReturn(10.0);
    assertEquals("10.00 CHF", controller.getDiscount(request));
  }

  @Test
  void S039() {
    cookieJwt(jwtWithEmail("d2@ex.com"));
    when(shoppingCartRepository.getTotalDiscount("d2@ex.com")).thenReturn(10.01);
    assertEquals("10.01 CHF", controller.getDiscount(request));
  }

  @Test
  void S040() {
    cookieJwt(jwtWithEmail("d3@ex.com"));
    when(shoppingCartRepository.getTotalDiscount("d3@ex.com")).thenReturn(10.02);
    assertEquals("10.02 CHF", controller.getDiscount(request));
  }

  // More targeted rounding/edge cases
  @Test
  void S041() {
    cookieJwt(jwtWithEmail("r1@ex.com"));
    when(shoppingCartRepository.getTotalPrice("r1@ex.com")).thenReturn(0.004);
    assertEquals("0.00 CHF", controller.getTotal(request));
  }

  @Test
  void S042() {
    cookieJwt(jwtWithEmail("r2@ex.com"));
    when(shoppingCartRepository.getTotalPrice("r2@ex.com")).thenReturn(0.005);
    assertEquals("0.01 CHF", controller.getTotal(request));
  }

  @Test
  void S043() {
    cookieJwt(jwtWithEmail("r3@ex.com"));
    when(shoppingCartRepository.getTotalDiscount("r3@ex.com")).thenReturn(0.004);
    assertEquals("0.00 CHF", controller.getDiscount(request));
  }

  @Test
  void S044() {
    cookieJwt(jwtWithEmail("r4@ex.com"));
    when(shoppingCartRepository.getTotalDiscount("r4@ex.com")).thenReturn(0.005);
    assertEquals("0.01 CHF", controller.getDiscount(request));
  }

  @Test
  void S045() {
    cookieJwt(jwtWithEmail("r5@ex.com"));
    when(shoppingCartRepository.getTotalPrice("r5@ex.com")).thenReturn(14.2857);
    assertEquals("13.29 CHF", controller.getPriceExclVat(request));
  }

  // Null and whitespace emails through JWT
  @Test
  void S046() {
    cookieJwt(jwtWithEmail(""));
    when(shoppingCartRepository.getTotalPrice("")).thenReturn(1.0);
    assertEquals("0.93 CHF", controller.getPriceExclVat(request));
  }

  @Test
  void S047() {
    cookieJwt(jwtWithEmail(" "));
    when(shoppingCartRepository.getTotalPrice(" ")).thenReturn(2.0);
    assertEquals("1.86 CHF", controller.getPriceExclVat(request));
  }

  @Test
  void S048() {
    cookieJwt(jwtWithEmail("user+tag@example.com"));
    when(shoppingCartRepository.getTotalPrice("user+tag@example.com")).thenReturn(2.0);
    assertEquals("1.86 CHF", controller.getPriceExclVat(request));
  }

  @Test
  void S049() {
    cookieJwt(jwtWithEmail("ümlaut@example.com"));
    when(shoppingCartRepository.getTotalDiscount("ümlaut@example.com")).thenReturn(1.23);
    assertEquals("1.23 CHF", controller.getDiscount(request));
  }

  @Test
  void S050() {
    cookieJwt(jwtWithEmail("dot.name@example.com"));
    when(shoppingCartRepository.getTotalPrice("dot.name@example.com")).thenReturn(3.33);
    assertEquals("3.33 CHF", controller.getTotal(request));
  }

  // Many getShoppingCartEntries variations to increase count and branch coverage
  @Test
  void S051() {
    when(authController.extractEmail(request)).thenReturn("e1");
    when(shoppingCartRepository.getShoppingCartEntries("e1")).thenReturn(gen(0));
    assertEquals(0, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S052() {
    when(authController.extractEmail(request)).thenReturn("e2");
    when(shoppingCartRepository.getShoppingCartEntries("e2")).thenReturn(gen(1));
    assertEquals(1, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S053() {
    when(authController.extractEmail(request)).thenReturn("e3");
    when(shoppingCartRepository.getShoppingCartEntries("e3")).thenReturn(gen(2));
    assertEquals(2, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S054() {
    when(authController.extractEmail(request)).thenReturn("e4");
    when(shoppingCartRepository.getShoppingCartEntries("e4")).thenReturn(gen(3));
    assertEquals(3, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S055() {
    when(authController.extractEmail(request)).thenReturn("e5");
    when(shoppingCartRepository.getShoppingCartEntries("e5")).thenReturn(gen(4));
    assertEquals(4, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S056() {
    when(authController.extractEmail(request)).thenReturn("e6");
    when(shoppingCartRepository.getShoppingCartEntries("e6")).thenReturn(gen(5));
    assertEquals(5, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S057() {
    when(authController.extractEmail(request)).thenReturn("e7");
    when(shoppingCartRepository.getShoppingCartEntries("e7")).thenReturn(gen(6));
    assertEquals(6, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S058() {
    when(authController.extractEmail(request)).thenReturn("e8");
    when(shoppingCartRepository.getShoppingCartEntries("e8")).thenReturn(gen(7));
    assertEquals(7, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S059() {
    when(authController.extractEmail(request)).thenReturn("e9");
    when(shoppingCartRepository.getShoppingCartEntries("e9")).thenReturn(gen(8));
    assertEquals(8, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S060() {
    when(authController.extractEmail(request)).thenReturn("e10");
    when(shoppingCartRepository.getShoppingCartEntries("e10")).thenReturn(gen(9));
    assertEquals(9, controller.getShoppingCartEntries(request).size());
  }

  // Extra VAT/Total/Discount permutations
  @Test
  void S061() {
    cookieJwt(jwtWithEmail("vv1@ex.com"));
    when(shoppingCartRepository.getTotalPrice("vv1@ex.com")).thenReturn(0.01);
    assertEquals("0.00 CHF", controller.getVat(request));
  }

  @Test
  void S062() {
    cookieJwt(jwtWithEmail("vv2@ex.com"));
    when(shoppingCartRepository.getTotalPrice("vv2@ex.com")).thenReturn(0.02);
    assertEquals("0.00 CHF", controller.getVat(request));
  }

  @Test
  void S063() {
    cookieJwt(jwtWithEmail("vv3@ex.com"));
    when(shoppingCartRepository.getTotalPrice("vv3@ex.com")).thenReturn(14.29);
    assertEquals("1.00 CHF", controller.getVat(request));
  }

  @Test
  void S064() {
    cookieJwt(jwtWithEmail("tt1@ex.com"));
    when(shoppingCartRepository.getTotalPrice("tt1@ex.com")).thenReturn(0.004);
    assertEquals("0.00 CHF", controller.getTotal(request));
  }

  @Test
  void S065() {
    cookieJwt(jwtWithEmail("tt2@ex.com"));
    when(shoppingCartRepository.getTotalPrice("tt2@ex.com")).thenReturn(0.005);
    assertEquals("0.01 CHF", controller.getTotal(request));
  }

  @Test
  void S066() {
    cookieJwt(jwtWithEmail("dd1@ex.com"));
    when(shoppingCartRepository.getTotalDiscount("dd1@ex.com")).thenReturn(0.004);
    assertEquals("0.00 CHF", controller.getDiscount(request));
  }

  @Test
  void S067() {
    cookieJwt(jwtWithEmail("dd2@ex.com"));
    when(shoppingCartRepository.getTotalDiscount("dd2@ex.com")).thenReturn(0.005);
    assertEquals("0.01 CHF", controller.getDiscount(request));
  }

  @Test
  void S068() {
    cookieJwt(jwtWithEmail("ex@ex.com"));
    when(shoppingCartRepository.getTotalPrice("ex@ex.com")).thenReturn(1.999);
    assertEquals("2.00 CHF", controller.getTotal(request));
  }

  @Test
  void S069() {
    cookieJwt(jwtWithEmail("q@w.e"));
    when(shoppingCartRepository.getTotalPrice("q@w.e")).thenReturn(3.335);
    assertEquals("3.34 CHF", controller.getTotal(request));
  }

  @Test
  void S070() {
    cookieJwt(jwtWithEmail("q@w.f"));
    when(shoppingCartRepository.getTotalDiscount("q@w.f")).thenReturn(3.335);
    assertEquals("3.34 CHF", controller.getDiscount(request));
  }

  // More entries list size variations
  @Test
  void S071() {
    when(authController.extractEmail(request)).thenReturn("e11");
    when(shoppingCartRepository.getShoppingCartEntries("e11")).thenReturn(gen(10));
    assertEquals(10, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S072() {
    when(authController.extractEmail(request)).thenReturn("e12");
    when(shoppingCartRepository.getShoppingCartEntries("e12")).thenReturn(gen(11));
    assertEquals(11, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S073() {
    when(authController.extractEmail(request)).thenReturn("e13");
    when(shoppingCartRepository.getShoppingCartEntries("e13")).thenReturn(gen(12));
    assertEquals(12, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S074() {
    when(authController.extractEmail(request)).thenReturn("e14");
    when(shoppingCartRepository.getShoppingCartEntries("e14")).thenReturn(gen(13));
    assertEquals(13, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S075() {
    when(authController.extractEmail(request)).thenReturn("e15");
    when(shoppingCartRepository.getShoppingCartEntries("e15")).thenReturn(gen(14));
    assertEquals(14, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S076() {
    when(authController.extractEmail(request)).thenReturn("e16");
    when(shoppingCartRepository.getShoppingCartEntries("e16")).thenReturn(gen(15));
    assertEquals(15, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S077() {
    when(authController.extractEmail(request)).thenReturn("e17");
    when(shoppingCartRepository.getShoppingCartEntries("e17")).thenReturn(gen(16));
    assertEquals(16, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S078() {
    when(authController.extractEmail(request)).thenReturn("e18");
    when(shoppingCartRepository.getShoppingCartEntries("e18")).thenReturn(gen(17));
    assertEquals(17, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S079() {
    when(authController.extractEmail(request)).thenReturn("e19");
    when(shoppingCartRepository.getShoppingCartEntries("e19")).thenReturn(gen(18));
    assertEquals(18, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S080() {
    when(authController.extractEmail(request)).thenReturn("e20");
    when(shoppingCartRepository.getShoppingCartEntries("e20")).thenReturn(gen(19));
    assertEquals(19, controller.getShoppingCartEntries(request).size());
  }

  // More price permutations
  @Test
  void S081() {
    cookieJwt(jwtWithEmail("mix1@ex.com"));
    when(shoppingCartRepository.getTotalPrice("mix1@ex.com")).thenReturn(19.99);
    assertEquals("18.59 CHF", controller.getPriceExclVat(request));
  }

  @Test
  void S082() {
    cookieJwt(jwtWithEmail("mix2@ex.com"));
    when(shoppingCartRepository.getTotalPrice("mix2@ex.com")).thenReturn(19.99);
    assertEquals("1.40 CHF", controller.getVat(request));
  }

  @Test
  void S083() {
    cookieJwt(jwtWithEmail("mix3@ex.com"));
    when(shoppingCartRepository.getTotalDiscount("mix3@ex.com")).thenReturn(19.99);
    assertEquals("19.99 CHF", controller.getDiscount(request));
  }

  @Test
  void S084() {
    cookieJwt(jwtWithEmail("mix4@ex.com"));
    when(shoppingCartRepository.getTotalPrice("mix4@ex.com")).thenReturn(19.99);
    assertEquals("19.99 CHF", controller.getTotal(request));
  }

  @Test
  void S085() {
    cookieJwt(jwtWithEmail("mix5@ex.com"));
    when(shoppingCartRepository.getTotalPrice("mix5@ex.com")).thenReturn(1.234);
    assertEquals("1.15 CHF", controller.getPriceExclVat(request));
  }

  @Test
  void S086() {
    cookieJwt(jwtWithEmail("mix6@ex.com"));
    when(shoppingCartRepository.getTotalPrice("mix6@ex.com")).thenReturn(1.234);
    assertEquals("0.09 CHF", controller.getVat(request));
  }

  @Test
  void S087() {
    cookieJwt(jwtWithEmail("mix7@ex.com"));
    when(shoppingCartRepository.getTotalDiscount("mix7@ex.com")).thenReturn(1.234);
    assertEquals("1.23 CHF", controller.getDiscount(request));
  }

  @Test
  void S088() {
    cookieJwt(jwtWithEmail("mix8@ex.com"));
    when(shoppingCartRepository.getTotalPrice("mix8@ex.com")).thenReturn(1.234);
    assertEquals("1.23 CHF", controller.getTotal(request));
  }

  // Bad/malformed tokens -> null email path
  @Test
  void S089() {
    cookieJwt("a.b");
    when(shoppingCartRepository.getTotalPrice(null)).thenReturn(0.0);
    assertEquals("0.00 CHF", controller.getTotal(request));
  }

  @Test
  void S090() {
    cookieJwt(".");
    when(shoppingCartRepository.getTotalDiscount(null)).thenReturn(0.0);
    assertEquals("0.00 CHF", controller.getDiscount(request));
  }

  @Test
  void S091() {
    cookieJwt("");
    when(shoppingCartRepository.getTotalPrice(null)).thenReturn(2.0);
    assertEquals("2.00 CHF", controller.getTotal(request));
  }

  @Test
  void S092() {
    cookieJwt("invalid");
    when(shoppingCartRepository.getTotalPrice(null)).thenReturn(1000.0);
    assertEquals("1000.00 CHF", controller.getTotal(request));
  }

  // Additional getShoppingCartEntries variations
  @Test
  void S093() {
    when(authController.extractEmail(request)).thenReturn("E1");
    when(shoppingCartRepository.getShoppingCartEntries("E1")).thenReturn(List.of());
    assertTrue(controller.getShoppingCartEntries(request).isEmpty());
  }

  @Test
  void S094() {
    when(authController.extractEmail(request)).thenReturn("E2");
    when(shoppingCartRepository.getShoppingCartEntries("E2")).thenReturn(gen(2));
    assertEquals(2, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S095() {
    when(authController.extractEmail(request)).thenReturn("E3");
    when(shoppingCartRepository.getShoppingCartEntries("E3")).thenReturn(gen(3));
    assertEquals(3, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S096() {
    when(authController.extractEmail(request)).thenReturn("E4");
    when(shoppingCartRepository.getShoppingCartEntries("E4")).thenReturn(gen(4));
    assertEquals(4, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S097() {
    when(authController.extractEmail(request)).thenReturn("E5");
    when(shoppingCartRepository.getShoppingCartEntries("E5")).thenReturn(gen(5));
    assertEquals(5, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S098() {
    when(authController.extractEmail(request)).thenReturn("E6");
    when(shoppingCartRepository.getShoppingCartEntries("E6")).thenReturn(gen(6));
    assertEquals(6, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S099() {
    when(authController.extractEmail(request)).thenReturn("E7");
    when(shoppingCartRepository.getShoppingCartEntries("E7")).thenReturn(gen(7));
    assertEquals(7, controller.getShoppingCartEntries(request).size());
  }

  @Test
  void S100() {
    when(authController.extractEmail(request)).thenReturn("E8");
    when(shoppingCartRepository.getShoppingCartEntries("E8")).thenReturn(gen(8));
    assertEquals(8, controller.getShoppingCartEntries(request).size());
  }
}
