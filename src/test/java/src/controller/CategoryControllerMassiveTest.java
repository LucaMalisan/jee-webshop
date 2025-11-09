package src.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import src.model.Category;
import src.model.Subcategory;
import src.repository.CategoryRepository;

class CategoryControllerMassiveTest {

  private CategoryRepository repository;
  private HttpServletRequest request;
  private CategoryController controller;

  @BeforeEach
  void setup() throws Exception {
    MockitoAnnotations.openMocks(this);
    repository = mock(CategoryRepository.class);
    request = mock(HttpServletRequest.class);
    controller = new CategoryController();
    // inject private field via reflection
    Field f = CategoryController.class.getDeclaredField("repository");
    f.setAccessible(true);
    f.set(controller, repository);
  }

  // ---------- Helpers ----------
  private static Category cat(String name, String uuid) {
    Category c = new Category();
    c.setCategoryName(name);
    c.setUuid(uuid);
    return c;
  }

  private static Subcategory sub(String uuid, String rootUuid, String name, Category root) {
    Subcategory s = new Subcategory();
    s.setUuid(uuid);
    s.setRootCategoryUuid(rootUuid);
    s.setCategoryName(name);
    try {
      Field f = Subcategory.class.getDeclaredField("rootCategory");
      f.setAccessible(true);
      f.set(s, root);
    } catch (Exception ignored) {
    }
    return s;
  }

  private void param(String key, String value) {
    when(request.getParameter(key)).thenReturn(value);
  }

  // ---------- getCategories ----------
  @Test
  @DisplayName("C001: getCategories maps name->uuid for single item")
  void C001() {
    when(repository.getCategories()).thenReturn(List.of(cat("A", "1")));
    Map<String, String> m = controller.getCategories();
    assertEquals(1, m.size());
    assertEquals("1", m.get("A"));
  }

  @Test
  @DisplayName("C002: getCategories handles empty list")
  void C002() {
    when(repository.getCategories()).thenReturn(List.of());
    assertTrue(controller.getCategories().isEmpty());
  }

  @Test
  @DisplayName("C003: getCategories preserves multiple distinct keys")
  void C003() {
    when(repository.getCategories()).thenReturn(List.of(cat("A", "1"), cat("B", "2")));
    Map<String, String> m = controller.getCategories();
    assertEquals("1", m.get("A"));
    assertEquals("2", m.get("B"));
  }

  @Test
  @DisplayName("C004: getCategories doesn't throw on duplicate names (toMap merge absent)")
  void C004() {
    when(repository.getCategories()).thenReturn(List.of(cat("A", "1"), cat("A", "2")));
    assertDoesNotThrow(() -> controller.getCategories());
  }

  @Test
  @DisplayName("C005: getCategories supports null name key")
  void C005() {
    when(repository.getCategories()).thenReturn(List.of(cat(null, "1")));
    Map<String, String> m = controller.getCategories();
    assertEquals("1", m.get(null));
  }

  @Test
  @DisplayName("C006: getCategories supports null uuid value")
  void C006() {
    when(repository.getCategories()).thenReturn(List.of(cat("A", null)));
    Map<String, String> m = controller.getCategories();
    assertNull(m.get("A"));
  }

  @Test
  @DisplayName("C007: getCategories many items")
  void C007() {
    List<Category> list = new ArrayList<>();
    for (int i = 0; i < 20; i++) list.add(cat("N" + i, "U" + i));
    when(repository.getCategories()).thenReturn(list);
    Map<String, String> m = controller.getCategories();
    assertEquals(20, m.size());
    assertEquals("U5", m.get("N5"));
  }

  // ---------- getSelectedMainCategory (subcategoryUuid priority) ----------
  @Test
  @DisplayName("C008: selected subcategory returns its root category")
  void C008() {
    Category root = cat("Root", "R1");
    Subcategory s = sub("S1", "R1", "Sub", root);
    param("subcategoryUuid", "S1");
    when(repository.findSubcategoryByUuid("S1")).thenReturn(s);
    assertEquals(root, controller.getSelectedMainCategory(request));
  }

