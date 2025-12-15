package com.example.backend.serviceTest;
import com.example.backend.dto.AppelOffreRequest;
import com.example.backend.entity.AppelOffre;
import com.example.backend.entity.Role;
import com.example.backend.entity.Statut;
import com.example.backend.entity.User;
import com.example.backend.exception.InvalidAmountException;
import com.example.backend.exception.InvalidDateException;
import com.example.backend.exception.AppelOffreNotFoundException;
import com.example.backend.repository.AppelOffreRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AppelOffreService;
import com.example.backend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppelOffreServiceTest {

    @Mock
    private AppelOffreRepository appelOffreRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private MultipartFile fichier1;

    @Mock
    private MultipartFile fichier2;

    @Mock
    private MultipartFile fichier3;

    @Mock
    private MultipartFile fichier4;

    @InjectMocks
    private AppelOffreService appelOffreService;

    private AppelOffreRequest appelOffreRequest;
    private User chef;
    private AppelOffre appelOffre;

    @BeforeEach
    void setUp() {
        // Configurer le chemin d'upload pour les tests
        ReflectionTestUtils.setField(appelOffreService, "uploadFolder", "test-uploads");

        // Créer le répertoire de test s'il n'existe pas
        String uploadDir = System.getProperty("user.dir") + File.separator + "test-uploads";
        new File(uploadDir).mkdirs();

        // Préparer les données de test
        chef = new User();
        chef.setId(1L);
        chef.setRole(Role.CHEF_DE_PROJET);
        chef.setNomComplet("Dupont");

        appelOffre = new AppelOffre();
        appelOffre.setId(1L);
        appelOffre.setTitre("Test Appel");
        appelOffre.setStatut(Statut.EN_ATTENTE);
        appelOffre.setEstimation(10000.0);
        appelOffre.setCautionProvisoire(1000.0);
        appelOffre.setDateCreation(LocalDate.now());
        appelOffre.setDateLimite(LocalDate.now().plusDays(30));

        appelOffreRequest = new AppelOffreRequest();
        appelOffreRequest.setTitre("Nouveau Appel");
        appelOffreRequest.setOrganisme("Organisme Test");
        appelOffreRequest.setEstimation("10 000,50");
        appelOffreRequest.setCautionProvisoire("1 000,25");
        appelOffreRequest.setDateCreation(LocalDate.now().toString());
        appelOffreRequest.setDateLimite(LocalDate.now().plusDays(30).toString());
        appelOffreRequest.setFichier1(fichier1);
        appelOffreRequest.setFichier2(fichier2);
        appelOffreRequest.setFichier3(fichier3);
        appelOffreRequest.setFichier4(fichier4);
    }

    @Test
    void create_ShouldCreateAppelOffreSuccessfully() throws IOException {
        // Arrange
        when(userRepository.findByRole(Role.CHEF_DE_PROJET))
                .thenReturn(Collections.singletonList(chef));
        when(fichier1.isEmpty()).thenReturn(false);
        when(fichier1.getOriginalFilename()).thenReturn("fichier1.pdf");
        when(appelOffreRepository.save(any(AppelOffre.class))).thenReturn(appelOffre);

        // Act
        AppelOffre result = appelOffreService.create(appelOffreRequest);

        // Assert
        assertNotNull(result);
        verify(appelOffreRepository).save(any(AppelOffre.class));
        verify(notificationService).envoyer(anyString(), eq("chef"));
    }

    @Test
    void create_ShouldThrowInvalidDateException_WhenInvalidDateFormat() {
        // Arrange
        appelOffreRequest.setDateCreation("invalid-date");
        appelOffreRequest.setDateLimite("invalid-date");

        // Act & Assert
        assertThrows(InvalidDateException.class, () -> appelOffreService.create(appelOffreRequest));
    }

    @Test
    void create_ShouldThrowInvalidAmountException_WhenInvalidAmountFormat() {
        // Arrange
        appelOffreRequest.setEstimation("invalid-amount");
        appelOffreRequest.setCautionProvisoire("invalid-amount");

        // Act & Assert
        assertThrows(InvalidAmountException.class, () -> appelOffreService.create(appelOffreRequest));
    }

    @Test
    void create_ShouldHandleEmptyFiles() throws IOException {
        // Arrange
        when(userRepository.findByRole(Role.CHEF_DE_PROJET))
                .thenReturn(Collections.singletonList(chef));
        when(fichier1.isEmpty()).thenReturn(true);
        when(fichier2.isEmpty()).thenReturn(true);
        when(fichier3.isEmpty()).thenReturn(true);
        when(fichier4.isEmpty()).thenReturn(true);
        when(appelOffreRepository.save(any(AppelOffre.class))).thenReturn(appelOffre);

        // Act
        AppelOffre result = appelOffreService.create(appelOffreRequest);

        // Assert
        assertNotNull(result);
        verify(appelOffreRepository).save(any(AppelOffre.class));
    }

    @Test
    void nettoyerEtConvertirMontant_ShouldHandleVariousFormats() {
        // Méthode privée - testée via réflexion
        double result = (double) ReflectionTestUtils.invokeMethod(
                appelOffreService,
                "nettoyerEtConvertirMontant",
                "10 000,50"
        );

        assertEquals(10000.50, result, 0.01);
    }



    @Test
    void nettoyerEtConvertirMontant_ShouldThrowException_WhenEmpty() {
        assertThrows(NumberFormatException.class, () ->
                ReflectionTestUtils.invokeMethod(
                        appelOffreService,
                        "nettoyerEtConvertirMontant",
                        ""
                )
        );
    }

    @Test
    void nettoyerEtConvertirMontant_ShouldThrowException_WhenNull() {
        assertThrows(NumberFormatException.class, () ->
                ReflectionTestUtils.invokeMethod(
                        appelOffreService,
                        "nettoyerEtConvertirMontant",
                        (String) null
                )
        );
    }

    @Test
    void getAppelsForChef_ShouldReturnAppels() {
        // Arrange
        when(appelOffreRepository.findByDestinatairesContainingAndStatut(chef, Statut.EN_ATTENTE))
                .thenReturn(Collections.singletonList(appelOffre));

        // Act
        List<AppelOffre> result = appelOffreService.getAppelsForChef(chef);

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void selectionnerAppel_ShouldUpdateStatus() {
        // Arrange
        when(appelOffreRepository.findById(1L)).thenReturn(Optional.of(appelOffre));
        when(appelOffreRepository.save(any(AppelOffre.class))).thenReturn(appelOffre);

        // Act
        AppelOffre result = appelOffreService.selectionnerAppel(1L);

        // Assert
        assertEquals(Statut.ENVOYE, result.getStatut());
        verify(appelOffreRepository).save(appelOffre);
    }

    @Test
    void selectionnerAppel_ShouldThrowException_WhenNotFound() {
        // Arrange
        when(appelOffreRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AppelOffreNotFoundException.class, () ->
                appelOffreService.selectionnerAppel(999L)
        );
    }

    @Test
    void getAllAppels_ShouldReturnAllAppels() {
        // Arrange
        when(appelOffreRepository.findAll()).thenReturn(Arrays.asList(appelOffre));

        // Act
        List<AppelOffre> result = appelOffreService.getAllAppels();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void validerAppel_ShouldUpdateStatusToValide() {
        // Arrange
        when(appelOffreRepository.findById(1L)).thenReturn(Optional.of(appelOffre));
        when(appelOffreRepository.save(any(AppelOffre.class))).thenReturn(appelOffre);

        // Act
        AppelOffre result = appelOffreService.validerAppel(1L);

        // Assert
        assertEquals(Statut.VALIDE, result.getStatut());
        verify(appelOffreRepository).save(appelOffre);
    }

    @Test
    void refuserAppel_ShouldUpdateStatusToRejete() {
        // Arrange
        when(appelOffreRepository.findById(1L)).thenReturn(Optional.of(appelOffre));
        when(appelOffreRepository.save(any(AppelOffre.class))).thenReturn(appelOffre);

        // Act
        AppelOffre result = appelOffreService.refuserAppel(1L);

        // Assert
        assertEquals(Statut.REJETE, result.getStatut());
        verify(appelOffreRepository).save(appelOffre);
    }

    @Test
    void enregistrerFichier_ShouldReturnFilename() throws IOException {
        // Arrange
        when(fichier1.isEmpty()).thenReturn(false);
        when(fichier1.getOriginalFilename()).thenReturn("test.pdf");

        String uploadDir = System.getProperty("user.dir") + File.separator + "test-uploads";

        // Act
        String result = (String) ReflectionTestUtils.invokeMethod(
                appelOffreService,
                "enregistrerFichier",
                fichier1,
                uploadDir
        );

        // Assert
        assertEquals("test.pdf", result);
        verify(fichier1).transferTo(any(File.class));
    }

    @Test
    void enregistrerFichier_ShouldReturnNull_WhenFileIsEmpty() {
        // Arrange
        when(fichier1.isEmpty()).thenReturn(true);

        // Act
        String result = (String) ReflectionTestUtils.invokeMethod(
                appelOffreService,
                "enregistrerFichier",
                fichier1,
                "test-dir"
        );

        // Assert
        assertNull(result);
    }

    @Test
    void create_ShouldSetCorrectAttributes() throws IOException {
        // Arrange
        when(userRepository.findByRole(Role.CHEF_DE_PROJET))
                .thenReturn(Collections.singletonList(chef));
        when(fichier1.isEmpty()).thenReturn(false);
        when(fichier1.getOriginalFilename()).thenReturn("fichier1.pdf");
        when(appelOffreRepository.save(any(AppelOffre.class))).thenAnswer(invocation -> {
            AppelOffre saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // Act
        AppelOffre result = appelOffreService.create(appelOffreRequest);

        // Assert
        assertEquals("Nouveau Appel", result.getTitre());
        assertEquals("Organisme Test", result.getOrganisme());
        assertEquals(Statut.EN_ATTENTE, result.getStatut());
        assertFalse(result.isEnvoyeParAdmin());
        assertFalse(result.isSelectionneParChef());
        assertNull(result.getNomChefSelectionneur());
        assertNotNull(result.getDestinataires());
        assertEquals(1, result.getDestinataires().size());
    }
}