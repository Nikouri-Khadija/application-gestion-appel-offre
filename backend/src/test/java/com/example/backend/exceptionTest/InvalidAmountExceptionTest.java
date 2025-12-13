package com.example.backend.exceptionTest;

import com.example.backend.exception.InvalidAmountException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvalidAmountExceptionTest {

    @Test
    void testExceptionMessage() {
        String message = "Montant invalide";
        InvalidAmountException exception = assertThrows(
                InvalidAmountException.class,
                () -> { throw new InvalidAmountException(message); }
        );

        assertEquals(message, exception.getMessage());
    }
}
