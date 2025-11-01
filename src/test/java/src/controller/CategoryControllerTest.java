package src.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
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
}