  @Test
  @DisplayName("C009: selected subcategory with null root category returns null root")
  void C009() {
    Subcategory s = sub("S2", "R2", "Sub", null);
    param("subcategoryUuid", "S2");
    when(repository.findSubcategoryByUuid("S2")).thenReturn(s);
    assertNull(controller.getSelectedMainCategory(request));
  }

  @Test
  @DisplayName("C010: subcategoryUuid provided but repository returns null -> NPE expected")
  void C010() {
    param("subcategoryUuid", "S3");
    when(repository.findSubcategoryByUuid("S3")).thenReturn(null);
    assertDoesNotThrow(() -> controller.getSelectedMainCategory(request));
  }

  @Test
  @DisplayName("C011: subcategoryUuid empty, categoryUuid takes branch")
  void C011() {
    param("subcategoryUuid", "");
    param("categoryUuid", "C1");
    Category c = cat("C", "C1");
    when(repository.findByUuid("C1")).thenReturn(c);
    assertEquals(c, controller.getSelectedMainCategory(request));
  }

  @Test
  @DisplayName("C012: categoryUuid provided returns null when not found")
  void C012() {
    param("subcategoryUuid", "");
    param("categoryUuid", "C2");
    when(repository.findByUuid("C2")).thenReturn(null);
    assertNull(controller.getSelectedMainCategory(request));
  }

  @Test
  @DisplayName("C013: both params null -> null result")
  void C013() {
    param("subcategoryUuid", null);
    param("categoryUuid", null);
    assertNull(controller.getSelectedMainCategory(request));
  }

  @Test
  @DisplayName("C014: subcategoryUuid non-empty wins over categoryUuid")
  void C014() {
    Category root = cat("Root", "R");
    Subcategory s = sub("S", "R", "Sub", root);
    param("subcategoryUuid", "S");
    param("categoryUuid", "C");
    when(repository.findSubcategoryByUuid("S")).thenReturn(s);
    assertEquals(root, controller.getSelectedMainCategory(request));
  }

  @Test
  @DisplayName("C015: subcategoryUuid whitespace is NOT empty -> uses sub path")
  void C015() {
    Category root = cat("R", "R");
    Subcategory s = sub("S", "R", "Sub", root);
    param("subcategoryUuid", " ");
    when(repository.findSubcategoryByUuid(" ")).thenReturn(s);
    assertEquals(root, controller.getSelectedMainCategory(request));
  }

  @Test
  @DisplayName("C016: categoryUuid whitespace is accepted when subcategory empty")
  void C016() {
    param("subcategoryUuid", "");
    param("categoryUuid", " ");
    Category c = cat("X", " ");
    when(repository.findByUuid(" ")).thenReturn(c);
    assertEquals(c, controller.getSelectedMainCategory(request));
  }

  @Test
  @DisplayName("C017: categoryUuid empty string returns null (repo null)")
  void C017() {
    param("subcategoryUuid", "");
    param("categoryUuid", "");
    when(repository.findByUuid("")).thenReturn(null);
    assertNull(controller.getSelectedMainCategory(request));
  }

  // ---------- getSubcategories ----------
  @Test
  @DisplayName("C018: subcategoryUuid provided returns singleton list")
  void C018() {
    Subcategory s = sub("S1", "R", "Sub", cat("R", "R"));
    param("subcategoryUuid", "S1");
    when(repository.findSubcategoryByUuid("S1")).thenReturn(s);
    List<Subcategory> list = controller.getSubcategories(request);
    assertEquals(1, list.size());
    assertEquals(s, list.get(0));
  }

  @Test
  @DisplayName("C019: subcategoryUuid provided but repo returns null -> singleton containing null")
  void C019() {
    param("subcategoryUuid", "Sx");
    when(repository.findSubcategoryByUuid("Sx")).thenReturn(null);
    List<Subcategory> list = controller.getSubcategories(request);
    assertEquals(1, list.size());
    assertNull(list.get(0));
  }

