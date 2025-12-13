package com.example.backend;


import com.example.backend.config.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private UserDetails userDetails;

    @BeforeEach
    void setup() {
        jwtUtils = new JwtUtils();

        userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userDetails.getAuthorities()).thenReturn(Collections.emptyList());
    }

    // =========================
    // Test génération de token
    // =========================
    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtUtils.generateToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    // =========================
    // Test extraction username
    // =========================
    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtUtils.generateToken(userDetails);
        String username = jwtUtils.extractUsername(token);
        assertEquals("testuser", username);
    }

    // =========================
    // Test validation token valide
    // =========================
    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtUtils.generateToken(userDetails);
        boolean isValid = jwtUtils.validateToken(token, userDetails);
        assertTrue(isValid);
    }

    // =========================
    // Test validation token pour un autre utilisateur
    // =========================
    @Test
    void validateToken_shouldReturnFalseForWrongUser() {
        String token = jwtUtils.generateToken(userDetails);

        UserDetails anotherUser = mock(UserDetails.class);
        when(anotherUser.getUsername()).thenReturn("wronguser");
        when(anotherUser.getAuthorities()).thenReturn(Collections.emptyList());

        boolean isValid = jwtUtils.validateToken(token, anotherUser);
        assertFalse(isValid);
    }

    // =========================
    // Test token expiré
    // =========================
    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        // Générer un token expiré
        String expiredToken = io.jsonwebtoken.Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new java.util.Date(System.currentTimeMillis() - 2000)) // 2s avant
                .setExpiration(new java.util.Date(System.currentTimeMillis() - 1000)) // expiré
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(
                                "ma-cle-secrete-très-longue-et-sécurisée-pour-jwt-hs512-1234567890abcdef".getBytes()),
                        io.jsonwebtoken.SignatureAlgorithm.HS512)
                .compact();

        boolean isValid;
        try {
            isValid = jwtUtils.validateToken(expiredToken, userDetails);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            isValid = false; // Token expiré → invalide
        }

        assertFalse(isValid);
    }
}

