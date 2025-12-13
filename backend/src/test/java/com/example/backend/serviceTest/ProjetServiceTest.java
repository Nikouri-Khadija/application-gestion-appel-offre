package com.example.backend.serviceTest;

import com.example.backend.dto.ProjetRequest;
import com.example.backend.entity.AppelOffre;
import com.example.backend.entity.Projet;
import com.example.backend.entity.Statut;
import com.example.backend.entity.User;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.AppelOffreRepository;
import com.example.backend.repository.ProjetRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.NotificationService;
import com.example.backend.service.ProjetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProjetServiceTest {

    @InjectMocks
    private ProjetService projetService;

    @Mock
    private ProjetRepository projetRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private AppelOffreRepository appelOffreRepository;

    private ProjetRequest request;
    private User chef;
    private Projet projet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        chef = new User();
        chef.setId(1L);
        chef.setEmail("chef@test.com");

        projet = new Projet();
        projet.setId(1L);
        projet.setNomProjet("Projet A");
        projet.setCodeProjet("P001");
        projet.setChefProjet(chef);
        projet.setDateCreation(LocalDate.now());
        projet.setDateLimite(LocalDate.now().plusDays(10));
        projet.setDescription("Description");

        request = new ProjetRequest();
        request.setNomProjet("Projet A");
        request.setCodeProjet("P001");
        request.setChefId(1L);
        request.setDateCreation(LocalDate.now());
        request.setDateLimite(LocalDate.now().plusDays(10));
        request.setDescription("Description");
    }

    // =========================
    // Ajouter Projet
    // =========================
    @Test
    void ajouterProjet_shouldReturnSavedProjet_withAppels() {
        when(projetRepository.existsByCodeProjet(request.getCodeProjet())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(chef));
        when(projetRepository.save(any(Projet.class))).thenReturn(projet);
        when(appelOffreRepository.findByStatut(Statut.VALIDE))
                .thenReturn(List.of(new AppelOffre(), new AppelOffre()));
        when(appelOffreRepository.countByStatut(Statut.EN_ATTENTE)).thenReturn(2L);

        Projet result = projetService.ajouterProjet(request);

        assertNotNull(result);
        verify(notificationService, times(1)).envoyer(anyString(), eq(chef.getEmail()));
    }

    @Test
    void ajouterProjet_shouldReturnSavedProjet_withEmptyAppels() {
        when(projetRepository.existsByCodeProjet(request.getCodeProjet())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(chef));
        when(projetRepository.save(any(Projet.class))).thenReturn(projet);
        when(appelOffreRepository.findByStatut(Statut.VALIDE)).thenReturn(Collections.emptyList());
        when(appelOffreRepository.countByStatut(Statut.EN_ATTENTE)).thenReturn(0L);

        Projet result = projetService.ajouterProjet(request);

        assertNotNull(result);
        verify(notificationService, times(1)).envoyer(anyString(), eq(chef.getEmail()));
    }

    @Test
    void ajouterProjet_whenCodeExists_shouldThrowException() {
        when(projetRepository.existsByCodeProjet(request.getCodeProjet())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> projetService.ajouterProjet(request));
    }

    @Test
    void ajouterProjet_whenChefNotFound_shouldThrowException() {
        when(projetRepository.existsByCodeProjet(request.getCodeProjet())).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> projetService.ajouterProjet(request));
    }

    // =========================
    // Modifier Projet
    // =========================
    @Test
    void modifierProjet_shouldReturnUpdatedProjet() {
        // Modifier les valeurs pour couvrir les setters
        request.setNomProjet("Projet B");
        request.setCodeProjet("P002");
        request.setDescription("Nouvelle description");
        when(projetRepository.findById(1L)).thenReturn(Optional.of(projet));
        when(userRepository.findById(1L)).thenReturn(Optional.of(chef));
        when(projetRepository.save(any(Projet.class))).thenReturn(projet);

        Projet result = projetService.modifierProjet(1L, request);

        assertNotNull(result);
        assertEquals("Projet B", result.getNomProjet());
        assertEquals("P002", result.getCodeProjet());
        assertEquals("Nouvelle description", result.getDescription());
        verify(notificationService, times(1)).envoyer(anyString(), eq(chef.getEmail()));
    }

    @Test
    void modifierProjet_whenProjetNotFound_shouldThrowException() {
        when(projetRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> projetService.modifierProjet(1L, request));
    }

    @Test
    void modifierProjet_whenChefNotFound_shouldThrowException() {
        when(projetRepository.findById(1L)).thenReturn(Optional.of(projet));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> projetService.modifierProjet(1L, request));
    }

    // =========================
    // Supprimer Projet
    // =========================
    @Test
    void supprimerProjet_shouldCallDelete() {
        when(projetRepository.findById(1L)).thenReturn(Optional.of(projet));
        projetService.supprimerProjet(1L);
        verify(projetRepository, times(1)).delete(projet);
    }

    @Test
    void supprimerProjet_whenProjetNotFound_shouldThrowException() {
        when(projetRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> projetService.supprimerProjet(1L));
    }

    // =========================
    // Get Compteurs
    // =========================
    @Test
    void getCompteurs_shouldReturnMap() {
        when(appelOffreRepository.countByStatut(Statut.EN_ATTENTE)).thenReturn(3L);
        when(projetRepository.count()).thenReturn(5L);

        Map<String, Integer> result = projetService.getCompteurs();

        assertEquals(3, result.get("enCours"));
        assertEquals(5, result.get("projets"));
    }

    // =========================
    // Get All Projets
    // =========================
    @Test
    void getAllProjets_shouldReturnListWithMultipleItems() {
        Projet projet2 = new Projet();
        projet2.setId(2L);
        projet2.setNomProjet("Projet B");
        when(projetRepository.findAll()).thenReturn(List.of(projet, projet2));

        List<Projet> result = projetService.getAllProjets();

        assertEquals(2, result.size());
    }

    @Test
    void getAllProjets_shouldReturnEmptyList() {
        when(projetRepository.findAll()).thenReturn(Collections.emptyList());

        List<Projet> result = projetService.getAllProjets();

        assertTrue(result.isEmpty());
    }

    // =========================
    // Get Projets Par Chef
    // =========================
    @Test
    void getProjetsParChef_shouldReturnListWithMultipleItems() {
        Projet projet2 = new Projet();
        projet2.setId(2L);
        projet2.setNomProjet("Projet B");
        when(projetRepository.findByChefProjetId(1L)).thenReturn(List.of(projet, projet2));

        List<Projet> result = projetService.getProjetsParChef(1L);

        assertEquals(2, result.size());
    }

    @Test
    void getProjetsParChef_shouldReturnEmptyList() {
        when(projetRepository.findByChefProjetId(1L)).thenReturn(Collections.emptyList());

        List<Projet> result = projetService.getProjetsParChef(1L);

        assertTrue(result.isEmpty());
    }
}
