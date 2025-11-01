package src.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import org.junit.jupiter.api.Test;

public class ArticleTest {

  /**
   * Tests for the getDiscountPercent() method in the Article class. This method calculates the
   * percentage discount between the selling price and the list price of the article.
   */
  @Test
  public void testGetDiscountPercentWhenListPriceIsNull() {
    // Arrange
    Article article = new Article();
    article.setSellingPrice(100.0);

    // Act
    String discountPercent = article.getDiscountPercent();

    // Assert
    assertNull(discountPercent, "Discount percent should be null when list price is null");
  }

  @Test
  public void testGetDiscountPercentWhenSellingPriceIsEqualToListPrice() {
    // Arrange
    Article article = new Article();
    article.setSellingPrice(100.0);
    article.setListPrice(100.0);

    // Act
    String discountPercent = article.getDiscountPercent();

    // Assert
    assertEquals(
        "0", discountPercent, "When selling price equals list price, discount percent should be 0");
  }

  @Test
  public void testGetDiscountPercentWhenSellingPriceIsLessThanListPrice() {
    // Arrange
    Article article = new Article();
    article.setSellingPrice(80.0);
    article.setListPrice(100.0);

    // Act
    String discountPercent = article.getDiscountPercent();

    // Assert
    assertEquals(
        "20",
        discountPercent,
        "When selling price is 80 and list price is 100, discount percent should be 20");
  }

  @Test
  public void testGetDiscountPercentWhenSellingPriceIsGreaterThanListPrice() {
    // Arrange
    Article article = new Article();
    article.setSellingPrice(120.0);
    article.setListPrice(100.0);

    // Act
    String discountPercent = article.getDiscountPercent();

    // Assert
    assertEquals(
        "-20",
        discountPercent,
        "When selling price is 120 and list price is 100, discount percent should be -20");
  }

  @Test
  public void testFormatPriceWithListPriceAndAvailable() {
    // Arrange
    Article article = new Article();
    article.setSellingPrice(80.0);
    article.setListPrice(100.0);
    article.setAvailable(true);

    // Act
    String formattedPrice = article.formatPrice();

    // Assert
    assertEquals(
        "80.00 was 100.00 CHF",
        formattedPrice,
        "When listPrice is not null and the article is available, it should return the formatted price with 'was' text");
  }

  @Test
  public void testFormatPriceWithoutListPrice() {
    // Arrange
    Article article = new Article();
    article.setSellingPrice(80.0);
    article.setListPrice(null);
    article.setAvailable(true);

    // Act
    String formattedPrice = article.formatPrice();

    // Assert
    assertEquals(
        "80.00 CHF",
        formattedPrice,
        "When listPrice is null, it should not include the 'was' text in the formatted price");
  }

  @Test
  public void testFormatPriceWhenNotAvailable() {
    // Arrange
    Article article = new Article();
    article.setSellingPrice(80.0);
    article.setListPrice(100.0);
    article.setAvailable(false);

    // Act
    String formattedPrice = article.formatPrice();

    // Assert
    assertEquals(
        "80.00 CHF",
        formattedPrice,
        "When the article is not available, it should not include the 'was' text in the formatted price");
  }

  @Test
  public void testGetPrimaryImageURLWhenPrimaryImageExists() {
    // Arrange
    ArticleImage primaryImage = new ArticleImage();
    primaryImage.setPosition(1);
    primaryImage.setImageURL("https://example.com/primary.jpg");

    ArticleImage otherImage = new ArticleImage();
    otherImage.setPosition(2);
    otherImage.setImageURL("https://example.com/secondary.jpg");

    Article article = new Article();
    article.setImageList(List.of(primaryImage, otherImage));

    // Act
    String primaryImageURL = article.getPrimaryImageURL();

    // Assert
    assertEquals(
        "https://example.com/primary.jpg",
        primaryImageURL,
        "The primary image URL should match the URL of the image with position 1");
  }

  @Test
  public void testGetPrimaryImageURLWhenPrimaryImageDoesNotExist() {
    // Arrange
    ArticleImage nonPrimaryImage = new ArticleImage();
    nonPrimaryImage.setPosition(2);
    nonPrimaryImage.setImageURL("https://example.com/secondary.jpg");

    Article article = new Article();
    article.setImageList(List.of(nonPrimaryImage));

    // Act
    String primaryImageURL = article.getPrimaryImageURL();

    // Assert
    assertEquals(
        "",
        primaryImageURL,
        "If no image with position 1 exists, the primary image URL should be an empty string");
  }

  @Test
  public void testGetPrimaryImageURLWhenImageListIsEmpty() {
    // Arrange
    Article article = new Article();
    article.setImageList(List.of());

    // Act
    String primaryImageURL = article.getPrimaryImageURL();

    // Assert
    assertEquals(
        "",
        primaryImageURL,
        "If the image list is empty, the primary image URL should be an empty string");
  }
}
