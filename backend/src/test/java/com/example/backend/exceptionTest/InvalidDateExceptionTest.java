package com.example.backend.exceptionTest;

import com.example.backend.exception.InvalidDateException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidDateExceptionTest {

    @Test
    void testExceptionWithMessage() {
        // Given
        String message = "Format de date invalide. Utiliser 'yyyy-MM-dd'.";

        // When
        InvalidDateException exception = new InvalidDateException(message);

        // Then
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause()); // Pas de cause car constructeur à 1 paramètre
    }

    @Test
    void testExceptionThrownInCode() {
        // Given
        String errorMessage = "Format de date invalide. Utiliser 'yyyy-MM-dd'.";

        // When
        InvalidDateException exception = assertThrows(
                InvalidDateException.class,
                () -> { throw new InvalidDateException(errorMessage); }
        );

        // Then
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testExceptionInheritance() {
        // Given
        InvalidDateException exception = new InvalidDateException("Test");

        // Then
        assertInstanceOf(RuntimeException.class, exception);
        assertInstanceOf(Exception.class, exception);
        assertInstanceOf(Throwable.class, exception);
    }

    @Test
    void testExceptionToStringContainsMessage() {
        // Given
        String message = "Format de date invalide";
        InvalidDateException exception = new InvalidDateException(message);

        // When
        String toStringResult = exception.toString();

        // Then
        assertTrue(toStringResult.contains(message));
        assertTrue(toStringResult.contains("InvalidDateException"));
    }

    @Test
    void testExceptionWithNullMessage() {
        // When
        InvalidDateException exception = new InvalidDateException(null);

        // Then
        assertNull(exception.getMessage());
    }

    @Test
    void testExceptionWithEmptyMessage() {
        // Given
        String message = "";

        // When
        InvalidDateException exception = new InvalidDateException(message);

        // Then
        assertEquals("", exception.getMessage());
    }

}