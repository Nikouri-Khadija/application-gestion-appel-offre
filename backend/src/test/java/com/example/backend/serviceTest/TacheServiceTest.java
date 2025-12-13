package com.example.backend.serviceTest;

import com.example.backend.dto.StatistiquesTacheResponse;
import com.example.backend.dto.TacheRequest;
import com.example.backend.dto.TacheResponse;
import com.example.backend.entity.*;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.ConsultantRepository;
import com.example.backend.repository.ProjetRepository;
import com.example.backend.repository.TacheRepository;
import com.example.backend.service.TacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TacheServiceTest {

    @InjectMocks
    private TacheService tacheService;

    @Mock private TacheRepository tacheRepository;
    @Mock private ConsultantRepository consultantRepository;
    @Mock private ProjetRepository projetRepository;

    private Tache tache;
    private Consultant consultant;
    private Projet projet;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@mail.com");
        user.setNomComplet("John Doe");
        user.setRole(Role.CONSULTANT);

        projet = new Projet();
        projet.setId(1L);
        projet.setNomProjet("Projet Test");

        consultant = new Consultant();
        consultant.setId(1L);
        consultant.setUser(user);
        consultant.setProjet(projet);
        consultant.setNomComplet("John Doe");

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
        tache.setDateCreation(LocalDateTime.now());
    }

    @Test
    void createTache_success() {
        TacheRequest request = new TacheRequest();
        request.setNomTache("Nouvelle Tâche");
        request.setConsultantId(1L);
        request.setProjetId(1L);
        request.setPriorite(Priority.MOYENNE);
        request.setDescription("Description");
        request.setDateAffectation("2024-01-01");
        request.setDateLimite("2024-12-31");

        when(consultantRepository.findByUserId(1L)).thenReturn(List.of(consultant));
        when(projetRepository.findById(1L)).thenReturn(Optional.of(projet));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse response = tacheService.createTache(request);

        assertNotNull(response);
        assertEquals("Tâche Test", response.getNomTache());
        verify(consultantRepository).findByUserId(1L);
        verify(projetRepository).findById(1L);
        verify(tacheRepository).save(any(Tache.class));
    }

    @Test
    void createTache_consultantNotFound() {
        TacheRequest request = new TacheRequest();
        request.setConsultantId(999L);
        request.setProjetId(1L);
        request.setNomTache("Test");

        when(consultantRepository.findByUserId(999L)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> tacheService.createTache(request));
        verify(consultantRepository).findByUserId(999L);
    }

    @Test
    void createTache_projetNotFound() {
        TacheRequest request = new TacheRequest();
        request.setConsultantId(1L);
        request.setProjetId(999L);
        request.setNomTache("Test");

        when(consultantRepository.findByUserId(1L)).thenReturn(List.of(consultant));
        when(projetRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> tacheService.createTache(request));
        verify(consultantRepository).findByUserId(1L);
        verify(projetRepository).findById(999L);
    }

    @Test
    void createTache_withNullDates() {
        TacheRequest request = new TacheRequest();
        request.setNomTache("Test");
        request.setConsultantId(1L);
        request.setProjetId(1L);
        request.setPriorite(Priority.MOYENNE);
        request.setDescription("Description");
        request.setDateAffectation(null);
        request.setDateLimite(null);

        when(consultantRepository.findByUserId(1L)).thenReturn(List.of(consultant));
        when(projetRepository.findById(1L)).thenReturn(Optional.of(projet));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse response = tacheService.createTache(request);
        assertNotNull(response);
        verify(tacheRepository).save(any(Tache.class));
    }

    @Test
    void getAllTaches_success() {
        when(tacheRepository.findAll()).thenReturn(List.of(tache));

        List<TacheResponse> responses = tacheService.getAllTaches();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(tacheRepository).findAll();
    }

    @Test
    void getAllTaches_emptyList() {
        when(tacheRepository.findAll()).thenReturn(List.of());

        List<TacheResponse> responses = tacheService.getAllTaches();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(tacheRepository).findAll();
    }

    @Test
    void updateStatut_consultantToEnCours_shouldSuccess() {
        user.setRole(Role.CONSULTANT);
        tache.setStatut(StatutTache.A_FAIRE);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse response = tacheService.updateStatut(1L, StatutTache.EN_COURS, "Avancement", user);

        assertNotNull(response);
        assertEquals(StatutTache.EN_COURS, tache.getStatut());
        verify(tacheRepository).save(tache);
    }

    @Test
    void updateStatut_consultantToBloqueWithComment_shouldSuccess() {
        user.setRole(Role.CONSULTANT);
        tache.setStatut(StatutTache.A_FAIRE);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse response = tacheService.updateStatut(
                1L,
                StatutTache.BLOQUE,
                "Problème technique rencontré",
                user
        );

        assertNotNull(response);
        assertEquals(StatutTache.BLOQUE, tache.getStatut());
        assertEquals("Problème technique rencontré", tache.getCommentaire());
    }

    @Test
    void updateStatut_consultantToBloqueNoComment_shouldThrow() {
        user.setRole(Role.CONSULTANT);
        tache.setStatut(StatutTache.A_FAIRE);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));

        assertThrows(IllegalArgumentException.class, () ->
                tacheService.updateStatut(1L, StatutTache.BLOQUE, null, user)
        );
        verify(tacheRepository, never()).save(any());
    }

    @Test
    void updateStatut_consultantToBloqueEmptyComment_shouldThrow() {
        user.setRole(Role.CONSULTANT);
        tache.setStatut(StatutTache.A_FAIRE);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));

        assertThrows(IllegalArgumentException.class, () ->
                tacheService.updateStatut(1L, StatutTache.BLOQUE, "", user)
        );
        verify(tacheRepository, never()).save(any());
    }

    @Test
    void updateStatut_consultantToBloqueWhitespaceComment_shouldThrow() {
        user.setRole(Role.CONSULTANT);
        tache.setStatut(StatutTache.A_FAIRE);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));

        assertThrows(IllegalArgumentException.class, () ->
                tacheService.updateStatut(1L, StatutTache.BLOQUE, "   ", user)
        );
        verify(tacheRepository, never()).save(any());
    }

    @Test
    void updateStatut_consultantToEnAttenteValidation_shouldSuccess() {
        user.setRole(Role.CONSULTANT);
        tache.setStatut(StatutTache.EN_COURS);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse response = tacheService.updateStatut(
                1L,
                StatutTache.EN_ATTENTE_VALIDATION,
                "Prêt pour validation",
                user
        );

        assertNotNull(response);
        assertEquals(StatutTache.EN_ATTENTE_VALIDATION, tache.getStatut());
    }

    @Test
    void updateStatut_consultantToTermine_shouldThrowAccessDenied() {
        user.setRole(Role.CONSULTANT);
        tache.setStatut(StatutTache.EN_COURS);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));

        assertThrows(AccessDeniedException.class, () ->
                tacheService.updateStatut(1L, StatutTache.TERMINE, "Terminé", user)
        );
        verify(tacheRepository, never()).save(any());
    }

    @Test
    void updateStatut_consultantToAFaire_shouldThrowAccessDenied() {
        user.setRole(Role.CONSULTANT);
        tache.setStatut(StatutTache.EN_COURS);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));

        assertThrows(AccessDeniedException.class, () ->
                tacheService.updateStatut(1L, StatutTache.A_FAIRE, "Retour à faire", user)
        );
        verify(tacheRepository, never()).save(any());
    }

    @Test
    void updateStatut_chefDeProjetToTermineFromValidation_shouldSuccess() {
        user.setRole(Role.CHEF_DE_PROJET);
        tache.setStatut(StatutTache.EN_ATTENTE_VALIDATION);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse response = tacheService.updateStatut(
                1L,
                StatutTache.TERMINE,
                "Validé et terminé",
                user
        );

        assertNotNull(response);
        assertEquals(StatutTache.TERMINE, tache.getStatut());
    }

    @Test
    void updateStatut_chefDeProjetToTermineFromEnCours_shouldThrow() {
        user.setRole(Role.CHEF_DE_PROJET);
        tache.setStatut(StatutTache.EN_COURS);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));

        assertThrows(IllegalArgumentException.class, () ->
                tacheService.updateStatut(1L, StatutTache.TERMINE, "Terminé", user)
        );
        verify(tacheRepository, never()).save(any());
    }

    @Test
    void updateStatut_chefDeProjetToTermineFromAFaire_shouldThrow() {
        user.setRole(Role.CHEF_DE_PROJET);
        tache.setStatut(StatutTache.A_FAIRE);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));

        assertThrows(IllegalArgumentException.class, () ->
                tacheService.updateStatut(1L, StatutTache.TERMINE, "Terminé", user)
        );
        verify(tacheRepository, never()).save(any());
    }

    @Test
    void updateStatut_chefDeProjetToTermineFromBloque_shouldThrow() {
        user.setRole(Role.CHEF_DE_PROJET);
        tache.setStatut(StatutTache.BLOQUE);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));

        assertThrows(IllegalArgumentException.class, () ->
                tacheService.updateStatut(1L, StatutTache.TERMINE, "Terminé", user)
        );
        verify(tacheRepository, never()).save(any());
    }

    @Test
    void updateStatut_chefDeProjetToBloqueWithoutComment_shouldSuccess() {
        user.setRole(Role.CHEF_DE_PROJET);
        tache.setStatut(StatutTache.EN_COURS);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse response = tacheService.updateStatut(
                1L,
                StatutTache.BLOQUE,
                null,
                user
        );

        assertNotNull(response);
        assertEquals(StatutTache.BLOQUE, tache.getStatut());
        assertNull(tache.getCommentaire());
    }

    @Test
    void updateStatut_chefDeProjetToBloqueWithComment_shouldSuccess() {
        user.setRole(Role.CHEF_DE_PROJET);
        tache.setStatut(StatutTache.EN_COURS);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse response = tacheService.updateStatut(
                1L,
                StatutTache.BLOQUE,
                "Problème budget",
                user
        );

        assertNotNull(response);
        assertEquals(StatutTache.BLOQUE, tache.getStatut());
        assertEquals("Problème budget", tache.getCommentaire());
    }

    @Test
    void updateStatut_chefDeProjetToEnCours_shouldSuccess() {
        user.setRole(Role.CHEF_DE_PROJET);
        tache.setStatut(StatutTache.A_FAIRE);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse response = tacheService.updateStatut(
                1L,
                StatutTache.EN_COURS,
                "Démarrage",
                user
        );

        assertNotNull(response);
        assertEquals(StatutTache.EN_COURS, tache.getStatut());
    }

    @Test
    void updateStatut_chefDeProjetToEnAttenteValidation_shouldSuccess() {
        user.setRole(Role.CHEF_DE_PROJET);
        tache.setStatut(StatutTache.EN_COURS);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse response = tacheService.updateStatut(
                1L,
                StatutTache.EN_ATTENTE_VALIDATION,
                "Prêt à valider",
                user
        );

        assertNotNull(response);
        assertEquals(StatutTache.EN_ATTENTE_VALIDATION, tache.getStatut());
    }

    @Test
    void updateStatut_chefDeProjetToAFaire_shouldSuccess() {
        user.setRole(Role.CHEF_DE_PROJET);
        tache.setStatut(StatutTache.EN_COURS);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse response = tacheService.updateStatut(
                1L,
                StatutTache.A_FAIRE,
                "Replanification",
                user
        );

        assertNotNull(response);
        assertEquals(StatutTache.A_FAIRE, tache.getStatut());
    }

    @Test
    void updateStatut_roleAdmin_shouldThrowAccessDenied() {
        user.setRole(Role.ADMIN);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));

        assertThrows(AccessDeniedException.class, () ->
                tacheService.updateStatut(1L, StatutTache.EN_COURS, "test", user)
        );
        verify(tacheRepository, never()).save(any());
    }

    @Test
    void updateStatut_tacheNotFound() {
        user.setRole(Role.CONSULTANT);

        when(tacheRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> tacheService.updateStatut(999L, StatutTache.EN_COURS, "commentaire", user));
        verify(tacheRepository).findById(999L);
        verify(tacheRepository, never()).save(any());
    }

    @Test
    void updateStatut_nullCommentaireForAllowedStatus_shouldSuccess() {
        user.setRole(Role.CONSULTANT);
        tache.setStatut(StatutTache.A_FAIRE);

        when(tacheRepository.findById(1L)).thenReturn(Optional.of(tache));
        when(tacheRepository.save(any(Tache.class))).thenReturn(tache);

        TacheResponse response = tacheService.updateStatut(
                1L,
                StatutTache.EN_COURS,
                null,
                user
        );

        assertNotNull(response);
        assertEquals(StatutTache.EN_COURS, tache.getStatut());
    }

    @Test
    void deleteTache_success() {
        tacheService.deleteTache(1L);
        verify(tacheRepository).deleteById(1L);
    }

    @Test
    void deleteTache_withNullId_shouldCallRepository() {
        tacheService.deleteTache(null);
        verify(tacheRepository).deleteById(null);
    }

    @Test
    void mapToResponse_success() {
        TacheResponse response = tacheService.mapToResponse(tache);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Tâche Test", response.getNomTache());
        assertEquals("John Doe", response.getNomConsultant());
        assertEquals("Projet Test", response.getNomProjet());
        assertEquals(Priority.MOYENNE, response.getPriorite());
        assertEquals(StatutTache.A_FAIRE, response.getStatut());
        assertNotNull(response.getDateCreation());
        assertNotNull(response.getDateAffectation());
        assertNotNull(response.getDateLimite());
        assertEquals("Description test", response.getDescription());
        assertEquals("Commentaire test", response.getCommentaire());
    }

    @Test
    void mapToResponse_withNullDates() {
        tache.setDateCreation(null);
        tache.setDateAffectation(null);
        tache.setDateLimite(null);

        TacheResponse response = tacheService.mapToResponse(tache);

        assertNotNull(response);
        assertNull(response.getDateCreation());
        assertNull(response.getDateAffectation());
        assertNull(response.getDateLimite());
    }

    @Test
    void getStatistiquesParProjet_success() {
        when(tacheRepository.findByProjet_NomProjet("Projet Test")).thenReturn(List.of(tache));

        StatistiquesTacheResponse response = tacheService.getStatistiquesParProjet("Projet Test");

        assertNotNull(response);
        assertEquals(1, response.getNbAFaire());
        assertEquals(0, response.getNbEnCours());
        assertEquals(0, response.getNbTermine());
        assertEquals(0, response.getNbBloque());
        assertEquals(0, response.getNbRetarde());
        verify(tacheRepository).findByProjet_NomProjet("Projet Test");
    }

    @Test
    void getStatistiquesParProjet_multipleStatus() {
        Tache tacheEnCours = new Tache();
        tacheEnCours.setStatut(StatutTache.EN_COURS);
        tacheEnCours.setDateLimite(LocalDate.now().plusDays(5));

        Tache tacheTermine = new Tache();
        tacheTermine.setStatut(StatutTache.TERMINE);
        tacheTermine.setDateLimite(LocalDate.now().minusDays(1));

        Tache tacheBloque = new Tache();
        tacheBloque.setStatut(StatutTache.BLOQUE);
        tacheBloque.setDateLimite(LocalDate.now().minusDays(2));

        Tache tacheRetardee = new Tache();
        tacheRetardee.setStatut(StatutTache.EN_COURS);
        tacheRetardee.setDateLimite(LocalDate.now().minusDays(3));

        when(tacheRepository.findByProjet_NomProjet("Projet Complexe"))
                .thenReturn(List.of(tache, tacheEnCours, tacheTermine, tacheBloque, tacheRetardee));

        StatistiquesTacheResponse response = tacheService.getStatistiquesParProjet("Projet Complexe");

        assertEquals(1, response.getNbAFaire());
        assertEquals(2, response.getNbEnCours());
        assertEquals(1, response.getNbTermine());
        assertEquals(1, response.getNbBloque());
        assertEquals(2, response.getNbRetarde());
    }

    @Test
    void getStatistiquesParProjet_withNullDateLimite() {
        tache.setDateLimite(null);

        when(tacheRepository.findByProjet_NomProjet("Projet Test")).thenReturn(List.of(tache));

        StatistiquesTacheResponse response = tacheService.getStatistiquesParProjet("Projet Test");

        assertEquals(0, response.getNbRetarde());
    }

    @Test
    void getStatistiquesParProjet_todayDateLimit() {
        tache.setDateLimite(LocalDate.now());
        tache.setStatut(StatutTache.EN_COURS);

        when(tacheRepository.findByProjet_NomProjet("Projet Test")).thenReturn(List.of(tache));

        StatistiquesTacheResponse response = tacheService.getStatistiquesParProjet("Projet Test");

        assertEquals(0, response.getNbRetarde());
    }

    @Test
    void getStatistiquesParProjet_tomorrowDateLimit() {
        tache.setDateLimite(LocalDate.now().plusDays(1));
        tache.setStatut(StatutTache.EN_COURS);

        when(tacheRepository.findByProjet_NomProjet("Projet Test")).thenReturn(List.of(tache));

        StatistiquesTacheResponse response = tacheService.getStatistiquesParProjet("Projet Test");

        assertEquals(0, response.getNbRetarde());
    }

    @Test
    void getStatistiquesParProjet_terminatedTaskWithPastDate() {
        tache.setDateLimite(LocalDate.now().minusDays(5));
        tache.setStatut(StatutTache.TERMINE);

        when(tacheRepository.findByProjet_NomProjet("Projet Test")).thenReturn(List.of(tache));

        StatistiquesTacheResponse response = tacheService.getStatistiquesParProjet("Projet Test");

        assertEquals(0, response.getNbRetarde());
    }

    @Test
    void getStatistiquesParProjet_noTasks() {
        when(tacheRepository.findByProjet_NomProjet("Projet Vide")).thenReturn(List.of());

        StatistiquesTacheResponse response = tacheService.getStatistiquesParProjet("Projet Vide");

        assertNotNull(response);
        assertEquals(0, response.getNbAFaire());
        assertEquals(0, response.getNbEnCours());
        assertEquals(0, response.getNbTermine());
        assertEquals(0, response.getNbBloque());
        assertEquals(0, response.getNbRetarde());
    }

    @Test
    void getTachesParEmailConsultant_success() {
        when(tacheRepository.findByConsultant_User_Email("test@mail.com")).thenReturn(List.of(tache));

        List<TacheResponse> responses = tacheService.getTachesParEmailConsultant("test@mail.com");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(tacheRepository).findByConsultant_User_Email("test@mail.com");
    }

    @Test
    void getTachesParEmailConsultant_noTasks() {
        when(tacheRepository.findByConsultant_User_Email("empty@mail.com")).thenReturn(List.of());

        List<TacheResponse> responses = tacheService.getTachesParEmailConsultant("empty@mail.com");

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getTachesParEmailConsultant_nullEmail_shouldReturnEmpty() {
        when(tacheRepository.findByConsultant_User_Email(null)).thenReturn(List.of());

        List<TacheResponse> responses = tacheService.getTachesParEmailConsultant(null);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void formatDateTime_valid() {
        tache.setDateCreation(LocalDateTime.of(2024, 1, 15, 14, 30));
        TacheResponse response = tacheService.mapToResponse(tache);

        assertNotNull(response.getDateCreation());
        assertTrue(response.getDateCreation().contains("15/01/2024 14:30"));
    }

    @Test
    void formatDateTime_null() {
        tache.setDateCreation(null);
        TacheResponse response = tacheService.mapToResponse(tache);

        assertNull(response.getDateCreation());
    }

    @Test
    void formatDate_valid() {
        tache.setDateAffectation(LocalDate.of(2024, 1, 15));
        TacheResponse response = tacheService.mapToResponse(tache);

        assertEquals("2024-01-15", response.getDateAffectation());
    }

    @Test
    void formatDate_null() {
        tache.setDateAffectation(null);
        TacheResponse response = tacheService.mapToResponse(tache);

        assertNull(response.getDateAffectation());
    }

    @Test
    void countByStatus_multipleTasks() {
        Tache tache1 = new Tache();
        tache1.setStatut(StatutTache.EN_COURS);

        Tache tache2 = new Tache();
        tache2.setStatut(StatutTache.EN_COURS);

        Tache tache3 = new Tache();
        tache3.setStatut(StatutTache.TERMINE);

        List<Tache> taches = List.of(tache1, tache2, tache3);

        // Tester via getStatistiquesParProjet
        when(tacheRepository.findByProjet_NomProjet("Projet")).thenReturn(taches);

        StatistiquesTacheResponse response = tacheService.getStatistiquesParProjet("Projet");

        assertEquals(2, response.getNbEnCours());
        assertEquals(1, response.getNbTermine());
    }

    @Test
    void countRetardedTasks_emptyList() {
        when(tacheRepository.findByProjet_NomProjet("Projet")).thenReturn(List.of());

        StatistiquesTacheResponse response = tacheService.getStatistiquesParProjet("Projet");

        assertEquals(0, response.getNbRetarde());
    }




    @Test
    void handleChefDeProjetUpdate_bloqueWithComment_shouldUpdate() {
        tache.setStatut(StatutTache.EN_COURS);

        try {
            var method = TacheService.class.getDeclaredMethod("handleChefDeProjetUpdate",
                    Tache.class, StatutTache.class, String.class);
            method.setAccessible(true);

            method.invoke(tacheService, tache, StatutTache.BLOQUE, "Problème");

            assertEquals(StatutTache.BLOQUE, tache.getStatut());
            assertEquals("Problème", tache.getCommentaire());
        } catch (Exception e) {
            fail("Erreur lors de l'appel de la méthode privée: " + e.getMessage());
        }
    }

    @Test
    void handleChefDeProjetUpdate_bloqueWithoutComment_shouldUpdate() {
        tache.setStatut(StatutTache.EN_COURS);

        try {
            var method = TacheService.class.getDeclaredMethod("handleChefDeProjetUpdate",
                    Tache.class, StatutTache.class, String.class);
            method.setAccessible(true);

            method.invoke(tacheService, tache, StatutTache.BLOQUE, null);

            assertEquals(StatutTache.BLOQUE, tache.getStatut());
            assertNull(tache.getCommentaire()); // Chef peut bloquer sans commentaire
        } catch (Exception e) {
            fail("Erreur lors de l'appel de la méthode privée: " + e.getMessage());
        }
    }



    @Test
    void handleConsultantUpdate_bloqueWithComment_shouldUpdate() {
        tache.setStatut(StatutTache.A_FAIRE);

        try {
            var method = TacheService.class.getDeclaredMethod("handleConsultantUpdate",
                    Tache.class, StatutTache.class, String.class);
            method.setAccessible(true);

            method.invoke(tacheService, tache, StatutTache.BLOQUE, "Problème");

            assertEquals(StatutTache.BLOQUE, tache.getStatut());
            assertEquals("Problème", tache.getCommentaire());
        } catch (Exception e) {
            fail("Erreur lors de l'appel de la méthode privée: " + e.getMessage());
        }
    }





    @Test
    void handleConsultantUpdate_enAttenteValidation_shouldUpdate() {
        tache.setStatut(StatutTache.EN_COURS);

        try {
            var method = TacheService.class.getDeclaredMethod("handleConsultantUpdate",
                    Tache.class, StatutTache.class, String.class);
            method.setAccessible(true);

            method.invoke(tacheService, tache, StatutTache.EN_ATTENTE_VALIDATION, "Prêt");

            assertEquals(StatutTache.EN_ATTENTE_VALIDATION, tache.getStatut());
            // Commentaire non sauvegardé pour EN_ATTENTE_VALIDATION
            assertNotEquals("Prêt", tache.getCommentaire());
        } catch (Exception e) {
            fail("Erreur lors de l'appel de la méthode privée: " + e.getMessage());
        }
    }

// Tests pour les lambdas et autres méthodes non couvertes

    @Test
    void getAllTaches_emptyDatabase() {
        when(tacheRepository.findAll()).thenReturn(List.of());

        List<TacheResponse> responses = tacheService.getAllTaches();

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
        verify(tacheRepository).findAll();
    }

    @Test
    void parseDate_validString() {
        TacheService service = new TacheService(tacheRepository, consultantRepository, projetRepository);

        try {
            var method = TacheService.class.getDeclaredMethod("parseDate", String.class);
            method.setAccessible(true);

            LocalDate result = (LocalDate) method.invoke(service, "2024-01-15");

            assertNotNull(result);
            assertEquals(LocalDate.of(2024, 1, 15), result);
        } catch (Exception e) {
            fail("Erreur lors de l'appol de parseDate: " + e.getMessage());
        }
    }

    @Test
    void parseDate_nullString() {
        TacheService service = new TacheService(tacheRepository, consultantRepository, projetRepository);

        try {
            var method = TacheService.class.getDeclaredMethod("parseDate", String.class);
            method.setAccessible(true);

            LocalDate result = (LocalDate) method.invoke(service, (String) null);

            assertNull(result);
        } catch (Exception e) {
            fail("Erreur lors de l'appol de parseDate: " + e.getMessage());
        }
    }

    @Test
    void countByStatus_emptyList() {
        TacheService service = new TacheService(tacheRepository, consultantRepository, projetRepository);

        try {
            var method = TacheService.class.getDeclaredMethod("countByStatus", List.class, StatutTache.class);
            method.setAccessible(true);

            long count = (long) method.invoke(service, List.of(), StatutTache.EN_COURS);

            assertEquals(0, count);
        } catch (Exception e) {
            fail("Erreur lors de l'appol de countByStatus: " + e.getMessage());
        }
    }

    @Test
    void countRetardedTasks_nullDateLimite() {
        Tache tache1 = new Tache();
        tache1.setStatut(StatutTache.EN_COURS);
        tache1.setDateLimite(null);

        TacheService service = new TacheService(tacheRepository, consultantRepository, projetRepository);

        try {
            var method = TacheService.class.getDeclaredMethod("countRetardedTasks", List.class);
            method.setAccessible(true);

            long count = (long) method.invoke(service, List.of(tache1));

            assertEquals(0, count);
        } catch (Exception e) {
            fail("Erreur lors de l'appol de countRetardedTasks: " + e.getMessage());
        }
    }

    @Test
    void countRetardedTasks_futureDate() {
        Tache tache1 = new Tache();
        tache1.setStatut(StatutTache.EN_COURS);
        tache1.setDateLimite(LocalDate.now().plusDays(1));

        TacheService service = new TacheService(tacheRepository, consultantRepository, projetRepository);

        try {
            var method = TacheService.class.getDeclaredMethod("countRetardedTasks", List.class);
            method.setAccessible(true);

            long count = (long) method.invoke(service, List.of(tache1));

            assertEquals(0, count);
        } catch (Exception e) {
            fail("Erreur lors de l'appol de countRetardedTasks: " + e.getMessage());
        }
    }

    @Test
    void countRetardedTasks_terminatedTaskWithPastDate() {
        Tache tache1 = new Tache();
        tache1.setStatut(StatutTache.TERMINE);
        tache1.setDateLimite(LocalDate.now().minusDays(5));

        TacheService service = new TacheService(tacheRepository, consultantRepository, projetRepository);

        try {
            var method = TacheService.class.getDeclaredMethod("countRetardedTasks", List.class);
            method.setAccessible(true);

            long count = (long) method.invoke(service, List.of(tache1));

            assertEquals(0, count);
        } catch (Exception e) {
            fail("Erreur lors de l'appol de countRetardedTasks: " + e.getMessage());
        }
    }

    @Test
    void formatDate_validDate() {
        TacheService service = new TacheService(tacheRepository, consultantRepository, projetRepository);

        try {
            var method = TacheService.class.getDeclaredMethod("formatDate", LocalDate.class);
            method.setAccessible(true);

            String result = (String) method.invoke(service, LocalDate.of(2024, 1, 15));

            assertEquals("2024-01-15", result);
        } catch (Exception e) {
            fail("Erreur lors de l'appol de formatDate: " + e.getMessage());
        }
    }

    @Test
    void formatDate_nullDate() {
        TacheService service = new TacheService(tacheRepository, consultantRepository, projetRepository);

        try {
            var method = TacheService.class.getDeclaredMethod("formatDate", LocalDate.class);
            method.setAccessible(true);

            String result = (String) method.invoke(service, (LocalDate) null);

            assertNull(result);
        } catch (Exception e) {
            fail("Erreur lors de l'appol de formatDate: " + e.getMessage());
        }
    }

    @Test
    void formatDateTime_validDateTime() {
        TacheService service = new TacheService(tacheRepository, consultantRepository, projetRepository);

        try {
            var method = TacheService.class.getDeclaredMethod("formatDateTime", LocalDateTime.class);
            method.setAccessible(true);

            String result = (String) method.invoke(service, LocalDateTime.of(2024, 1, 15, 14, 30));

            assertNotNull(result);
            assertTrue(result.contains("15/01/2024 14:30"));
        } catch (Exception e) {
            fail("Erreur lors de l'appol de formatDateTime: " + e.getMessage());
        }
    }

    @Test
    void formatDateTime_nullDateTime() {
        TacheService service = new TacheService(tacheRepository, consultantRepository, projetRepository);

        try {
            var method = TacheService.class.getDeclaredMethod("formatDateTime", LocalDateTime.class);
            method.setAccessible(true);

            String result = (String) method.invoke(service, (LocalDateTime) null);

            assertNull(result);
        } catch (Exception e) {
            fail("Erreur lors de l'appol de formatDateTime: " + e.getMessage());
        }
    }
}