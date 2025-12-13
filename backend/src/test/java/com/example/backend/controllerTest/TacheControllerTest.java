package com.example.backend.controllerTest;

import com.example.backend.controller.TacheController;
import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import com.example.backend.service.TacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TacheControllerTest {

    @InjectMocks
    private TacheController controller;

    @Mock private TacheService tacheService;
    @Mock private ConsultantRepository consultantRepository;
    @Mock private UserRepository userRepository;
    @Mock private TacheRepository tacheRepository;
    @Mock private ProjetRepository projetRepository;

    private Tache tache;
    private Consultant consultant;
    private User user;
    private Projet projet;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();

        // Setup User
        user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");
        user.setNomComplet("John Doe");
        user.setRole(Role.CONSULTANT);

        // Setup Projet
        projet = new Projet();
        projet.setId(1L);
        projet.setNomProjet("Projet Test");

        // Setup Consultant - CORRECTION: Ajouter setNomComplet
        consultant = new Consultant();
        consultant.setId(1L);
        consultant.setUser(user);
        consultant.setProjet(projet);
        consultant.setNomComplet("John Doe"); // AJOUTÉ

        // Setup Tache
        tache = new Tache();
        tache.setId(1L);
        tache.setNomTache("Tâche Test");
        tache.setConsultant(consultant);
        tache.setProjet(projet);
        tache.setDateAffectation(LocalDate.now());
        tache.setDateLimite(LocalDate.now().plusDays(7));
        tache.setDescription("Description test");
        tache.setCommentaire("Commentaire test");
        tache.setPriorite(Priority.MOYENNE);
        tache.setStatut(StatutTache.A_FAIRE);
    }

    // =====================================================
    // TESTS pour updateDetails - TOUTES les branches
    // =====================================================

    @Test
    void updateDetails_successFullUpdate() {
        // Given
        TacheUpdateRequest request = new TacheUpdateRequest();
        request.setDateAffectation("2024-01-01");
        request.setDateLimite("2024-12-31");
        request.setConsultantId(1L);
        request.setProjetId(1L);
        request.setPriorite(Priority.ELEVEE);
        request.setDescription("Nouvelle description");
        request.setCommentaire("Nouveau commentaire");

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(consultantRepository.findByUserId(1L)).thenReturn(List.of(consultant));
        when(projetRepository.findById(1L)).thenReturn(Optional.of(projet));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse tacheResponse = new TacheResponse();
        tacheResponse.setId(1L);
        when(tacheService.mapToResponse(any(Tache.class))).thenReturn(tacheResponse);

        // When
        ResponseEntity<TacheResponse> response = controller.updateDetails(1L, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(tacheRepository).findById(1L);
        verify(consultantRepository).findByUserId(1L);
        verify(projetRepository).findById(1L);
        verify(tacheRepository).save(any(Tache.class));
        verify(tacheService).mapToResponse(tache);
    }

    @Test
    void updateDetails_tacheNotFound() {
        // Given
        TacheUpdateRequest request = new TacheUpdateRequest();
        when(tacheRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> controller.updateDetails(1L, request));
        assertEquals("Tâche non trouvée", exception.getMessage());

        verify(tacheRepository).findById(1L);
        verifyNoInteractions(consultantRepository, projetRepository, tacheService);
    }

    @Test
    void updateDetails_dateAffectationNull() {
        // Given
        TacheUpdateRequest request = new TacheUpdateRequest();
        request.setDateAffectation(null);
        request.setDateLimite("2024-12-31");

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse tacheResponse = new TacheResponse();
        when(tacheService.mapToResponse(any(Tache.class))).thenReturn(tacheResponse);

        // When
        ResponseEntity<TacheResponse> response = controller.updateDetails(1L, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tacheRepository).save(tache);
        // La date d'affectation ne doit pas être modifiée
        assertNotNull(tache.getDateAffectation());
    }

    @Test
    void updateDetails_dateAffectationBlank() {
        // Given
        TacheUpdateRequest request = new TacheUpdateRequest();
        request.setDateAffectation("");
        request.setDateLimite("2024-12-31");

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse tacheResponse = new TacheResponse();
        when(tacheService.mapToResponse(any(Tache.class))).thenReturn(tacheResponse);

        // When
        ResponseEntity<TacheResponse> response = controller.updateDetails(1L, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tacheRepository).save(tache);
    }

    @Test
    void updateDetails_dateLimiteNull() {
        // Given
        TacheUpdateRequest request = new TacheUpdateRequest();
        request.setDateAffectation("2024-01-01");
        request.setDateLimite(null);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse tacheResponse = new TacheResponse();
        when(tacheService.mapToResponse(any(Tache.class))).thenReturn(tacheResponse);

        // When
        ResponseEntity<TacheResponse> response = controller.updateDetails(1L, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tacheRepository).save(tache);
        // La date limite ne doit pas être modifiée
        assertNotNull(tache.getDateLimite());
    }

    @Test
    void updateDetails_dateLimiteBlank() {
        // Given
        TacheUpdateRequest request = new TacheUpdateRequest();
        request.setDateAffectation("2024-01-01");
        request.setDateLimite("   ");

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse tacheResponse = new TacheResponse();
        when(tacheService.mapToResponse(any(Tache.class))).thenReturn(tacheResponse);

        // When
        ResponseEntity<TacheResponse> response = controller.updateDetails(1L, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tacheRepository).save(tache);
    }

    @Test
    void updateDetails_consultantNotFound() {
        // Given
        TacheUpdateRequest request = new TacheUpdateRequest();
        request.setConsultantId(999L);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(consultantRepository.findByUserId(999L)).thenReturn(Collections.emptyList());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> controller.updateDetails(1L, request));
        assertEquals("Consultant non trouvé", exception.getMessage());

        verify(tacheRepository).findById(1L);
        verify(consultantRepository).findByUserId(999L);
        verifyNoInteractions(projetRepository, tacheService);
    }

    @Test
    void updateDetails_projetNotFound() {
        // Given
        TacheUpdateRequest request = new TacheUpdateRequest();
        request.setProjetId(999L);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(projetRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> controller.updateDetails(1L, request));
        assertEquals("Projet non trouvé", exception.getMessage());

        verify(tacheRepository).findById(1L);
        verify(projetRepository).findById(999L);
        verifyNoInteractions(tacheService);
    }

    @Test
    void updateDetails_prioriteNull() {
        // Given
        TacheUpdateRequest request = new TacheUpdateRequest();
        request.setPriorite(null);
        request.setDescription("Nouvelle description");
        request.setCommentaire("Nouveau commentaire");

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse tacheResponse = new TacheResponse();
        when(tacheService.mapToResponse(any(Tache.class))).thenReturn(tacheResponse);

        // When
        ResponseEntity<TacheResponse> response = controller.updateDetails(1L, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tacheRepository).save(tache);
        // La priorité ne doit pas être modifiée
        assertEquals(Priority.MOYENNE, tache.getPriorite());
    }

    @Test
    void updateDetails_saveThrowsException() {
        // Given
        TacheUpdateRequest request = new TacheUpdateRequest();
        request.setDescription("Test");

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(tacheRepository.save(any(Tache.class))).thenThrow(new RuntimeException("Database error"));

        // When & Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> controller.updateDetails(1L, request));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("Erreur lors de la mise à jour", exception.getReason());
        assertNotNull(exception.getCause());
        assertEquals("Database error", exception.getCause().getMessage());

        verify(tacheRepository).findById(1L);
        verify(tacheRepository).save(any(Tache.class));
        verifyNoInteractions(tacheService);
    }

    @Test
    void updateDetails_partialUpdate() {
        // Given
        TacheUpdateRequest request = new TacheUpdateRequest();
        // Seulement la description est mise à jour
        request.setDescription("Description modifiée");
        request.setCommentaire("Commentaire modifié");
        // Les autres champs sont null

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse tacheResponse = new TacheResponse();
        when(tacheService.mapToResponse(any(Tache.class))).thenReturn(tacheResponse);

        // When
        ResponseEntity<TacheResponse> response = controller.updateDetails(1L, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tacheRepository).save(tache);
        assertEquals("Description modifiée", tache.getDescription());
        assertEquals("Commentaire modifié", tache.getCommentaire());
    }

    // =====================================================
    // TESTS pour updateStatut - Tous les rôles
    // =====================================================

    @Test
    void updateStatut_chefDeProjet() {
        // Given
        user.setRole(Role.CHEF_DE_PROJET);
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("chef@mail.com", null)
        );

        when(userRepository.findByEmail("chef@mail.com")).thenReturn(Optional.of(user));

        TacheResponse tacheResponse = new TacheResponse();
        tacheResponse.setId(1L);
        tacheResponse.setStatut(StatutTache.EN_COURS);

        when(tacheService.updateStatut(1L, StatutTache.EN_COURS, "ok", user))
                .thenReturn(tacheResponse);

        // When
        ResponseEntity<TacheResponse> response = controller.updateStatut(1L, StatutTache.EN_COURS, "ok");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals(StatutTache.EN_COURS, response.getBody().getStatut());

        verify(userRepository).findByEmail("chef@mail.com");
        verify(tacheService).updateStatut(1L, StatutTache.EN_COURS, "ok", user);
    }

    @Test
    void updateStatut_consultant() {
        // Given
        user.setRole(Role.CONSULTANT);
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("consultant@mail.com", null)
        );

        when(userRepository.findByEmail("consultant@mail.com")).thenReturn(Optional.of(user));

        TacheResponse tacheResponse = new TacheResponse();
        tacheResponse.setId(1L);
        tacheResponse.setStatut(StatutTache.TERMINE);

        when(tacheService.updateStatut(1L, StatutTache.TERMINE, "terminé", user))
                .thenReturn(tacheResponse);

        // When
        ResponseEntity<TacheResponse> response = controller.updateStatut(1L, StatutTache.TERMINE, "terminé");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(StatutTache.TERMINE, response.getBody().getStatut());

        verify(userRepository).findByEmail("consultant@mail.com");
        verify(tacheService).updateStatut(1L, StatutTache.TERMINE, "terminé", user);
    }

    @Test
    void updateStatut_userNotFound() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("unknown@mail.com", null)
        );

        when(userRepository.findByEmail("unknown@mail.com")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> controller.updateStatut(1L, StatutTache.EN_COURS, null));

        assertEquals("Utilisateur non trouvé", exception.getMessage());
        verify(userRepository).findByEmail("unknown@mail.com");
        verifyNoInteractions(tacheService);
    }

    @Test
    void updateStatut_commentaireNull() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("test@mail.com", null)
        );

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));

        TacheResponse tacheResponse = new TacheResponse();
        tacheResponse.setId(1L);

        when(tacheService.updateStatut(1L, StatutTache.A_FAIRE, null, user))
                .thenReturn(tacheResponse);

        // When
        ResponseEntity<TacheResponse> response = controller.updateStatut(1L, StatutTache.A_FAIRE, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tacheService).updateStatut(1L, StatutTache.A_FAIRE, null, user);
    }

    @Test
    void updateStatut_commentaireEmpty() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("test@mail.com", null)
        );

        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));

        TacheResponse tacheResponse = new TacheResponse();

        when(tacheService.updateStatut(1L, StatutTache.EN_COURS, "", user))
                .thenReturn(tacheResponse);

        // When
        ResponseEntity<TacheResponse> response = controller.updateStatut(1L, StatutTache.EN_COURS, "");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(tacheService).updateStatut(1L, StatutTache.EN_COURS, "", user);
    }

    // =====================================================
    // TESTS pour getConsultants - Lambda coverage
    // =====================================================

    @Test
    void getConsultants_withDuplicates() {
        // Given
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(2L);

        Consultant consultant1 = new Consultant();
        consultant1.setUser(user1);
        consultant1.setNomComplet("Ali Baba"); // AJOUTÉ

        Consultant consultant2 = new Consultant();
        consultant2.setUser(user2);
        consultant2.setNomComplet("John Doe"); // AJOUTÉ

        Consultant consultant3 = new Consultant(); // Duplicate of user1
        consultant3.setUser(user1);
        consultant3.setNomComplet("Ali Baba"); // AJOUTÉ (même nom)

        when(consultantRepository.findAll())
                .thenReturn(List.of(consultant1, consultant2, consultant3));

        // When
        ResponseEntity<List<Map<String, Object>>> response = controller.getConsultants();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size()); // Distinct should give 2

        List<Map<String, Object>> consultants = response.getBody();

        // Vérifiez les valeurs sans dépendre de l'ordre
        Set<Long> ids = new HashSet<>();
        Set<String> noms = new HashSet<>();

        for (Map<String, Object> consultantMap : consultants) {
            ids.add((Long) consultantMap.get("id"));
            noms.add((String) consultantMap.get("nomComplet"));
        }

        assertTrue(ids.contains(1L));
        assertTrue(ids.contains(2L));
        assertTrue(noms.contains("Ali Baba"));
        assertTrue(noms.contains("John Doe"));

        verify(consultantRepository).findAll();
    }

    @Test
    void getConsultants_emptyList() {
        // Given
        when(consultantRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<Map<String, Object>>> response = controller.getConsultants();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(consultantRepository).findAll();
    }

    @Test
    void getConsultants_singleConsultant() {
        // Given
        User testUser = new User();  // Renommé de "user" à "testUser"
        testUser.setId(1L);

        Consultant testConsultant = new Consultant();  // Renommé de "consultant" à "testConsultant"
        testConsultant.setUser(testUser);
        testConsultant.setNomComplet("Single User");

        when(consultantRepository.findAll()).thenReturn(List.of(testConsultant));

        // When
        ResponseEntity<List<Map<String, Object>>> response = controller.getConsultants();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());

        Map<String, Object> consultantMap = response.getBody().get(0);
        assertEquals(1L, consultantMap.get("id"));
        assertEquals("Single User", consultantMap.get("nomComplet"));
    }

    // =====================================================
    // TESTS pour getProjetsByConsultant - Lambda coverage
    // =====================================================

    @Test
    void getProjetsByConsultant_withDuplicates() {
        // Given
        Projet projet1 = new Projet();
        projet1.setId(1L);
        projet1.setNomProjet("Projet A");

        Projet projet2 = new Projet();
        projet2.setId(2L);
        projet2.setNomProjet("Projet B");

        Consultant consultant1 = new Consultant();
        consultant1.setProjet(projet1);

        Consultant consultant2 = new Consultant();
        consultant2.setProjet(projet2);

        Consultant consultant3 = new Consultant(); // Duplicate of projet1
        consultant3.setProjet(projet1);

        when(consultantRepository.findByUserId(1L))
                .thenReturn(List.of(consultant1, consultant2, consultant3));

        // When
        ResponseEntity<List<Map<String, Object>>> response =
                controller.getProjetsByConsultant(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size()); // Distinct should give 2

        List<Map<String, Object>> projets = response.getBody();

        // Verify both projects are present
        boolean hasProjetA = projets.stream()
                .anyMatch(p -> p.get("id").equals(1L) && p.get("nomProjet").equals("Projet A"));
        boolean hasProjetB = projets.stream()
                .anyMatch(p -> p.get("id").equals(2L) && p.get("nomProjet").equals("Projet B"));

        assertTrue(hasProjetA);
        assertTrue(hasProjetB);

        verify(consultantRepository).findByUserId(1L);
    }

    @Test
    void getProjetsByConsultant_emptyList() {
        // Given
        when(consultantRepository.findByUserId(1L)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<Map<String, Object>>> response =
                controller.getProjetsByConsultant(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(consultantRepository).findByUserId(1L);
    }

    @Test
    void getProjetsByConsultant_singleProjet() {
        // Given
        Projet testProjet = new Projet();  // CORRECTION: Renommé
        testProjet.setId(1L);
        testProjet.setNomProjet("Single Projet");

        Consultant testConsultant = new Consultant();  // CORRECTION: Renommé
        testConsultant.setProjet(testProjet);

        when(consultantRepository.findByUserId(1L)).thenReturn(List.of(testConsultant));

        // When
        ResponseEntity<List<Map<String, Object>>> response =
                controller.getProjetsByConsultant(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());

        Map<String, Object> projetMap = response.getBody().get(0);
        assertEquals(1L, projetMap.get("id"));
        assertEquals("Single Projet", projetMap.get("nomProjet"));
    }

    // =====================================================
    // TESTS pour getMesTaches - Toutes les branches
    // =====================================================

    @Test
    void getMesTaches_emailFromSecurityContext() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("consultant@mail.com", null)
        );

        TacheResponse tacheResponse = new TacheResponse();
        tacheResponse.setId(1L);
        tacheResponse.setNomTache("Tâche Test");
        tacheResponse.setNomConsultant("John Doe");

        when(tacheService.getTachesParEmailConsultant("consultant@mail.com"))
                .thenReturn(List.of(tacheResponse));

        // When
        ResponseEntity<List<TacheResponse>> response = controller.getMesTaches(null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Tâche Test", response.getBody().get(0).getNomTache());
        assertEquals("John Doe", response.getBody().get(0).getNomConsultant());

        verify(tacheService).getTachesParEmailConsultant("consultant@mail.com");
    }

    @Test
    void getMesTaches_emailFromParameter() {
        // Given
        TacheResponse tacheResponse = new TacheResponse();
        tacheResponse.setId(1L);
        tacheResponse.setNomProjet("Projet Test");

        when(tacheService.getTachesParEmailConsultant("test@mail.com"))
                .thenReturn(List.of(tacheResponse));

        // When
        ResponseEntity<List<TacheResponse>> response =
                controller.getMesTaches("test@mail.com");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("Projet Test", response.getBody().get(0).getNomProjet());

        verify(tacheService).getTachesParEmailConsultant("test@mail.com");
        // SecurityContext should not be used
        verifyNoInteractions(userRepository);
    }

    @Test
    void getMesTaches_emptyResult() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("empty@mail.com", null)
        );

        when(tacheService.getTachesParEmailConsultant("empty@mail.com"))
                .thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<TacheResponse>> response = controller.getMesTaches(null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(tacheService).getTachesParEmailConsultant("empty@mail.com");
    }

    @Test
    void getMesTaches_withMultipleTaches() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("multi@mail.com", null)
        );

        TacheResponse tache1 = new TacheResponse();
        tache1.setId(1L);
        tache1.setStatut(StatutTache.EN_COURS);

        TacheResponse tache2 = new TacheResponse();
        tache2.setId(2L);
        tache2.setStatut(StatutTache.TERMINE);

        when(tacheService.getTachesParEmailConsultant("multi@mail.com"))
                .thenReturn(List.of(tache1, tache2));

        // When
        ResponseEntity<List<TacheResponse>> response = controller.getMesTaches(null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals(StatutTache.EN_COURS, response.getBody().get(0).getStatut());
        assertEquals(StatutTache.TERMINE, response.getBody().get(1).getStatut());
    }

    @Test
    void getMesTaches_emailParameterOverridesSecurityContext() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("context@mail.com", null)
        );

        TacheResponse tacheResponse = new TacheResponse();
        tacheResponse.setId(1L);

        when(tacheService.getTachesParEmailConsultant("parameter@mail.com"))
                .thenReturn(List.of(tacheResponse));

        // When
        ResponseEntity<List<TacheResponse>> response =
                controller.getMesTaches("parameter@mail.com");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());

        // Should use parameter, not context
        verify(tacheService).getTachesParEmailConsultant("parameter@mail.com");
        verifyNoInteractions(userRepository);
    }

    // =====================================================
    // TESTS pour les autres méthodes
    // =====================================================

    @Test
    void createTache_success() {
        // Given
        TacheRequest request = new TacheRequest();
        TacheResponse responseMock = new TacheResponse();
        responseMock.setId(1L);

        when(tacheService.createTache(request)).thenReturn(responseMock);

        // When
        ResponseEntity<TacheResponse> response = controller.createTache(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());

        verify(tacheService).createTache(request);
    }

    @Test
    void getAllTaches_success() {
        // Given
        TacheResponse tache1 = new TacheResponse();
        tache1.setId(1L);

        TacheResponse tache2 = new TacheResponse();
        tache2.setId(2L);

        when(tacheService.getAllTaches()).thenReturn(List.of(tache1, tache2));

        // When
        ResponseEntity<List<TacheResponse>> response = controller.getAllTaches();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());

        verify(tacheService).getAllTaches();
    }

    @Test
    void deleteTache_success() {
        // When
        ResponseEntity<Void> response = controller.deleteTache(1L);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(tacheService).deleteTache(1L);
    }

    @Test
    void getStatistiques_success() {
        // Given
        StatistiquesTacheResponse stats = new StatistiquesTacheResponse(
                3L,  // nbEnCours
                2L,  // nbAFaire
                5L,  // nbTermine
                1L,  // nbBloque
                4L   // nbRetarde
        );

        when(tacheService.getStatistiquesParProjet("projet-test")).thenReturn(stats);

        // When
        ResponseEntity<StatistiquesTacheResponse> response =
                controller.getStatistiques("projet-test");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Vérification des valeurs individuelles
        assertEquals(3L, response.getBody().getNbEnCours());
        assertEquals(2L, response.getBody().getNbAFaire());
        assertEquals(5L, response.getBody().getNbTermine());
        assertEquals(1L, response.getBody().getNbBloque());
        assertEquals(4L, response.getBody().getNbRetarde());

        // Vérification que le service a été appelé
        verify(tacheService).getStatistiquesParProjet("projet-test");
    }

    // Vous pouvez aussi ajouter un test pour une réponse vide/nulle
    @Test
    void getStatistiques_withDefaultValues() {
        // Given - Utilisation du constructeur par défaut (si Lombok le génère)
        // Ou création avec toutes les valeurs à 0
        StatistiquesTacheResponse stats = new StatistiquesTacheResponse(0L, 0L, 0L, 0L, 0L);

        when(tacheService.getStatistiquesParProjet("projet-vide")).thenReturn(stats);

        // When
        ResponseEntity<StatistiquesTacheResponse> response =
                controller.getStatistiques("projet-vide");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Toutes les valeurs doivent être à 0
        assertEquals(0L, response.getBody().getNbEnCours());
        assertEquals(0L, response.getBody().getNbAFaire());
        assertEquals(0L, response.getBody().getNbTermine());
        assertEquals(0L, response.getBody().getNbBloque());
        assertEquals(0L, response.getBody().getNbRetarde());
    }

    @Test
    void getAffectations_success() {
        // Given
        User testUser = new User();  // CORRECTION: Renommé
        testUser.setId(1L);
        testUser.setNomComplet("Test User");

        Projet testProjet = new Projet();  // CORRECTION: Renommé
        testProjet.setId(1L);
        testProjet.setNomProjet("Test Projet");

        Consultant testConsultant = new Consultant();  // CORRECTION: Renommé
        testConsultant.setUser(testUser);
        testConsultant.setProjet(testProjet);

        when(consultantRepository.findAll()).thenReturn(List.of(testConsultant));

        // When
        ResponseEntity<List<AffectationDto>> response = controller.getAffectations();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());

        AffectationDto dto = response.getBody().get(0);
        assertEquals(1L, dto.getUserId());
        assertEquals("Test User", dto.getNomConsultant());
        assertEquals(1L, dto.getProjetId());
        assertEquals("Test Projet", dto.getNomProjet());

        verify(consultantRepository).findAll();
    }

    @Test
    void getAffectations_emptyList() {
        // Given
        when(consultantRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<AffectationDto>> response = controller.getAffectations();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());

        verify(consultantRepository).findAll();
    }
}