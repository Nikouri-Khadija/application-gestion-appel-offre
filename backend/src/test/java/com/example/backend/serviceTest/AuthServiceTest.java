package com.example.backend.serviceTest;


import com.example.backend.config.JwtUtils;
import com.example.backend.dto.AuthRequest;
import com.example.backend.dto.AuthResponse;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AuthService;
import com.example.backend.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository repo;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoginSuccess() {
        AuthRequest request = new AuthRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(Role.ADMIN);

        UserDetails userDetails = mock(UserDetails.class);
        String token = "fake-jwt-token";

        // Mock AuthenticationManager
        Authentication auth = mock(Authentication.class);
        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        // Mock UserRepository
        when(repo.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        // Mock UserDetailsService
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);

        // Mock JwtUtils
        when(jwtUtils.generateToken(userDetails)).thenReturn(token);

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo(token);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getRole()).isEqualTo(Role.ADMIN);

        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(repo).findByEmail("test@example.com");
        verify(userDetailsService).loadUserByUsername("test@example.com");
        verify(jwtUtils).generateToken(userDetails);
    }

    @Test
    void testLoginUserNotFound() {
        AuthRequest request = new AuthRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("password");

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));

        when(repo.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.login(request));
    }

    @Test
    void testLoginAuthenticationFails() {
        AuthRequest request = new AuthRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        assertThrows(RuntimeException.class, () -> authService.login(request));

        verify(repo, never()).findByEmail(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtUtils, never()).generateToken(any());
    }
}

