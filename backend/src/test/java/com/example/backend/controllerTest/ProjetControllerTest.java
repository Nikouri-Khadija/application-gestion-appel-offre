package com.example.backend.controllerTest;

import com.example.backend.controller.ProjetController;
import com.example.backend.dto.ProjetRequest;
import com.example.backend.entity.Projet;
import com.example.backend.entity.User;
import com.example.backend.service.ProjetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ProjetControllerTest {

    @Mock
    private ProjetService projetService;

    @InjectMocks
    private ProjetController projetController;

    private Projet projet;
    private ProjetRequest projetRequest;
    private User chef;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        chef = new User();
        chef.setId(1L);
        chef.setEmail("chef@example.com");

        projet = new Projet();
        projet.setId(1L);
        projet.setNomProjet("Projet Test");
        projet.setCodeProjet("P001");
        projet.setChefProjet(chef);
        projet.setDateCreation(LocalDate.now());
        projet.setDateLimite(LocalDate.now().plusDays(30));
        projet.setDescription("Description test");

        projetRequest = new ProjetRequest();
        projetRequest.setNomProjet("Projet Test");
        projetRequest.setCodeProjet("P001");
        projetRequest.setChefId(1L);
        projetRequest.setDateCreation(LocalDate.now());
        projetRequest.setDateLimite(LocalDate.now().plusDays(30));
        projetRequest.setDescription("Description test");
    }

    @Test
    void testAjouterProjet() {
        when(projetService.ajouterProjet(projetRequest)).thenReturn(projet);

        ResponseEntity<Projet> response = projetController.ajouterProjet(projetRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(projet, response.getBody());
        verify(projetService, times(1)).ajouterProjet(projetRequest);
    }

    @Test
    void testGetAllProjets() {
        when(projetService.getAllProjets()).thenReturn(Arrays.asList(projet));

        List<Projet> result = projetController.getAllProjets();

        assertEquals(1, result.size());
        assertEquals(projet, result.get(0));
        verify(projetService, times(1)).getAllProjets();
    }

    @Test
    void testSupprimerProjet() {
        doNothing().when(projetService).supprimerProjet(1L);

        projetController.supprimerProjet(1L);

        verify(projetService, times(1)).supprimerProjet(1L);
    }

    @Test
    void testModifierProjet() {
        when(projetService.modifierProjet(1L, projetRequest)).thenReturn(projet);

        Projet result = projetController.modifierProjet(1L, projetRequest);

        assertEquals(projet, result);
        verify(projetService, times(1)).modifierProjet(1L, projetRequest);
    }

    @Test
    void testGetProjetsParChef() {
        when(projetService.getProjetsParChef(1L)).thenReturn(Arrays.asList(projet));

        List<Projet> result = projetController.getProjetsParChef(1L);

        assertEquals(1, result.size());
        assertEquals(projet, result.get(0));
        verify(projetService, times(1)).getProjetsParChef(1L);
    }

    @Test
    void testGetCompteurs() {
        Map<String, Integer> compteurs = new HashMap<>();
        compteurs.put("enCours", 2);
        compteurs.put("projets", 5);

        when(projetService.getCompteurs()).thenReturn(compteurs);

        ResponseEntity<Map<String, Integer>> response = projetController.getCompteurs();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(compteurs, response.getBody());
        verify(projetService, times(1)).getCompteurs();
    }
}
