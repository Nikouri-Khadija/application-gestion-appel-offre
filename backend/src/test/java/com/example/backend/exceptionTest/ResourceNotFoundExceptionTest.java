package com.example.backend.exceptionTest;

import com.example.backend.exception.ResourceNotFoundException;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    @Test
    void testExceptionMessage() {
        String message = "Ressource non trouvée";
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> { throw new ResourceNotFoundException(message); }
        );

        assertEquals(message, exception.getMessage());
    }

}
