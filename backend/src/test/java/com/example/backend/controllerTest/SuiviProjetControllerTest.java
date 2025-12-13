package com.example.backend.controllerTest;

import com.example.backend.controller.SuiviProjetController;
import com.example.backend.dto.SuiviProjetResponse;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.SuiviProjetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SuiviProjetControllerTest {

    @InjectMocks
    private SuiviProjetController suiviProjetController;

    @Mock
    private SuiviProjetService suiviProjetService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    private User chef;
    private SuiviProjetResponse suivi;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        chef = new User();
        chef.setId(1L);
        chef.setEmail("chef@example.com");

        suivi = new SuiviProjetResponse();
        suivi.setNomProjet("Projet Test");
        suivi.setProgressionGlobale(50);
    }

    @Test
    void testGetSuiviProjet() {
        when(suiviProjetService.getSuiviParProjet("Projet Test")).thenReturn(suivi);

        ResponseEntity<SuiviProjetResponse> response = suiviProjetController.getSuiviProjet("Projet Test");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Projet Test", response.getBody().getNomProjet());
        verify(suiviProjetService, times(1)).getSuiviParProjet("Projet Test");
    }

    @Test
    void testGetNomProjetsParChef() {
        when(authentication.getName()).thenReturn("chef@example.com");
        when(userRepository.findByEmail("chef@example.com")).thenReturn(Optional.of(chef));
        when(suiviProjetService.getAllProjets(1L)).thenReturn(Arrays.asList("Projet1", "Projet2"));

        ResponseEntity<List<String>> response = suiviProjetController.getNomProjetsParChef(authentication);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().contains("Projet1"));
        verify(userRepository, times(1)).findByEmail("chef@example.com");
        verify(suiviProjetService, times(1)).getAllProjets(1L);
    }

    @Test
    void testGetNomProjetsParChef_chefIntrouvable() {
        when(authentication.getName()).thenReturn("inconnu@example.com");
        when(userRepository.findByEmail("inconnu@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> suiviProjetController.getNomProjetsParChef(authentication));

        assertEquals("Chef introuvable", exception.getMessage());
        verify(userRepository, times(1)).findByEmail("inconnu@example.com");
        verifyNoInteractions(suiviProjetService);
    }
}
