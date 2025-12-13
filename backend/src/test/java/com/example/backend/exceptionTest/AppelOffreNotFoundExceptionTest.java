package com.example.backend.exceptionTest;

import com.example.backend.exception.AppelOffreNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppelOffreNotFoundExceptionTest {

    @Test
    void testExceptionMessage() {
        String message = "Appel non trouvé";
        AppelOffreNotFoundException exception = assertThrows(
                AppelOffreNotFoundException.class,
                () -> { throw new AppelOffreNotFoundException(message); }
        );

        assertEquals(message, exception.getMessage());
    }
}

