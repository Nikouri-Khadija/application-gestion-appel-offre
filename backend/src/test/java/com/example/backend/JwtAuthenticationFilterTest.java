package com.example.backend;

import com.example.backend.config.JwtAuthenticationFilter;
import com.example.backend.config.JwtUtils;
import com.example.backend.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {

    private JwtUtils jwtUtils;
    private CustomUserDetailsService userDetailsService;
    private JwtAuthenticationFilter filter;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @BeforeEach
    void setup() {
        jwtUtils = mock(JwtUtils.class);
        userDetailsService = mock(CustomUserDetailsService.class);
        filter = new JwtAuthenticationFilter(jwtUtils, userDetailsService);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        chain = mock(FilterChain.class);

        SecurityContextHolder.clearContext();
    }

    // =========================
    // Test passage sans Authorization header
    // =========================
    @Test
    void doFilter_noHeader_shouldCallChain() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/api/test");

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // =========================
    // Test passage OPTIONS method (autorisé)
    // =========================
    @Test
    void doFilter_optionsMethod_shouldCallChain() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getMethod()).thenReturn("OPTIONS");

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // =========================
    // Test token valide
    // =========================
    @Test
    void doFilter_validToken_shouldAuthenticateUser() throws IOException, ServletException {
        String token = "valid-token";
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());

        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtils.extractUsername(token)).thenReturn("testuser");
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
        when(jwtUtils.validateToken(token, userDetails)).thenReturn(true);

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("testuser", SecurityContextHolder.getContext().getAuthentication().getName());
    }

    // =========================
    // Test token invalide / exception
    // =========================
    @Test
    void doFilter_invalidToken_shouldSendUnauthorized() throws IOException, ServletException {
        String token = "invalid-token";

        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        doThrow(new RuntimeException("Token invalide")).when(jwtUtils).extractUsername(token);

        filter.doFilter(request, response, chain);

        verify(response, times(1)).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT invalide ou expiré");
        // Chain ne doit pas être appelé après erreur
        verify(chain, times(0)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // =========================
    // Test passage des fichiers PDF (autorisé)
    // =========================
    @Test
    void doFilter_pdfPath_shouldCallChainWithoutAuth() throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn("/api/files/test.pdf");

        filter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
