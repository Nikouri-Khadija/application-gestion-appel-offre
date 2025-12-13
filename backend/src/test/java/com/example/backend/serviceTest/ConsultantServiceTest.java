package com.example.backend.serviceTest;

import com.example.backend.dto.ConsultantRequest;
import com.example.backend.dto.ConsultantResponse;
import com.example.backend.dto.ProjetResponse;
import com.example.backend.entity.Consultant;
import com.example.backend.entity.Projet;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.repository.ConsultantRepository;
import com.example.backend.repository.ProjetRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.ConsultantService;
import com.example.backend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultantServiceTest {

    @Mock
    private ConsultantRepository consultantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjetRepository projetRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ConsultantService consultantService;

    private User user;
    private Projet projet;
    private Consultant consultant;
    private ConsultantRequest request;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setNomComplet("John Doe");
        user.setRole(Role.CONSULTANT);

        projet = new Projet();
        projet.setId(1L);
        projet.setNomProjet("Projet A");
        projet.setCodeProjet("PA001");
        projet.setDescription("Description du projet A");

        consultant = new Consultant();
        consultant.setId(1L);
        consultant.setNomComplet("John Doe");
        consultant.setUser(user);
        consultant.setProjet(projet);
        consultant.setOrganisme("Org1");
        consultant.setDescriptionProjet("Description projet");
        consultant.setDateAffectation(LocalDate.of(2025, 12, 12));

        request = new ConsultantRequest();
        request.setNomComplet("John Doe");
        request.setEmail("test@example.com");
        request.setNomProjet("Projet A");
        request.setOrganisme("Org1");
        request.setDescriptionProjet("Description projet");
        request.setDateAffectation("12/12/2025");
    }

    // Tests existants... (tous les tests précédents restent)

    // TESTS SPÉCIFIQUES POUR LES LAMBDAS ET MÉTHODES PRIVÉES

    @Test
    void mapToConsultantResponse_success() throws Exception {
        // Utiliser la réflexion pour tester la méthode privée
        Method method = ConsultantService.class.getDeclaredMethod("mapToConsultantResponse", Consultant.class);
        method.setAccessible(true);

        ConsultantResponse response = (ConsultantResponse) method.invoke(consultantService, consultant);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getNomComplet());
        assertEquals("Org1", response.getOrganisme());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Projet A", response.getNomProjet());
        assertEquals("12/12/2025", response.getDateAffectation());
        assertEquals("Description projet", response.getDescriptionProjet());
    }

    @Test
    void mapToConsultantResponse_nullValues() throws Exception {
        Consultant consultantWithNulls = new Consultant();
        consultantWithNulls.setId(2L);
        consultantWithNulls.setNomComplet("Jane Doe");
        // User, Projet, DateAffectation, Organisme, DescriptionProjet sont null

        Method method = ConsultantService.class.getDeclaredMethod("mapToConsultantResponse", Consultant.class);
        method.setAccessible(true);

        ConsultantResponse response = (ConsultantResponse) method.invoke(consultantService, consultantWithNulls);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("Jane Doe", response.getNomComplet());
        assertNull(response.getOrganisme());
        assertNull(response.getEmail());
        assertNull(response.getNomProjet());
        assertNull(response.getDateAffectation());
        assertNull(response.getDescriptionProjet());
    }

    @Test
    void mapToConsultantResponse_nullUser() throws Exception {
        consultant.setUser(null);

        Method method = ConsultantService.class.getDeclaredMethod("mapToConsultantResponse", Consultant.class);
        method.setAccessible(true);

        ConsultantResponse response = (ConsultantResponse) method.invoke(consultantService, consultant);

        assertNotNull(response);
        assertNull(response.getEmail());
        assertEquals("Projet A", response.getNomProjet());
    }

    @Test
    void mapToConsultantResponse_nullProjet() throws Exception {
        consultant.setProjet(null);

        Method method = ConsultantService.class.getDeclaredMethod("mapToConsultantResponse", Consultant.class);
        method.setAccessible(true);

        ConsultantResponse response = (ConsultantResponse) method.invoke(consultantService, consultant);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertNull(response.getNomProjet());
    }

    @Test
    void mapToConsultantResponse_nullDateAffectation() throws Exception {
        consultant.setDateAffectation(null);

        Method method = ConsultantService.class.getDeclaredMethod("mapToConsultantResponse", Consultant.class);
        method.setAccessible(true);

        ConsultantResponse response = (ConsultantResponse) method.invoke(consultantService, consultant);

        assertNotNull(response);
        assertNull(response.getDateAffectation());
    }

    @Test
    void mapToProjetResponse_success() throws Exception {
        Method method = ConsultantService.class.getDeclaredMethod("mapToProjetResponse", Consultant.class);
        method.setAccessible(true);

        ProjetResponse response = (ProjetResponse) method.invoke(consultantService, consultant);

        assertNotNull(response);
        assertEquals("Projet A", response.getNomProjet());
        assertEquals("PA001", response.getCodeProjet());
        assertEquals("Description projet", response.getDescriptionProjet());
        assertEquals("12/12/2025", response.getDateAffectation());
        assertEquals("Org1", response.getOrganisme());
    }

    @Test
    void mapToProjetResponse_nullValues() throws Exception {
        Consultant consultantWithNulls = new Consultant();
        // Toutes les valeurs sont null

        Method method = ConsultantService.class.getDeclaredMethod("mapToProjetResponse", Consultant.class);
        method.setAccessible(true);

        ProjetResponse response = (ProjetResponse) method.invoke(consultantService, consultantWithNulls);

        assertNotNull(response);
        assertNull(response.getNomProjet());
        assertNull(response.getCodeProjet());
        assertNull(response.getDescriptionProjet());
        assertNull(response.getDateAffectation());
        assertNull(response.getOrganisme());
    }

    @Test
    void mapToProjetResponse_nullProjet() throws Exception {
        consultant.setProjet(null);

        Method method = ConsultantService.class.getDeclaredMethod("mapToProjetResponse", Consultant.class);
        method.setAccessible(true);

        ProjetResponse response = (ProjetResponse) method.invoke(consultantService, consultant);

        assertNotNull(response);
        assertNull(response.getNomProjet());
        assertNull(response.getCodeProjet());
        assertEquals("Description projet", response.getDescriptionProjet());
    }

    @Test
    void mapToProjetResponse_nullDateAffectation() throws Exception {
        consultant.setDateAffectation(null);

        Method method = ConsultantService.class.getDeclaredMethod("mapToProjetResponse", Consultant.class);
        method.setAccessible(true);

        ProjetResponse response = (ProjetResponse) method.invoke(consultantService, consultant);

        assertNotNull(response);
        assertEquals("Projet A", response.getNomProjet());
        assertNull(response.getDateAffectation());
    }

    @Test
    void mapToProjetResponse_nullOrganisme() throws Exception {
        consultant.setOrganisme(null);

        Method method = ConsultantService.class.getDeclaredMethod("mapToProjetResponse", Consultant.class);
        method.setAccessible(true);

        ProjetResponse response = (ProjetResponse) method.invoke(consultantService, consultant);

        assertNotNull(response);
        assertNull(response.getOrganisme());
    }

    @Test
    void mapToProjetResponse_nullDescriptionProjet() throws Exception {
        consultant.setDescriptionProjet(null);

        Method method = ConsultantService.class.getDeclaredMethod("mapToProjetResponse", Consultant.class);
        method.setAccessible(true);

        ProjetResponse response = (ProjetResponse) method.invoke(consultantService, consultant);

        assertNotNull(response);
        assertNull(response.getDescriptionProjet());
    }

    // Tests pour les streams avec les lambdas externalisées

    @Test
    void getAllConsultantsDetailed_usesLambda() {
        when(consultantRepository.findAll()).thenReturn(List.of(consultant));

        List<ConsultantResponse> responses = consultantService.getAllConsultantsDetailed();

        assertThat(responses).hasSize(1);
        verify(consultantRepository).findAll();
    }

    @Test
    void getProjetsByConsultantEmail_usesLambda() {
        when(consultantRepository.findByUserEmail("test@example.com")).thenReturn(List.of(consultant));

        List<ProjetResponse> responses = consultantService.getProjetsByConsultantEmail("test@example.com");

        assertThat(responses).hasSize(1);
        verify(consultantRepository).findByUserEmail("test@example.com");
    }

    @Test
    void getEmailsConsultants_usesLambda() {
        User user1 = new User();
        user1.setEmail("a@example.com");
        user1.setRole(Role.CONSULTANT);

        User user2 = new User();
        user2.setEmail("b@example.com");
        user2.setRole(Role.CONSULTANT);

        when(userRepository.findByRole(Role.CONSULTANT)).thenReturn(List.of(user1, user2));

        List<String> emails = consultantService.getEmailsConsultants();

        assertThat(emails).containsExactlyInAnyOrder("a@example.com", "b@example.com");
        verify(userRepository).findByRole(Role.CONSULTANT);
    }

    @Test
    void getNomProjets_usesLambda() {
        Projet p1 = new Projet();
        p1.setNomProjet("P1");
        Projet p2 = new Projet();
        p2.setNomProjet("P2");

        when(projetRepository.findAll()).thenReturn(List.of(p1, p2));

        List<String> noms = consultantService.getNomProjets();

        assertThat(noms).containsExactlyInAnyOrder("P1", "P2");
        verify(projetRepository).findAll();
    }

    // Tests pour les cas de comparaison avec valeurs nulles dans updateConsultant

    @Test
    void updateConsultant_organismeNullInConsultant_requestNotNull() {
        consultant.setOrganisme(null);
        request.setOrganisme("New Org");

        when(consultantRepository.findById(1L)).thenReturn(Optional.of(consultant));
        when(consultantRepository.save(any(Consultant.class))).thenReturn(consultant);

        Consultant result = consultantService.updateConsultant(1L, request);

        assertNotNull(result);
        assertEquals("New Org", result.getOrganisme());
        verify(notificationService).envoyer(contains("L'organisme a été mis à jour"), eq("test@example.com"));
    }

    @Test
    void updateConsultant_organismeNotNullInConsultant_requestNull() {
        consultant.setOrganisme("Old Org");
        request.setOrganisme(null);

        when(consultantRepository.findById(1L)).thenReturn(Optional.of(consultant));
        when(consultantRepository.save(any(Consultant.class))).thenReturn(consultant);

        Consultant result = consultantService.updateConsultant(1L, request);

        assertNotNull(result);
        assertEquals("Old Org", result.getOrganisme()); // Ne doit pas changer
        verify(notificationService, never()).envoyer(anyString(), anyString());
    }

    @Test
    void updateConsultant_descriptionNullInConsultant_requestNotNull() {
        consultant.setDescriptionProjet(null);
        request.setDescriptionProjet("New Description");

        when(consultantRepository.findById(1L)).thenReturn(Optional.of(consultant));
        when(consultantRepository.save(any(Consultant.class))).thenReturn(consultant);

        Consultant result = consultantService.updateConsultant(1L, request);

        assertNotNull(result);
        assertEquals("New Description", result.getDescriptionProjet());
        verify(notificationService).envoyer(contains("La description du projet a été mise à jour"), eq("test@example.com"));
    }

    @Test
    void updateConsultant_descriptionNotNullInConsultant_requestNull() {
        consultant.setDescriptionProjet("Old Description");
        request.setDescriptionProjet(null);

        when(consultantRepository.findById(1L)).thenReturn(Optional.of(consultant));
        when(consultantRepository.save(any(Consultant.class))).thenReturn(consultant);

        Consultant result = consultantService.updateConsultant(1L, request);

        assertNotNull(result);
        assertEquals("Old Description", result.getDescriptionProjet()); // Ne doit pas changer
        verify(notificationService, never()).envoyer(anyString(), anyString());
    }

    @Test
    void updateConsultant_bothOrganismeNull() {
        consultant.setOrganisme(null);
        request.setOrganisme(null);

        when(consultantRepository.findById(1L)).thenReturn(Optional.of(consultant));
        when(consultantRepository.save(any(Consultant.class))).thenReturn(consultant);

        Consultant result = consultantService.updateConsultant(1L, request);

        assertNotNull(result);
        assertNull(result.getOrganisme());
        verify(notificationService, never()).envoyer(anyString(), anyString());
    }

    @Test
    void updateConsultant_bothDescriptionNull() {
        consultant.setDescriptionProjet(null);
        request.setDescriptionProjet(null);

        when(consultantRepository.findById(1L)).thenReturn(Optional.of(consultant));
        when(consultantRepository.save(any(Consultant.class))).thenReturn(consultant);

        Consultant result = consultantService.updateConsultant(1L, request);

        assertNotNull(result);
        assertNull(result.getDescriptionProjet());
        verify(notificationService, never()).envoyer(anyString(), anyString());
    }
}