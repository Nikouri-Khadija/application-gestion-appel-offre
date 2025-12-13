package com.example.backend.controllerTest;

import com.example.backend.controller.UserController;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserRepository userRepository;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testGetChefs() {
        User chef1 = new User();
        chef1.setId(1L);
        chef1.setEmail("chef1@example.com");
        chef1.setRole(Role.CHEF_DE_PROJET);

        User chef2 = new User();
        chef2.setId(2L);
        chef2.setEmail("chef2@example.com");
        chef2.setRole(Role.CHEF_DE_PROJET);

        when(userRepository.findByRole(Role.CHEF_DE_PROJET)).thenReturn(List.of(chef1, chef2));

        List<User> result = userController.getChefs();

        assertEquals(2, result.size());
        assertEquals("chef1@example.com", result.get(0).getEmail());
        verify(userRepository, times(1)).findByRole(Role.CHEF_DE_PROJET);
    }

    @Test
    void testGetUserByEmailFound() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setRole(Role.CHEF_DE_PROJET);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        ResponseEntity<User> response = userController.getUserByEmail("user@example.com");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("user@example.com", response.getBody().getEmail());
        verify(userRepository, times(1)).findByEmail("user@example.com");
    }

    @Test
    void testGetUserByEmailNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.getUserByEmail("unknown@example.com");

        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());
        verify(userRepository, times(1)).findByEmail("unknown@example.com");
    }
}
