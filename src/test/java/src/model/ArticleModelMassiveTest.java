package src.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ArticleModelMassiveTest {

  private Article article;

  @BeforeEach
  void setup() {
    article = new Article();
    article.setSellingPrice(10.0);
    article.setListPrice(20.0);
    article.setAvailable(true);
    article.setImageList(new ArrayList<>());
  }

  private static ArticleImage img(String url, Integer pos) {
    ArticleImage i = new ArticleImage();
    i.setImageURL(url);
    i.setPosition(pos);
    return i;
  }

  // ------- getDiscountPercent -------
  @Test @DisplayName("AM001: getDiscountPercent returns null when listPrice null")
  void AM001() {
    article.setListPrice(null);
    assertNull(article.getDiscountPercent());
  }

  @Test @DisplayName("AM002: getDiscountPercent 50% when selling is half of list")
  void AM002() {
    article.setSellingPrice(50.0);
    article.setListPrice(100.0);
    assertEquals("50", article.getDiscountPercent());
  }

  @Test @DisplayName("AM003: getDiscountPercent 0 when no discount")
  void AM003() {
    article.setSellingPrice(100.0);
    article.setListPrice(100.0);
    assertEquals("0", article.getDiscountPercent());
  }

  @Test @DisplayName("AM004: getDiscountPercent rounds correctly")
  void AM004() {
    article.setSellingPrice(33.3);
    article.setListPrice(100.0);
    // 100 - 33.3% = 66.7% -> rounds 67
    assertEquals("67", article.getDiscountPercent());
  }

  @Test @DisplayName("AM005: getDiscountPercent negative when selling > list")
  void AM005() {
    article.setSellingPrice(120.0);
    article.setListPrice(100.0);
    // 100 - 120% = -20 -> "-20"
    assertEquals("-20", article.getDiscountPercent());
  }

  @Test @DisplayName("AM006: getDiscountPercent with small numbers")
  void AM006() {
    article.setSellingPrice(0.01);
    article.setListPrice(0.02);
    assertEquals("50", article.getDiscountPercent());
  }

  @Test @DisplayName("AM007: getDiscountPercent with big numbers")
  void AM007() {
    article.setSellingPrice(1_000_000.0);
    article.setListPrice(2_000_000.0);
    assertEquals("50", article.getDiscountPercent());
  }

  @Test @DisplayName("AM008: getDiscountPercent results 100 when selling is 0")
  void AM008() {
    article.setSellingPrice(0.0);
    article.setListPrice(99.99);
    assertEquals("100", article.getDiscountPercent());
  }

  @Test @DisplayName("AM009: getDiscountPercent results -100 when selling double list")
  void AM009() {
    article.setSellingPrice(200.0);
    article.setListPrice(100.0);
    assertEquals("-100", article.getDiscountPercent());
  }

  @Test @DisplayName("AM010: getDiscountPercent with non-integer percent")
  void AM010() {
    article.setSellingPrice(19.0);
    article.setListPrice(30.0);
    // 100 - 63.333... = 36.666... -> rounds 37
    assertEquals("37", article.getDiscountPercent());
  }

  // ------- formatPrice -------
  @Test @DisplayName("AM011: formatPrice shows was when listPrice present and available")
  void AM011() {
    article.setSellingPrice(12.0);
    article.setListPrice(20.0);
    article.setAvailable(true);
    assertEquals("12.00 was 20.00 CHF", article.formatPrice());
  }

  @Test @DisplayName("AM012: formatPrice without was when not available")
  void AM012() {
    article.setAvailable(false);
    assertEquals("10.00 CHF", article.formatPrice());
  }

  @Test @DisplayName("AM013: formatPrice without was when listPrice null")
  void AM013() {
    article.setListPrice(null);
    assertEquals("10.00 CHF", article.formatPrice());
  }

  @Test @DisplayName("AM014: formatPrice formatting two decimals")
  void AM014() {
    article.setSellingPrice(1.2);
    article.setListPrice(2.3456);
    article.setAvailable(true);
    assertEquals("1.20 was 2.35 CHF", article.formatPrice());
  }

  @Test @DisplayName("AM015: formatPrice large numbers")
  void AM015() {
    article.setSellingPrice(123456.789);
    article.setListPrice(234567.891);
    article.setAvailable(true);
    assertEquals("123456.79 was 234567.89 CHF", article.formatPrice());
  }

  @Test @DisplayName("AM016: formatPrice only selling when available true but listPrice null")
  void AM016() {
    article.setListPrice(null);
    article.setAvailable(true);
    article.setSellingPrice(0.005);
    assertEquals("0.01 CHF", article.formatPrice());
  }

  @Test @DisplayName("AM017: formatPrice handles selling with many decimals")
  void AM017() {
    article.setSellingPrice(1.999);
    article.setListPrice(null);
    assertEquals("2.00 CHF", article.formatPrice());
  }

  @Test @DisplayName("AM018: formatPrice shows was only when both conditions met")
  void AM018() {
    article.setAvailable(false);
    article.setListPrice(100.0);
    article.setSellingPrice(80.0);
    assertEquals("80.00 CHF", article.formatPrice());
  }

  // ------- getPrimaryImageURL -------
  @Test @DisplayName("AM019: getPrimaryImageURL returns empty when list empty")
  void AM019() {
    article.setImageList(List.of());
    assertEquals("", article.getPrimaryImageURL());
  }

  @Test @DisplayName("AM020: getPrimaryImageURL returns image with position 1")
  void AM020() {
    article.setImageList(List.of(img("u1", 2), img("p", 1), img("u2", 3)));
    assertEquals("p", article.getPrimaryImageURL());
  }

  @Test @DisplayName("AM021: getPrimaryImageURL ignores null positions")
  void AM021() {
    article.setImageList(List.of(img("x", null), img("p", 1)));
    assertEquals("p", article.getPrimaryImageURL());
  }

  @Test @DisplayName("AM022: getPrimaryImageURL returns empty when none has position 1")
  void AM022() {
    article.setImageList(List.of(img("a", 2), img("b", 3)));
    assertEquals("", article.getPrimaryImageURL());
  }

  @Test @DisplayName("AM023: getPrimaryImageURL works with first element primary")
  void AM023() {
    article.setImageList(List.of(img("p", 1), img("x", 2)));
    assertEquals("p", article.getPrimaryImageURL());
  }

  @Test @DisplayName("AM024: getPrimaryImageURL works with last element primary")
  void AM024() {
    article.setImageList(List.of(img("x", 2), img("p", 1)));
    assertEquals("p", article.getPrimaryImageURL());
  }

  @Test @DisplayName("AM025: getPrimaryImageURL with multiple primaries returns one of them")
  void AM025() {
    article.setImageList(List.of(img("p1", 1), img("p2", 1)));
    String res = article.getPrimaryImageURL();
    assertTrue(res.equals("p1") || res.equals("p2"));
  }

  @Test @DisplayName("AM026: getPrimaryImageURL with mixed nulls and valid")
  void AM026() {
    article.setImageList(List.of(img("x", null), img("p", 1), img("y", null)));
    assertEquals("p", article.getPrimaryImageURL());
  }

  @Test @DisplayName("AM027: getPrimaryImageURL prefers exact position == 1")
  void AM027() {
    article.setImageList(List.of(img("x", 0), img("p", 1)));
    assertEquals("p", article.getPrimaryImageURL());
  }

  // Additional small variations to reach 50 tests
  @Test void AM028() { article.setSellingPrice(15.0); article.setListPrice(30.0); assertEquals("50", article.getDiscountPercent()); }
  @Test void AM029() { article.setSellingPrice(15.01); article.setListPrice(30.0); assertEquals("50", article.getDiscountPercent()); }
  @Test void AM030() { article.setSellingPrice(14.99); article.setListPrice(30.0); assertEquals("50", article.getDiscountPercent()); }
  @Test void AM031() { article.setSellingPrice(1.234); article.setListPrice(2.468); assertEquals("50", article.getDiscountPercent()); }
  @Test void AM032() { article.setSellingPrice(99.99); article.setListPrice(100.0); assertEquals("0", article.getDiscountPercent()); }
  @Test void AM033() { article.setSellingPrice(0.1); article.setListPrice(0.3); assertEquals("67", article.getDiscountPercent()); }
  @Test void AM034() { article.setSellingPrice(80.0); article.setListPrice(100.0); article.setAvailable(true); assertEquals("80.00 was 100.00 CHF", article.formatPrice()); }
  @Test void AM035() { article.setSellingPrice(80.0); article.setListPrice(100.0); article.setAvailable(false); assertEquals("80.00 CHF", article.formatPrice()); }
  @Test void AM036() { article.setSellingPrice(0.0); article.setListPrice(null); assertEquals("0.00 CHF", article.formatPrice()); }
  @Test void AM037() { article.setSellingPrice(0.004); article.setListPrice(null); assertEquals("0.00 CHF", article.formatPrice()); }
  @Test void AM038() { article.setSellingPrice(0.005); article.setListPrice(null); assertEquals("0.01 CHF", article.formatPrice()); }
  @Test void AM039() { article.setSellingPrice(9999.999); article.setListPrice(10000.0); article.setAvailable(true); assertEquals("10000.00 was 10000.00 CHF", article.formatPrice()); }
  @Test void AM040() { article.setImageList(List.of(img("", 1))); assertEquals("", article.getPrimaryImageURL()); }
  @Test void AM041() { article.setImageList(List.of(img("u", 2), img("", 1))); assertEquals("", article.getPrimaryImageURL()); }
  @Test void AM042() { article.setImageList(List.of(img("u", 2), img("p ", 1))); assertEquals("p ", article.getPrimaryImageURL()); }
  @Test void AM043() { article.setImageList(List.of(img("u", 2), img("p", null))); assertEquals("", article.getPrimaryImageURL()); }
  @Test void AM044() { article.setImageList(List.of(img("u", 0))); assertEquals("", article.getPrimaryImageURL()); }
  @Test void AM045() { article.setImageList(List.of(img("p", 1), img("p", 1))); assertEquals("p", article.getPrimaryImageURL()); }
  @Test void AM046() { article.setSellingPrice(1.0); article.setListPrice(2.0); article.setAvailable(true); assertEquals("1.00 was 2.00 CHF", article.formatPrice()); }
  @Test void AM047() { article.setSellingPrice(1.0); article.setListPrice(2.0); article.setAvailable(false); assertEquals("1.00 CHF", article.formatPrice()); }
  @Test void AM048() { article.setSellingPrice(2.5); article.setListPrice(null); assertEquals("2.50 CHF", article.formatPrice()); }
  @Test void AM049() { article.setSellingPrice(2.5); article.setListPrice(5.0); article.setAvailable(true); assertEquals("2.50 was 5.00 CHF", article.formatPrice()); }
  @Test void AM050() { article.setSellingPrice(2.5); article.setListPrice(5.0); article.setAvailable(false); assertEquals("2.50 CHF", article.formatPrice()); }
}
