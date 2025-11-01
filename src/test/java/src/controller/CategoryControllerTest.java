package src.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import src.model.Category;
import src.model.Subcategory;
import src.repository.CategoryRepository;

class CategoryControllerTest {

  @Mock private CategoryRepository repository;

  @Mock private HttpServletRequest request;

  @InjectMocks private CategoryController controller;

  @BeforeEach
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetSelectedMainCategory_ReturnsCategoryFromSubcategoryUuid() {
    // Arrange
    String subcategoryUuid = "sub123";
    when(request.getParameter("subcategoryUuid")).thenReturn(subcategoryUuid);

    Category rootCategory = new Category();
    rootCategory.setUuid("root123");

    Subcategory subcategory = new Subcategory();
    subcategory.setRootCategory(rootCategory);

    when(repository.findSubcategoryByUuid(subcategoryUuid)).thenReturn(subcategory);

    // Act
    Category result = controller.getSelectedMainCategory(request);

    // Assert
    assertNotNull(result);
    assertEquals("root123", result.getUuid());
    verify(repository).findSubcategoryByUuid(subcategoryUuid);
  }

  @Test
  void testGetSelectedMainCategory_ReturnsCategoryFromCategoryUuid() {
    // Arrange
    String categoryUuid = "cat123";
    when(request.getParameter("subcategoryUuid")).thenReturn(null);
    when(request.getParameter("categoryUuid")).thenReturn(categoryUuid);

    Category category = new Category();
    category.setUuid(categoryUuid);

    when(repository.findByUuid(categoryUuid)).thenReturn(category);

    // Act
    Category result = controller.getSelectedMainCategory(request);

    // Assert
    assertNotNull(result);
    assertEquals("cat123", result.getUuid());
    verify(repository).findByUuid(categoryUuid);
  }

  @Test
  void testGetSelectedMainCategory_ReturnsNullWhenNoUuidsProvided() {
    // Arrange
    when(request.getParameter("subcategoryUuid")).thenReturn(null);
    when(request.getParameter("categoryUuid")).thenReturn(null);

    // Act
    Category result = controller.getSelectedMainCategory(request);

    // Assert
    assertNull(result);
    verify(repository, never()).findSubcategoryByUuid(anyString());
    verify(repository, never()).findByUuid(anyString());
  }

  @Test
  void testGetSubcategories_ReturnsSingleSubcategory_WhenSubcategoryUuidIsProvided() {
    // Arrange
    String subcategoryUuid = "sub123";
    when(request.getParameter("subcategoryUuid")).thenReturn(subcategoryUuid);

    Subcategory subcategory = new Subcategory();
    subcategory.setUuid(subcategoryUuid);

    when(repository.findSubcategoryByUuid(subcategoryUuid)).thenReturn(subcategory);

    // Act
    List<Subcategory> result = controller.getSubcategories(request);

    // Assert
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("sub123", result.get(0).getUuid());
    verify(repository).findSubcategoryByUuid(subcategoryUuid);
  }

  @Test
  void testGetSubcategories_ReturnsSubcategoriesList_WhenCategoryUuidIsProvided() {
    // Arrange
    String categoryUuid = "cat123";
    when(request.getParameter("subcategoryUuid")).thenReturn(null);
    when(request.getParameter("categoryUuid")).thenReturn(categoryUuid);

    Subcategory sub1 = new Subcategory();
    sub1.setUuid("sub1");

    Subcategory sub2 = new Subcategory();
    sub2.setUuid("sub2");

    List<Subcategory> subcategories = List.of(sub1, sub2);
    when(repository.getSubcategoriesByRootCategoryUuid(categoryUuid)).thenReturn(subcategories);

    // Act
    List<Subcategory> result = controller.getSubcategories(request);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("sub1", result.get(0).getUuid());
    assertEquals("sub2", result.get(1).getUuid());
    verify(repository).getSubcategoriesByRootCategoryUuid(categoryUuid);
  }

  @Test
  void testGetSubcategories_ReturnsEmptyList_WhenNoUuidsProvided() {
    // Arrange
    when(request.getParameter("subcategoryUuid")).thenReturn(null);
    when(request.getParameter("categoryUuid")).thenReturn(null);

    // Act
    List<Subcategory> result = controller.getSubcategories(request);

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(repository, never()).findSubcategoryByUuid(anyString());
    verify(repository, never()).getSubcategoriesByRootCategoryUuid(anyString());
  }

  @Test
  void testGetCategories_ReturnsValidMap() {
    // Arrange
    Category category1 = new Category();
    category1.setCategoryName("Category1");
    category1.setUuid("uuid1");

    Category category2 = new Category();
    category2.setCategoryName("Category2");
    category2.setUuid("uuid2");

    List<Category> categories = List.of(category1, category2);
    when(repository.getCategories()).thenReturn(categories);

    // Act
    Map<String, String> result = controller.getCategories();

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals("uuid1", result.get("Category1"));
    assertEquals("uuid2", result.get("Category2"));
    verify(repository).getCategories();
  }

  @Test
  void testGetCategories_ReturnsEmptyMap_WhenNoCategoriesAvailable() {
    // Arrange
    when(repository.getCategories()).thenReturn(Collections.emptyList());

    // Act
    Map<String, String> result = controller.getCategories();

    // Assert
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(repository).getCategories();
  }
}
