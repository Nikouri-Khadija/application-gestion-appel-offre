package com.example.backend.exceptionTest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IllegalArgumentExceptionTest {

    @Test
    void testExceptionMessage() {
        String message = "Code projet déjà utilisé";
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> { throw new IllegalArgumentException(message); }
        );

        assertEquals(message, exception.getMessage());
    }
}