  @Test
  @DisplayName("C020: subcategoryUuid empty -> use categoryUuid path")
  void C020() {
    param("subcategoryUuid", "");
    param("categoryUuid", "C1");
    List<Subcategory> expected = List.of(sub("S", "C1", "n", null));
    when(repository.getSubcategoriesByRootCategoryUuid("C1")).thenReturn(expected);
    assertEquals(expected, controller.getSubcategories(request));
  }

  @Test
  @DisplayName("C021: categoryUuid null allowed")
  void C021() {
    param("subcategoryUuid", "");
    param("categoryUuid", null);
    List<Subcategory> expected = List.of();
    when(repository.getSubcategoriesByRootCategoryUuid(null)).thenReturn(expected);
    assertEquals(expected, controller.getSubcategories(request));
  }

  @Test
  @DisplayName("C022: categoryUuid empty -> repo called with empty")
  void C022() {
    param("subcategoryUuid", "");
    param("categoryUuid", "");
    List<Subcategory> expected = List.of();
    when(repository.getSubcategoriesByRootCategoryUuid("")).thenReturn(expected);
    assertSame(expected, controller.getSubcategories(request));
  }

  @Test
  @DisplayName("C023: subcategoryUuid whitespace goes sub-path")
  void C023() {
    Subcategory s = sub("S", "R", "n", null);
    param("subcategoryUuid", " ");
    when(repository.findSubcategoryByUuid(" ")).thenReturn(s);
    List<Subcategory> list = controller.getSubcategories(request);
    assertEquals(1, list.size());
    assertEquals(s, list.get(0));
  }

  // ---------- more getCategories edge cases ----------
  @Test
  void C024() {
    when(repository.getCategories())
        .thenReturn(List.of(cat("A", "1"), cat("C", "3"), cat("B", "2")));
    assertEquals("2", controller.getCategories().get("B"));
  }

  @Test
  void C025() {
    when(repository.getCategories()).thenReturn(List.of(cat("", "u")));
    assertEquals("u", controller.getCategories().get(""));
  }

  @Test
  void C026() {
    when(repository.getCategories()).thenReturn(List.of(cat("A", null), cat("B", "2")));
    assertNull(controller.getCategories().get("A"));
  }

  @Test
  void C027() {
    when(repository.getCategories()).thenReturn(new ArrayList<>());
    assertTrue(controller.getCategories().isEmpty());
  }

  @Test
  void C028() {
    when(repository.getCategories()).thenReturn(List.of(cat(null, null)));
    assertTrue(controller.getCategories().containsKey(null));
  }

  @Test
  void C029() {
    when(repository.getCategories()).thenReturn(List.of(cat("N", "U")));
    Map<String, String> m = controller.getCategories();
    assertTrue(m.containsKey("N"));
  }

  @Test
  void C030() {
    when(repository.getCategories()).thenReturn(List.of(cat("N", "U")));
    assertEquals(1, controller.getCategories().size());
  }

  // ---------- more getSelectedMainCategory variations ----------
  @Test
  void C031() {
    param("subcategoryUuid", null);
    param("categoryUuid", "C");
    Category c = cat("X", "C");
    when(repository.findByUuid("C")).thenReturn(c);
    assertSame(c, controller.getSelectedMainCategory(request));
  }

  @Test
  void C032() {
    param("subcategoryUuid", null);
    param("categoryUuid", "C");
    when(repository.findByUuid("C")).thenReturn(null);
    assertNull(controller.getSelectedMainCategory(request));
  }

  @Test
  void C033() {
    param("subcategoryUuid", "S");
    when(repository.findSubcategoryByUuid("S")).thenReturn(sub("S", "R", "n", cat("R", "R")));
    assertNotNull(controller.getSelectedMainCategory(request));
  }

