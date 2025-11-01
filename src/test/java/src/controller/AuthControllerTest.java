package src.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import src.model.User;
import src.repository.UserRepository;

class AuthControllerTest {

  @BeforeEach
  public void init() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test class for {@link AuthController}.
   *
   * <p>This class contains unit tests for the {@code mailExistsAndIsConfirmed} method. The method
   * checks if the email extracted from a request exists in the repository and is confirmed. Each
   * test verifies specific scenarios.
   */
  @Test
  void testMailExistsAndIsConfirmed_EmailConfirmed_ReturnsTrue() {
    // Arrange
    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    UserRepository mockRepository = Mockito.mock(UserRepository.class);
    AuthController authController = spy(new AuthController(mockRepository));

    String jwtToken = "valid.jwt.token";
    String email = "test@example.com";
    Cookie[] cookies = {new Cookie("jwt", jwtToken)};

    when(mockRequest.getCookies()).thenReturn(cookies);
    when(mockRepository.findByEmail(email)).thenReturn(new User(email, UUID.randomUUID().toString(), true));
    when(authController.extractEmail(jwtToken)).thenReturn(email);

    // Act
    boolean result = authController.mailExistsAndIsConfirmed(mockRequest);

    // Assert
    assertTrue(result);
  }

  @Test
  void testMailExistsAndIsConfirmed_UserNotFound_ReturnsFalse() {
    // Arrange
    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    UserRepository mockRepository = Mockito.mock(UserRepository.class);
    AuthController authController = spy(new AuthController(mockRepository));

    String jwtToken = "valid.jwt.token";
    String email = "test@example.com";
    Cookie[] cookies = {new Cookie("jwt", jwtToken)};

    when(mockRequest.getCookies()).thenReturn(cookies);
    when(mockRepository.findByEmail(email)).thenReturn(null);
    when(authController.extractEmail(jwtToken)).thenReturn(email);

    // Act
    boolean result = authController.mailExistsAndIsConfirmed(mockRequest);

    // Assert
    assertFalse(result);
  }

  @Test
  void testMailExistsAndIsConfirmed_UserNotConfirmed_ReturnsFalse() {
    // Arrange
    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    UserRepository mockRepository = Mockito.mock(UserRepository.class);
    AuthController authController = spy(new AuthController(mockRepository));

    String jwtToken = "valid.jwt.token";
    String email = "test@example.com";
    Cookie[] cookies = {new Cookie("jwt", jwtToken)};

    when(mockRequest.getCookies()).thenReturn(cookies);
    when(mockRepository.findByEmail(email)).thenReturn(new User(email, UUID.randomUUID().toString(), false));
    when(authController.extractEmail(jwtToken)).thenReturn(email);

    // Act
    boolean result = authController.mailExistsAndIsConfirmed(mockRequest);

    // Assert
    assertFalse(result);
  }

  @Test
  void testMailExistsAndIsConfirmed_InvalidToken_ReturnsFalse() {
    // Arrange
    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    UserRepository mockRepository = Mockito.mock(UserRepository.class);
    AuthController authController = spy(new AuthController(mockRepository));

    String jwtToken = "invalid.jwt.token";
    Cookie[] cookies = {new Cookie("jwt", jwtToken)};

    when(mockRequest.getCookies()).thenReturn(cookies);
    when(authController.extractEmail(jwtToken)).thenReturn(null);

    // Act
    boolean result = authController.mailExistsAndIsConfirmed(mockRequest);

    // Assert
    assertFalse(result);
  }

  @Test
  void testMailExistsAndIsConfirmed_NoJwtCookie_ReturnsFalse() {
    // Arrange
    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    UserRepository mockRepository = Mockito.mock(UserRepository.class);
    AuthController authController = spy(new AuthController(mockRepository));

    when(mockRequest.getCookies()).thenReturn(null);

    // Act
    boolean result = authController.mailExistsAndIsConfirmed(mockRequest);

    // Assert
    assertFalse(result);
  }

  @Test
  void testGetCookieByName_CookieExists_ReturnsValue() {
    // Arrange
    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    AuthController authController = new AuthController();

    Cookie[] cookies = {new Cookie("jwt", "valid.jwt.token"), new Cookie("session", "session_id")};
    when(mockRequest.getCookies()).thenReturn(cookies);

    // Act
    String result = authController.getCookieByName(mockRequest, "jwt");

    // Assert
    assertTrue(result.equals("valid.jwt.token"));
  }

  @Test
  void testGetCookieByName_CookieNotExists_ReturnsNull() {
    // Arrange
    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    AuthController authController = new AuthController();

    Cookie[] cookies = {new Cookie("session", "session_id")};
    when(mockRequest.getCookies()).thenReturn(cookies);

    // Act
    String result = authController.getCookieByName(mockRequest, "jwt");

    // Assert
    assertTrue(result == null);
  }

  @Test
  void testGetCookieByName_NoCookies_ReturnsNull() {
    // Arrange
    HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);
    AuthController authController = new AuthController();

    when(mockRequest.getCookies()).thenReturn(null);

    // Act
    String result = authController.getCookieByName(mockRequest, "jwt");

    // Assert
    assertTrue(result == null);
  }
}
