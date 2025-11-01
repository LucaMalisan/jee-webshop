package src.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import src.model.ShoppingCart;
import src.repository.ShoppingCartRepository;

class ShoppingCartControllerTest {

    @Test
    void testGetShoppingCartEntries_ReturnsListOfShoppingCartEntries() {
        // Arrange
        ShoppingCartRepository shoppingCartRepository = mock(ShoppingCartRepository.class);
        AuthController authController = mock(AuthController.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        ShoppingCartController shoppingCartController = new ShoppingCartController(shoppingCartRepository, authController);

        String email = "test@example.com";
        ShoppingCart entry1 = new ShoppingCart();
        ShoppingCart entry2 = new ShoppingCart();
        List<ShoppingCart> mockEntries = Arrays.asList(entry1, entry2);

        when(authController.extractEmail(request)).thenReturn(email);
        when(shoppingCartRepository.getShoppingCartEntries(email)).thenReturn(mockEntries);

        // Act
        List<ShoppingCart> result = shoppingCartController.getShoppingCartEntries(request);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.contains(entry1));
        assertTrue(result.contains(entry2));

        verify(authController, times(1)).extractEmail(request);
        verify(shoppingCartRepository, times(1)).getShoppingCartEntries(email);
    }

    @Test
    void testGetShoppingCartEntries_ReturnsEmptyListIfNoEntries() {
        // Arrange
        ShoppingCartRepository shoppingCartRepository = mock(ShoppingCartRepository.class);
        AuthController authController = mock(AuthController.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        ShoppingCartController shoppingCartController = new ShoppingCartController(shoppingCartRepository, authController);
        String email = "empty@example.com";

        when(authController.extractEmail(request)).thenReturn(email);
        when(shoppingCartRepository.getShoppingCartEntries(email)).thenReturn(List.of());

        // Act
        List<ShoppingCart> result = shoppingCartController.getShoppingCartEntries(request);

        // Assert
        assertTrue(result.isEmpty());

        verify(authController, times(1)).extractEmail(request);
        verify(shoppingCartRepository, times(1)).getShoppingCartEntries(email);
    }

    @Test
    void testGetShoppingCartEntries_HandlesNullEmailFromAuthController() {
        // Arrange
        ShoppingCartRepository shoppingCartRepository = mock(ShoppingCartRepository.class);
        AuthController authController = mock(AuthController.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        ShoppingCartController shoppingCartController = new ShoppingCartController(shoppingCartRepository, authController);
        when(authController.extractEmail(request)).thenReturn(null);

        // Act
        List<ShoppingCart> result = shoppingCartController.getShoppingCartEntries(request);

        // Assert
        assertTrue(result.isEmpty());

        verify(authController, times(1)).extractEmail(request);
        verifyNoInteractions(shoppingCartRepository);
    }
}