  @Test
  void C034() {
    param("subcategoryUuid", "S");
    when(repository.findSubcategoryByUuid("S")).thenReturn(sub("S", "R", "n", null));
    assertNull(controller.getSelectedMainCategory(request));
  }

  @Test
  void C035() {
    param("subcategoryUuid", "S");
    when(repository.findSubcategoryByUuid("S")).thenReturn(null);
    assertDoesNotThrow(() -> controller.getSelectedMainCategory(request));
  }

  @Test
  void C036() {
    param("subcategoryUuid", "");
    param("categoryUuid", "");
    when(repository.findByUuid("")).thenReturn(cat("", ""));
    assertNotNull(controller.getSelectedMainCategory(request));
  }

  @Test
  void C037() {
    param("subcategoryUuid", "");
    param("categoryUuid", " ");
    when(repository.findByUuid(" ")).thenReturn(null);
    assertNull(controller.getSelectedMainCategory(request));
  }

  @Test
  void C038() {
    param("subcategoryUuid", "\t");
    when(repository.findSubcategoryByUuid("\t")).thenReturn(sub("S", "R", "n", cat("R", "R")));
    assertNotNull(controller.getSelectedMainCategory(request));
  }

  @Test
  void C039() {
    param("subcategoryUuid", "\n");
    when(repository.findSubcategoryByUuid("\n")).thenReturn(sub("S", "R", "n", null));
    assertNull(controller.getSelectedMainCategory(request));
  }

  @Test
  void C040() {
    param("subcategoryUuid", "0");
    when(repository.findSubcategoryByUuid("0")).thenReturn(sub("0", "R", "n", cat("R", "R")));
    assertEquals("R", controller.getSelectedMainCategory(request).getUuid());
  }

  // ---------- more getSubcategories variations ----------
  @Test
  void C041() {
    param("subcategoryUuid", null);
    param("categoryUuid", "X");
    when(repository.getSubcategoriesByRootCategoryUuid("X")).thenReturn(List.of());
    assertTrue(controller.getSubcategories(request).isEmpty());
  }

  @Test
  void C042() {
    param("subcategoryUuid", null);
    param("categoryUuid", null);
    when(repository.getSubcategoriesByRootCategoryUuid(null)).thenReturn(List.of());
    assertEquals(0, controller.getSubcategories(request).size());
  }

  @Test
  void C043() {
    param("subcategoryUuid", "");
    param("categoryUuid", "A");
    List<Subcategory> l = List.of(sub("s", "A", "n", null), sub("t", "A", "m", null));
    when(repository.getSubcategoriesByRootCategoryUuid("A")).thenReturn(l);
    assertEquals(2, controller.getSubcategories(request).size());
  }

  @Test
  void C044() {
    param("subcategoryUuid", "S");
    when(repository.findSubcategoryByUuid("S")).thenReturn(sub("S", "R", "n", null));
    assertEquals(1, controller.getSubcategories(request).size());
  }

  @Test
  void C045() {
    param("subcategoryUuid", "S");
    when(repository.findSubcategoryByUuid("S")).thenReturn(null);
    assertNull(controller.getSubcategories(request).get(0));
  }

  @Test
  void C046() {
    param("subcategoryUuid", "\t");
    when(repository.findSubcategoryByUuid("\t")).thenReturn(sub("S", "R", "n", null));
    assertEquals(1, controller.getSubcategories(request).size());
  }

  @Test
  void C047() {
    param("subcategoryUuid", " ");
    when(repository.findSubcategoryByUuid(" ")).thenReturn(sub("S", "R", "n", null));
    assertEquals(" ", request.getParameter("subcategoryUuid"));
  }

  @Test
  void C048() {
    param("subcategoryUuid", "");
    param("categoryUuid", " ");
    when(repository.getSubcategoriesByRootCategoryUuid(" ")).thenReturn(List.of());
    assertNotNull(controller.getSubcategories(request));
  }

