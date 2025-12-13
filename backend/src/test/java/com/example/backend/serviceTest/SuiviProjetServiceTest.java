package com.example.backend.serviceTest;

import com.example.backend.dto.SuiviProjetResponse;
import com.example.backend.dto.TacheResponse;
import com.example.backend.entity.*;
import com.example.backend.repository.ProjetRepository;
import com.example.backend.repository.TacheRepository;
import com.example.backend.service.SuiviProjetService;
import com.example.backend.service.TacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SuiviProjetServiceTest {

    @InjectMocks
    private SuiviProjetService suiviProjetService;

    @Mock
    private ProjetRepository projetRepository;

    @Mock
    private TacheRepository tacheRepository;

    @Mock
    private TacheService tacheService;

    private Projet projet;
    private Tache tache;
    private Consultant consultant;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Création du projet
        projet = new Projet();
        projet.setId(1L);
        projet.setNomProjet("Projet A");
        projet.setDateLimite(LocalDate.now().plusDays(5));

        // Création du consultant
        consultant = new Consultant();
        consultant.setId(10L);
        consultant.setNomComplet("Consultant Test");

        // Création de la tâche
        tache = new Tache();
        tache.setId(1L);
        tache.setProjet(projet);
        tache.setStatut(StatutTache.TERMINE);
        tache.setConsultant(consultant);
        tache.setNomTache("Tache 1");
        tache.setDateLimite(LocalDate.now().plusDays(2));
    }

    // =========================
    // getSuiviProjet
    // =========================
    @Test
    void getSuiviProjet_withCompletedTask_shouldReturnSuivi() {
        when(projetRepository.findById(1L)).thenReturn(Optional.of(projet));
        when(projetRepository.findByNomProjet("Projet A")).thenReturn(Optional.of(projet));
        when(tacheRepository.findByProjet_NomProjet("Projet A")).thenReturn(List.of(tache));
        when(tacheService.mapToResponse(tache)).thenReturn(new TacheResponse());

        SuiviProjetResponse response = suiviProjetService.getSuiviProjet(1L);

        assertNotNull(response);
        assertEquals("Projet A", response.getNomProjet());
        assertEquals(1, response.getNbTaches());
        assertEquals(100.0, response.getProgressionGlobale());
        assertEquals(1, response.getNbConsultants());
        assertEquals(0, response.getNbAFaire());
        assertEquals(0, response.getNbEnCours());
        assertEquals(1, response.getNbTermine());
        assertEquals(0, response.getNbBloque());
        assertEquals(0, response.getNbRetarde());
        assertEquals(1, response.getTaches().size());
    }

    @Test
    void getSuiviProjet_whenProjetNotFound_shouldThrow() {
        when(projetRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> suiviProjetService.getSuiviProjet(1L));
    }

    // =========================
    // getSuiviParProjet
    // =========================
    @Test
    void getSuiviParProjet_withMultipleTaches_shouldReturnSuivi() {
        Tache tache2 = new Tache();
        tache2.setId(2L);
        tache2.setProjet(projet);
        tache2.setStatut(StatutTache.EN_COURS);
        tache2.setConsultant(consultant);
        tache2.setNomTache("Tache 2");

        when(projetRepository.findByNomProjet("Projet A")).thenReturn(Optional.of(projet));
        when(tacheRepository.findByProjet_NomProjet("Projet A")).thenReturn(List.of(tache, tache2));
        when(tacheService.mapToResponse(any(Tache.class))).thenAnswer(invocation -> {
            Tache t = invocation.getArgument(0);
            TacheResponse resp = new TacheResponse();
            resp.setId(t.getId());
            resp.setNomTache(t.getNomTache());
            resp.setStatut(t.getStatut());
            return resp;
        });

        SuiviProjetResponse response = suiviProjetService.getSuiviParProjet("Projet A");

        assertNotNull(response);
        assertEquals("Projet A", response.getNomProjet());
        assertEquals(2, response.getNbTaches());
        assertEquals(50.0, response.getProgressionGlobale()); // 1 sur 2 terminées
    }

    @Test
    void getSuiviParProjet_whenProjetNotFound_shouldThrow() {
        when(projetRepository.findByNomProjet("Projet A")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> suiviProjetService.getSuiviParProjet("Projet A"));
    }

    // =========================
    // getAllProjets
    // =========================
    @Test
    void getAllProjets_shouldReturnNames() {
        when(projetRepository.findByChefProjetId(1L)).thenReturn(List.of(projet));

        var result = suiviProjetService.getAllProjets(1L);

        assertEquals(1, result.size());
        assertEquals("Projet A", result.get(0));
    }

    @Test
    void getAllProjets_whenNoProjet_shouldReturnEmptyList() {
        when(projetRepository.findByChefProjetId(1L)).thenReturn(List.of());

        var result = suiviProjetService.getAllProjets(1L);

        assertTrue(result.isEmpty());
    }
}
