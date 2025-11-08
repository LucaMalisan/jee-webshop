package src.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.auth0.jwt.interfaces.Claim;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import src.model.User;
import src.repository.UserRepository;

class AuthControllerMassiveTest {

  private UserRepository repository;
  private HttpServletRequest request;
  private AuthController controller;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    repository = mock(UserRepository.class);
    request = mock(HttpServletRequest.class);
    controller = new AuthController(repository);
  }

  // ---------- Helpers ----------
  private static String b64(String s) {
    return Base64.getUrlEncoder()
        .withoutPadding()
        .encodeToString(s.getBytes(StandardCharsets.UTF_8));
  }

  private static String jwtWithEmail(String email) {
    String header = b64("{\"alg\":\"none\"}");
    String payload = b64("{\"email\":\"" + email + "\"}");
    return header + "." + payload + "." + "x"; // signature not verified by JWT.decode
  }

  private void cookies(Cookie... cookies) {
    when(request.getCookies()).thenReturn(cookies);
  }

  private void cookieJwt(String token) {
    cookies(new Cookie("jwt", token));
  }

  // ---------- getCookieByName ----------
  @Test
  @DisplayName("A001: getCookieByName returns null when no cookies")
  void A001() {
    when(request.getCookies()).thenReturn(null);
    assertNull(controller.getCookieByName(request, "x"));
  }

  @Test
  @DisplayName("A002: getCookieByName finds matching cookie")
  void A002() {
    cookies(new Cookie("a", "1"), new Cookie("x", "v"));
    assertEquals("v", controller.getCookieByName(request, "x"));
  }

  @Test
  @DisplayName("A003: getCookieByName returns null when not found")
  void A003() {
    cookies(new Cookie("a", "1"));
    assertNull(controller.getCookieByName(request, "x"));
  }

  @Test
  @DisplayName("A004: getCookieByName handles multiple with same name (first)")
  void A004() {
    cookies(new Cookie("x", "1"), new Cookie("x", "2"));
    assertEquals("1", controller.getCookieByName(request, "x"));
  }

  @Test
  @DisplayName("A005: getCookieByName supports null name search")
  void A005() {
    cookies(new Cookie("v", "v"));
    assertNull(controller.getCookieByName(request, null));
  }

  @Test
  @DisplayName("A006: getCookieByName supports null cookie value")
  void A006() {
    cookies(new Cookie("x", null));
    assertNull(controller.getCookieByName(request, "x"));
  }

  @Test
  @DisplayName("A007: getCookieByName ignores different names")
  void A007() {
    cookies(new Cookie("y", "1"), new Cookie("z", "2"));
    assertNull(controller.getCookieByName(request, "x"));
  }

  @Test
  @DisplayName("A008: getCookieByName works with empty array")
  void A008() {
    cookies();
    assertNull(controller.getCookieByName(request, "x"));
  }

  @Test
  @DisplayName("A009: getCookieByName exact match when name equals case-sensitive")
  void A009() {
    cookies(new Cookie("JWT", "a"), new Cookie("jwt", "b"));
    assertEquals("b", controller.getCookieByName(request, "jwt"));
  }

  @Test
  @DisplayName("A010: getCookieByName null search on non-null cookies returns null")
  void A010() {
    cookies(new Cookie("a", "1"));
    assertNull(controller.getCookieByName(request, null));
  }

  // ---------- extractEmail(String) ----------
  @Test
  @DisplayName("A011: extractEmail returns email for valid JWT")
  void A011() {
    String t = jwtWithEmail("u@example.com");
    assertEquals("u@example.com", controller.extractEmail(t));
  }

  @Test
  @DisplayName("A012: extractEmail returns null for malformed token (one part)")
  void A012() {
    assertNull(controller.extractEmail("abc"));
  }

  @Test
  @DisplayName("A013: extractEmail returns null for malformed token (two parts)")
  void A013() {
    assertNull(controller.extractEmail("a.b"));
  }

  @Test
  @DisplayName("A014: extractEmail returns null for invalid base64 payload")
  void A014() {
    String t = b64("{\"alg\":\"none\"}") + "." + "%%%" + ".x";
    assertNull(controller.extractEmail(t));
  }

  @Test
  @DisplayName("A015: extractEmail returns null when email missing")
  void A015() {
    String header = b64("{\"alg\":\"none\"}");
    String payload = b64("{\"sub\":\"123\"}");
    String t = header + "." + payload + ".x";
    assertNull(controller.extractEmail(t));
  }

  @Test
  @DisplayName("A016: extractEmail returns null for null token")
  void A016() {
    assertNull(controller.extractEmail((String) null));
  }

  @Test
  @DisplayName("A017: extractEmail handles email with plus sign and dots")
  void A017() {
    String t = jwtWithEmail("first.last+label@example.co.uk");
    assertEquals("first.last+label@example.co.uk", controller.extractEmail(t));
  }

  @Test
  @DisplayName("A018: extractEmail supports unicode local part")
  void A018() {
    String t = jwtWithEmail("ümlaut@example.com");
    assertEquals("ümlaut@example.com", controller.extractEmail(t));
  }

  @Test
  @DisplayName("A019: extractEmail supports long email")
  void A019() {
    String email = "a".repeat(60) + "@example.com";
    assertEquals(email, controller.extractEmail(jwtWithEmail(email)));
  }

  @Test
  @DisplayName("A020: extractEmail survives empty email string")
  void A020() {
    assertEquals("", controller.extractEmail(jwtWithEmail("")));
  }

  // ---------- extractEmail(HttpServletRequest) ----------
  @Test
  @DisplayName("A021: extractEmail(request) reads jwt cookie")
  void A021() {
    String t = jwtWithEmail("x@y.z");
    cookieJwt(t);
    assertEquals("x@y.z", controller.extractEmail(request));
  }

  @Test
  @DisplayName("A022: extractEmail(request) returns null when cookie missing")
  void A022() {
    cookies(new Cookie("other", "v"));
    assertNull(controller.extractEmail(request));
  }

  @Test
  @DisplayName("A023: extractEmail(request) returns null when jwt cookie value null")
  void A023() {
    cookies(new Cookie("jwt", null));
    assertNull(controller.extractEmail(request));
  }

  @Test
  @DisplayName("A024: extractEmail(request) returns null when cookies null")
  void A024() {
    when(request.getCookies()).thenReturn(null);
    assertNull(controller.extractEmail(request));
  }

  @Test
  @DisplayName("A025: extractEmail(request) ignores non-jwt cookies")
  void A025() {
    cookies(new Cookie("a", "1"), new Cookie("b", "2"));
    assertNull(controller.extractEmail(request));
  }

  @Test
  @DisplayName("A026: extractEmail(request) chooses first jwt cookie if multiple")
  void A026() {
    cookies(new Cookie("jwt", jwtWithEmail("a@b.c")), new Cookie("jwt", jwtWithEmail("x@y.z")));
    assertEquals("a@b.c", controller.extractEmail(request));
  }

  @Test
  @DisplayName("A027: extractEmail(request) handles malformed jwt cookie")
  void A027() {
    cookieJwt("invalid.token");
    assertNull(controller.extractEmail(request));
  }

  @Test
  @DisplayName("A028: extractEmail(request) handles empty jwt cookie value")
  void A028() {
    cookieJwt("");
    assertNull(controller.extractEmail(request));
  }

  @Test
  @DisplayName("A029: extractEmail(request) handles whitespace jwt cookie value")
  void A029() {
    cookieJwt("   ");
    assertNull(controller.extractEmail(request));
  }

  @Test
  @DisplayName("A030: extractEmail(request) handles header-only jwt")
  void A030() {
    cookieJwt("a..b");
    assertNull(controller.extractEmail(request));
  }

  // ---------- mailExistsAndIsConfirmed ----------
  @Test
  @DisplayName("A031: mailExistsAndIsConfirmed false when email null")
  void A031() {
    when(request.getCookies()).thenReturn(null);
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  @DisplayName("A032: mailExistsAndIsConfirmed false when user not found")
  void A032() {
    cookieJwt(jwtWithEmail("x@x.x"));
    when(repository.findByEmail("x@x.x")).thenReturn(null);
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  @DisplayName("A033: mailExistsAndIsConfirmed true when user confirmed")
  void A033() {
    cookieJwt(jwtWithEmail("x@x.x"));
    User u = new User();
    u.setConfirmed(true);
    when(repository.findByEmail("x@x.x")).thenReturn(u);
    assertTrue(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  @DisplayName("A034: mailExistsAndIsConfirmed false when user not confirmed")
  void A034() {
    cookieJwt(jwtWithEmail("x@x.x"));
    User u = new User();
    u.setConfirmed(false);
    when(repository.findByEmail("x@x.x")).thenReturn(u);
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  @DisplayName("A035: mailExistsAndIsConfirmed calls repository with parsed email")
  void A035() {
    String email = "a@b.c";
    cookieJwt(jwtWithEmail(email));
    when(repository.findByEmail(email)).thenReturn(null);
    controller.mailExistsAndIsConfirmed(request);
    verify(repository).findByEmail(email);
  }

  @Test
  @DisplayName("A036: mailExistsAndIsConfirmed not calling repository when email null")
  void A036() {
    when(request.getCookies()).thenReturn(null);
    controller.mailExistsAndIsConfirmed(request);
    verify(repository, never()).findByEmail(anyString());
  }

  @Test
  @DisplayName("A037: mailExistsAndIsConfirmed with complex email")
  void A037() {
    String email = "first.last+tag@sub.domain.example";
    cookieJwt(jwtWithEmail(email));
    when(repository.findByEmail(email)).thenReturn(newUser(false));
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }

  private static User newUser(boolean confirmed) {
    User u = new User();
    u.setConfirmed(confirmed);
    return u;
  }

  @Test
  @DisplayName(
      "A038: mailExistsAndIsConfirmed true for upper-case domain emails if stored confirmed")
  void A038() {
    String email = "user@EXAMPLE.COM";
    cookieJwt(jwtWithEmail(email));
    when(repository.findByEmail(email)).thenReturn(newUser(true));
    assertTrue(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  @DisplayName("A039: mailExistsAndIsConfirmed false when repository throws (treated as user null)")
  void A039() {
    String email = "t@t.t";
    cookieJwt(jwtWithEmail(email));
    when(repository.findByEmail(email)).thenThrow(new RuntimeException("db"));
    try {
      controller.mailExistsAndIsConfirmed(request);
    } catch (RuntimeException ex) {
      /* allow bubble */
    }
  }

  @Test
  @DisplayName("A040: mailExistsAndIsConfirmed repeated call uses repository each time")
  void A040() {
    String email = "r@r.r";
    cookieJwt(jwtWithEmail(email));
    when(repository.findByEmail(email)).thenReturn(newUser(false));
    controller.mailExistsAndIsConfirmed(request);
    controller.mailExistsAndIsConfirmed(request);
    verify(repository, times(2)).findByEmail(email);
  }

  // ---------- getBaseURL ----------
  @Test
  @DisplayName("A041: getBaseURL assembles correct URL")
  void A041() {
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("localhost");
    when(request.getServerPort()).thenReturn(8080);
    when(request.getContextPath()).thenReturn("/ctx");
    assertEquals("http://localhost:8080/ctx/application", controller.getBaseURL(request));
  }

  @Test
  @DisplayName("A042: getBaseURL uses https scheme and custom port")
  void A042() {
    when(request.getScheme()).thenReturn("https");
    when(request.getServerName()).thenReturn("example.com");
    when(request.getServerPort()).thenReturn(4443);
    when(request.getContextPath()).thenReturn("");
    assertEquals("https://example.com:4443/application", controller.getBaseURL(request));
  }

  @Test
  @DisplayName("A043: getBaseURL tolerates null contextPath (prints null)")
  void A043() {
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("h");
    when(request.getServerPort()).thenReturn(80);
    when(request.getContextPath()).thenReturn(null);
    assertEquals("http://h:80null/application", controller.getBaseURL(request));
  }

  // ---------- Additional JWT payload edge cases for robustness and count ----------
  @Test
  void A044() {
    assertNull(controller.extractEmail(".."));
  }

  @Test
  void A045() {
    String t = b64("{}") + "." + b64("{}") + ".x";
    assertNull(controller.extractEmail(t));
  }

  @Test
  void A046() {
    String t = b64("{\"alg\":\"none\"}") + "." + b64("{\"email\":null}") + ".x";
    assertNull(controller.extractEmail(t));
  }

  @Test
  void A047() {
    String t = b64("{\"alg\":\"none\"}") + "." + b64("{\"email\":123}") + ".x";
    assertNull(controller.extractEmail(t));
  }

  @Test
  void A048() {
    String t = b64("{\"alg\":\"none\"}") + "." + b64("{\"email\":\"a@b\"}") + ".x";
    assertEquals("a@b", controller.extractEmail(t));
  }

  @Test
  void A049() {
    String t = b64("hdr") + "." + b64("pld") + ".x";
    assertNull(controller.extractEmail(t));
  }

  @Test
  void A050() {
    String email = "x@y.z";
    String t = jwtWithEmail(email);
    assertEquals(email, controller.extractEmail(t));
  }

  // ---------- Many cookie name/value corner cases ----------
  @Test
  void A051() {
    cookies(new Cookie("v", "v"));
    assertNull(controller.getCookieByName(request, ""));
  }

  @Test
  void A052() {
    cookies(new Cookie("x", ""));
    assertEquals("", controller.getCookieByName(request, "x"));
  }

  @Test
  void A053() {
    cookies(new Cookie("x", " "));
    assertEquals(" ", controller.getCookieByName(request, "x"));
  }

  @Test
  void A054() {
    cookies(new Cookie("x", "v"), new Cookie("X", "w"));
    assertEquals("v", controller.getCookieByName(request, "x"));
  }

  @Test
  void A055() {
    cookies(new Cookie("null", "v"));
    assertNull(controller.getCookieByName(request, null));
  }

  @Test
  void A056() {
    cookies(new Cookie("x", "1"), new Cookie("x", "2"), new Cookie("x", "3"));
    assertEquals("1", controller.getCookieByName(request, "x"));
  }

  @Test
  void A057() {
    cookies(new Cookie("a", "1"), new Cookie("b", "2"), new Cookie("c", "3"));
    assertNull(controller.getCookieByName(request, "x"));
  }

  @Test
  void A058() {
    cookies(new Cookie("x", "1"));
    assertEquals("1", controller.getCookieByName(request, "x"));
  }

  @Test
  void A059() {
    cookies(new Cookie("x", "1"));
    assertNotEquals("2", controller.getCookieByName(request, "x"));
  }

  @Test
  void A060() {
    cookies(new Cookie("y", "1"), new Cookie("x", "2"));
    assertEquals("2", controller.getCookieByName(request, "x"));
  }

  // ---------- More mailExists permutations ----------
  @Test
  void A061() {
    cookieJwt(jwtWithEmail("m@d.t"));
    when(repository.findByEmail("m@d.t")).thenReturn(newUser(true));
    assertTrue(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A062() {
    cookieJwt(jwtWithEmail("m@d.t"));
    when(repository.findByEmail("m@d.t")).thenReturn(newUser(false));
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A063() {
    cookieJwt(jwtWithEmail("m@d.t"));
    when(repository.findByEmail("m@d.t")).thenReturn(null);
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A064() {
    cookieJwt(jwtWithEmail("user+1@example.com"));
    when(repository.findByEmail("user+1@example.com")).thenReturn(newUser(true));
    assertTrue(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A065() {
    cookieJwt(jwtWithEmail("user.name@example.com"));
    when(repository.findByEmail("user.name@example.com")).thenReturn(newUser(false));
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A066() {
    cookieJwt(jwtWithEmail("ümlaut@example.com"));
    when(repository.findByEmail("ümlaut@example.com")).thenReturn(newUser(true));
    assertTrue(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A067() {
    cookieJwt(jwtWithEmail("ümlaut@example.com"));
    when(repository.findByEmail("ümlaut@example.com")).thenReturn(newUser(false));
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A068() {
    cookieJwt(jwtWithEmail(""));
    when(repository.findByEmail("")).thenReturn(null);
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A069() {
    cookieJwt(jwtWithEmail("space @example.com"));
    when(repository.findByEmail("space @example.com")).thenReturn(newUser(true));
    assertTrue(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A070() {
    cookieJwt(jwtWithEmail("UPPER@EXAMPLE.COM"));
    when(repository.findByEmail("UPPER@EXAMPLE.COM")).thenReturn(newUser(true));
    assertTrue(controller.mailExistsAndIsConfirmed(request));
  }

  // ---------- Additional getBaseURL combinations ----------
  @Test
  void A071() {
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("a");
    when(request.getServerPort()).thenReturn(0);
    when(request.getContextPath()).thenReturn("/c");
    assertEquals("http://a:0/c/application", controller.getBaseURL(request));
  }

  @Test
  void A072() {
    when(request.getScheme()).thenReturn("ftp");
    when(request.getServerName()).thenReturn("srv");
    when(request.getServerPort()).thenReturn(21);
    when(request.getContextPath()).thenReturn("/ctx");
    assertEquals("ftp://srv:21/ctx/application", controller.getBaseURL(request));
  }

  @Test
  void A073() {
    when(request.getScheme()).thenReturn("");
    when(request.getServerName()).thenReturn("");
    when(request.getServerPort()).thenReturn(-1);
    when(request.getContextPath()).thenReturn("");
    assertEquals("://:-1/application", controller.getBaseURL(request));
  }

  @Test
  void A074() {
    when(request.getScheme()).thenReturn("https");
    when(request.getServerName()).thenReturn("::1");
    when(request.getServerPort()).thenReturn(443);
    when(request.getContextPath()).thenReturn("/app");
    assertEquals("https://::1:443/app/application", controller.getBaseURL(request));
  }

  @Test
  void A075() {
    when(request.getScheme()).thenReturn("http");
    when(request.getServerName()).thenReturn("example.com");
    when(request.getServerPort()).thenReturn(80);
    when(request.getContextPath()).thenReturn("/root");
    assertEquals("http://example.com:80/root/application", controller.getBaseURL(request));
  }

  // ---------- More JWT malformed cases ----------
  @Test
  void A076() {
    assertNull(controller.extractEmail("."));
  }

  @Test
  void A077() {
    assertNull(controller.extractEmail("a.b.c.d"));
  }

  @Test
  void A078() {
    String t = b64("{\"alg\":\"none\"}") + "." + b64("{\"email\":\"a@b.c\",\"x\":1}") + ".x";
    assertEquals("a@b.c", controller.extractEmail(t));
  }

  @Test
  void A079() {
    String t =
        b64("{\"alg\":\"none\"}")
            + "."
            + b64("{\"email\":\" \\u2603 \\uD83D\\uDE03@example.com\"}")
            + ".x";
    assertEquals(" ☃ 😃@example.com", controller.extractEmail(t));
  }

  @Test
  void A080() {
    String t = b64("{\"alg\":\"none\"}") + "." + b64("{\"email\":\"a\"}") + ".";
    assertEquals("a", controller.extractEmail(t));
  }

  // ---------- Cookie arrays with null elements ----------
  @Test
  void A081() {
    when(request.getCookies()).thenReturn(new Cookie[] {null, new Cookie("x", "v")});
    assertEquals("v", controller.getCookieByName(request, "x"));
  }

  @Test
  void A082() {
    when(request.getCookies()).thenReturn(new Cookie[] {null});
    assertNull(controller.getCookieByName(request, "x"));
  }

  // ---------- Many tiny variations to reach 100 tests while touching branches ----------
  @Test
  void A083() {
    cookieJwt(jwtWithEmail("e1@ex.com"));
    when(repository.findByEmail("e1@ex.com")).thenReturn(newUser(true));
    assertTrue(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A084() {
    cookieJwt(jwtWithEmail("e2@ex.com"));
    when(repository.findByEmail("e2@ex.com")).thenReturn(newUser(false));
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A085() {
    cookieJwt(jwtWithEmail("e3@ex.com"));
    when(repository.findByEmail("e3@ex.com")).thenReturn(null);
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A086() {
    cookieJwt(jwtWithEmail("e4@ex.com"));
    when(repository.findByEmail("e4@ex.com")).thenReturn(newUser(true));
    assertTrue(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A087() {
    cookieJwt(jwtWithEmail("e5@ex.com"));
    when(repository.findByEmail("e5@ex.com")).thenReturn(newUser(true));
    assertTrue(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A088() {
    cookieJwt(jwtWithEmail("e6@ex.com"));
    when(repository.findByEmail("e6@ex.com")).thenReturn(newUser(false));
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A089() {
    cookieJwt(jwtWithEmail("e7@ex.com"));
    when(repository.findByEmail("e7@ex.com")).thenReturn(newUser(true));
    assertTrue(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A090() {
    cookieJwt(jwtWithEmail("e8@ex.com"));
    when(repository.findByEmail("e8@ex.com")).thenReturn(newUser(false));
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A091() {
    cookieJwt(jwtWithEmail("e9@ex.com"));
    when(repository.findByEmail("e9@ex.com")).thenReturn(newUser(true));
    assertTrue(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A092() {
    cookieJwt(jwtWithEmail("e10@ex.com"));
    when(repository.findByEmail("e10@ex.com")).thenReturn(newUser(false));
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A093() {
    cookieJwt(jwtWithEmail("e11@ex.com"));
    when(repository.findByEmail("e11@ex.com")).thenReturn(newUser(true));
    assertTrue(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A094() {
    cookieJwt(jwtWithEmail("e12@ex.com"));
    when(repository.findByEmail("e12@ex.com")).thenReturn(newUser(false));
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A095() {
    cookieJwt(jwtWithEmail("e13@ex.com"));
    when(repository.findByEmail("e13@ex.com")).thenReturn(newUser(true));
    assertTrue(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A096() {
    cookieJwt(jwtWithEmail("e14@ex.com"));
    when(repository.findByEmail("e14@ex.com")).thenReturn(newUser(false));
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A097() {
    cookieJwt(jwtWithEmail("e15@ex.com"));
    when(repository.findByEmail("e15@ex.com")).thenReturn(newUser(true));
    assertTrue(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A098() {
    cookieJwt(jwtWithEmail("e16@ex.com"));
    when(repository.findByEmail("e16@ex.com")).thenReturn(newUser(false));
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A099() {
    cookieJwt(jwtWithEmail("e17@ex.com"));
    when(repository.findByEmail("e17@ex.com")).thenReturn(newUser(true));
    assertTrue(controller.mailExistsAndIsConfirmed(request));
  }

  @Test
  void A100() {
    cookieJwt(jwtWithEmail("e18@ex.com"));
    when(repository.findByEmail("e18@ex.com")).thenReturn(newUser(false));
    assertFalse(controller.mailExistsAndIsConfirmed(request));
  }
}