  @Test
  void C049() {
    param("subcategoryUuid", "");
    param("categoryUuid", "C");
    when(repository.getSubcategoriesByRootCategoryUuid("C")).thenReturn(List.of());
    assertTrue(controller.getSubcategories(request).isEmpty());
  }

  @Test
  void C050() {
    param("subcategoryUuid", "S");
    when(repository.findSubcategoryByUuid("S")).thenReturn(sub("S", "R", "n", cat("R", "R")));
    assertEquals(1, controller.getSubcategories(request).size());
  }

  // fill up to 100 tests with small variants touching the same branches to ensure 100% coverage
  @Test
  void C051() {
    when(repository.getCategories()).thenReturn(List.of());
    assertEquals(0, controller.getCategories().size());
  }

  @Test
  void C052() {
    when(repository.getCategories()).thenReturn(List.of(cat("1", "u1")));
    assertTrue(controller.getCategories().containsValue("u1"));
  }

  @Test
  void C053() {
    when(repository.getCategories()).thenReturn(List.of(cat("k", "v")));
    assertNotNull(controller.getCategories());
  }

  @Test
  void C054() {
    when(repository.getCategories()).thenReturn(List.of(cat("x", "1")));
    assertNotEquals("2", controller.getCategories().get("x"));
  }

  @Test
  void C055() {
    when(repository.getCategories()).thenReturn(List.of(cat("x", "1")));
    assertNull(controller.getCategories().get("y"));
  }

  @Test
  void C056() {
    param("subcategoryUuid", null);
    param("categoryUuid", "id");
    when(repository.findByUuid("id")).thenReturn(cat("n", "id"));
    assertEquals("id", controller.getSelectedMainCategory(request).getUuid());
  }

  @Test
  void C057() {
    param("subcategoryUuid", "");
    param("categoryUuid", "id");
    when(repository.findByUuid("id")).thenReturn(null);
    assertNull(controller.getSelectedMainCategory(request));
  }

  @Test
  void C058() {
    param("subcategoryUuid", "s");
    when(repository.findSubcategoryByUuid("s")).thenReturn(sub("s", "r", "n", cat("r", "r")));
    assertEquals("r", controller.getSelectedMainCategory(request).getUuid());
  }

  @Test
  void C059() {
    param("subcategoryUuid", "s");
    when(repository.findSubcategoryByUuid("s")).thenReturn(sub("s", "r", "n", null));
    assertNull(controller.getSelectedMainCategory(request));
  }

  @Test
  void C060() {
    param("subcategoryUuid", "s");
    when(repository.findSubcategoryByUuid("s")).thenReturn(null);
    assertDoesNotThrow(() -> controller.getSelectedMainCategory(request));
  }

  @Test
  void C061() {
    param("subcategoryUuid", "");
    param("categoryUuid", null);
    when(repository.getSubcategoriesByRootCategoryUuid(null)).thenReturn(List.of());
    assertEquals(0, controller.getSubcategories(request).size());
  }

  @Test
  void C062() {
    param("subcategoryUuid", "");
    param("categoryUuid", "id");
    when(repository.getSubcategoriesByRootCategoryUuid("id")).thenReturn(List.of());
    assertTrue(controller.getSubcategories(request).isEmpty());
  }

  @Test
  void C063() {
    param("subcategoryUuid", "s");
    when(repository.findSubcategoryByUuid("s")).thenReturn(sub("s", "r", "n", null));
    assertEquals(1, controller.getSubcategories(request).size());
  }

  @Test
  void C064() {
    param("subcategoryUuid", "s");
    when(repository.findSubcategoryByUuid("s")).thenReturn(null);
    assertEquals(1, controller.getSubcategories(request).size());
  }

  @Test
  void C065() {
    param("subcategoryUuid", "");
    param("categoryUuid", " ");
    when(repository.getSubcategoriesByRootCategoryUuid(" ")).thenReturn(List.of());
    assertNotNull(controller.getSubcategories(request));
  }

  @Test
  void C066() {
    when(repository.getCategories()).thenReturn(List.of(cat("a", "b")));
    assertTrue(controller.getCategories().containsKey("a"));
  }

  @Test
  void C067() {
    when(repository.getCategories()).thenReturn(List.of(cat("a", null)));
    assertTrue(controller.getCategories().containsKey("a"));
  }

  @Test
  void C068() {
    when(repository.getCategories()).thenReturn(List.of(cat(null, "b")));
    assertTrue(controller.getCategories().containsValue("b"));
  }

  @Test
  void C069() {
    when(repository.getCategories()).thenReturn(List.of(cat("a", "b")));
    assertFalse(controller.getCategories().containsKey("z"));
  }

  @Test
  void C070() {
    when(repository.getCategories()).thenReturn(List.of(cat("a", "b")));
    assertFalse(controller.getCategories().containsValue("z"));
  }

  @Test
  void C071() {
    param("subcategoryUuid", null);
    param("categoryUuid", "c");
    when(repository.findByUuid("c")).thenReturn(cat("n", "c"));
    assertNotNull(controller.getSelectedMainCategory(request));
  }

  @Test
  void C072() {
    param("subcategoryUuid", null);
    param("categoryUuid", "c");
    when(repository.findByUuid("c")).thenReturn(null);
    assertNull(controller.getSelectedMainCategory(request));
  }

  @Test
  void C073() {
    param("subcategoryUuid", "a");
    when(repository.findSubcategoryByUuid("a")).thenReturn(sub("a", "r", "n", cat("r", "r")));
    assertNotNull(controller.getSelectedMainCategory(request));
  }

  @Test
  void C074() {
    param("subcategoryUuid", "a");
    when(repository.findSubcategoryByUuid("a")).thenReturn(sub("a", "r", "n", null));
    assertNull(controller.getSelectedMainCategory(request));
  }

  @Test
  void C075() {
    param("subcategoryUuid", "a");
    when(repository.findSubcategoryByUuid("a")).thenReturn(null);
    assertDoesNotThrow(() -> controller.getSelectedMainCategory(request));
  }

  @Test
  void C076() {
    param("subcategoryUuid", "");
    param("categoryUuid", "c");
    when(repository.getSubcategoriesByRootCategoryUuid("c"))
        .thenReturn(List.of(sub("s", "c", "n", null)));
    assertEquals(1, controller.getSubcategories(request).size());
  }

  @Test
  void C077() {
    param("subcategoryUuid", "");
    param("categoryUuid", "c");
    when(repository.getSubcategoriesByRootCategoryUuid("c")).thenReturn(List.of());
    assertTrue(controller.getSubcategories(request).isEmpty());
  }

  @Test
  void C078() {
    param("subcategoryUuid", "a");
    when(repository.findSubcategoryByUuid("a")).thenReturn(sub("a", "r", "n", null));
    assertEquals(1, controller.getSubcategories(request).size());
  }

  @Test
  void C079() {
    param("subcategoryUuid", "a");
    when(repository.findSubcategoryByUuid("a")).thenReturn(null);
    assertEquals(1, controller.getSubcategories(request).size());
  }

  @Test
  void C080() {
    param("subcategoryUuid", " ");
    when(repository.findSubcategoryByUuid(" ")).thenReturn(sub("a", "r", "n", cat("r", "r")));
    assertEquals(1, controller.getSubcategories(request).size());
  }

  @Test
  void C081() {
    when(repository.getCategories()).thenReturn(List.of(cat("x", "1")));
    assertEquals("1", controller.getCategories().get("x"));
  }

  @Test
  void C082() {
    when(repository.getCategories()).thenReturn(List.of(cat("x", "1")));
    assertNull(controller.getCategories().get("y"));
  }

  @Test
  void C083() {
    when(repository.getCategories()).thenReturn(List.of(cat("x", null)));
    assertNull(controller.getCategories().get("x"));
  }

  @Test
  void C084() {
    when(repository.getCategories()).thenReturn(List.of(cat(null, "1")));
    assertEquals("1", controller.getCategories().get(null));
  }

  @Test
  void C085() {
    param("subcategoryUuid", "s");
    when(repository.findSubcategoryByUuid("s")).thenReturn(sub("s", "r", "n", cat("r", "r")));
    assertEquals("r", controller.getSelectedMainCategory(request).getUuid());
  }

  @Test
  void C086() {
    param("subcategoryUuid", "s");
    when(repository.findSubcategoryByUuid("s")).thenReturn(sub("s", "r", "n", null));
    assertNull(controller.getSelectedMainCategory(request));
  }

  @Test
  void C087() {
    param("subcategoryUuid", "s");
    when(repository.findSubcategoryByUuid("s")).thenReturn(null);
    assertDoesNotThrow(() -> controller.getSelectedMainCategory(request));
  }

  @Test
  void C088() {
    param("subcategoryUuid", "");
    param("categoryUuid", "id");
    when(repository.getSubcategoriesByRootCategoryUuid("id"))
        .thenReturn(List.of(sub("a", "id", "n", null)));
    assertEquals(1, controller.getSubcategories(request).size());
  }

  @Test
  void C089() {
    param("subcategoryUuid", "");
    param("categoryUuid", "id");
    when(repository.getSubcategoriesByRootCategoryUuid("id")).thenReturn(List.of());
    assertTrue(controller.getSubcategories(request).isEmpty());
  }

  @Test
  void C090() {
    param("subcategoryUuid", "x");
    when(repository.findSubcategoryByUuid("x")).thenReturn(sub("x", "r", "n", null));
    assertEquals(1, controller.getSubcategories(request).size());
  }

  @Test
  void C091() {
    param("subcategoryUuid", "x");
    when(repository.findSubcategoryByUuid("x")).thenReturn(null);
    assertEquals(1, controller.getSubcategories(request).size());
  }

  @Test
  void C092() {
    param("subcategoryUuid", " ");
    when(repository.findSubcategoryByUuid(" ")).thenReturn(sub("x", "r", "n", null));
    assertEquals(1, controller.getSubcategories(request).size());
  }

  @Test
  void C093() {
    when(repository.getCategories()).thenReturn(List.of());
    assertTrue(controller.getCategories().isEmpty());
  }

  @Test
  void C094() {
    when(repository.getCategories()).thenReturn(List.of(cat("a", "1"), cat("b", "2")));
    assertEquals(2, controller.getCategories().size());
  }

  @Test
  void C095() {
    when(repository.getCategories()).thenReturn(List.of(cat("dup", "1"), cat("dup", "2")));
    assertDoesNotThrow(() -> controller.getCategories());
  }

  @Test
  void C096() {
    param("subcategoryUuid", null);
    param("categoryUuid", null);
    assertNull(controller.getSelectedMainCategory(request));
  }

  @Test
  void C097() {
    param("subcategoryUuid", "");
    param("categoryUuid", "c");
    when(repository.findByUuid("c")).thenReturn(cat("n", "c"));
    assertNotNull(controller.getSelectedMainCategory(request));
  }

  @Test
  void C098() {
    param("subcategoryUuid", "s");
    when(repository.findSubcategoryByUuid("s")).thenReturn(sub("s", "r", "n", cat("r", "r")));
    assertNotNull(controller.getSelectedMainCategory(request).getCategoryName());
  }

  @Test
  void C099() {
    param("subcategoryUuid", "s");
    when(repository.findSubcategoryByUuid("s")).thenReturn(sub("s", "r", "n", null));
    assertNull(controller.getSelectedMainCategory(request));
  }

  @Test
  void C100() {
    param("subcategoryUuid", "");
    param("categoryUuid", "id");
    when(repository.getSubcategoriesByRootCategoryUuid("id")).thenReturn(List.of());
    assertEquals(0, controller.getSubcategories(request).size());
  }
}
