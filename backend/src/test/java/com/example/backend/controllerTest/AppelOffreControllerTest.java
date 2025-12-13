package com.example.backend.controllerTest;

import com.example.backend.controller.AppelOffreController;
import com.example.backend.dto.AppelOffreRequest;
import com.example.backend.entity.AppelOffre;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AppelOffreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AppelOffreControllerTest {

    private AppelOffreService service;
    private UserRepository userRepo;
    private AppelOffreController controller;

    @BeforeEach
    void setup() {
        service = Mockito.mock(AppelOffreService.class);
        userRepo = Mockito.mock(UserRepository.class);
        controller = new AppelOffreController(service, userRepo);
    }

    // =========================
    // Test création appel (Admin)
    // =========================
    @Test
    void createAppel_shouldReturnAppel() throws IOException {
        AppelOffreRequest request = new AppelOffreRequest();
        AppelOffre appel = new AppelOffre();

        Mockito.when(service.create(request)).thenReturn(appel);

        AppelOffre result = controller.createAppel(request);

        assertNotNull(result);
        assertEquals(appel, result);
    }

    // =========================
    // Test récupération appels chef
    // =========================
    @Test
    void getAppelsForChef_shouldReturnList() {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("chef@test.com");

        User chef = new User();
        Mockito.when(userRepo.findByEmail("chef@test.com")).thenReturn(Optional.of(chef));

        AppelOffre appel = new AppelOffre();
        Mockito.when(service.getAppelsForChef(chef)).thenReturn(List.of(appel));

        List<AppelOffre> result = controller.getAppelsForChef(auth);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(appel, result.get(0));
    }

    @Test
    void getAppelsForChef_userNotFound_shouldThrow() {
        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.getName()).thenReturn("unknown@test.com");
        Mockito.when(userRepo.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> controller.getAppelsForChef(auth));

        assertEquals("Utilisateur non trouvé", ex.getMessage());
    }

    // =========================
    // Test sélectionner un appel
    // =========================
    @Test
    void selectionnerAppel_shouldReturnAppel() {
        AppelOffre appel = new AppelOffre();
        Mockito.when(service.selectionnerAppel(1L)).thenReturn(appel);

        AppelOffre result = controller.selectionnerAppel(1L);

        assertNotNull(result);
        assertEquals(appel, result);
    }

    // =========================
    // Test valider un appel (Admin)
    // =========================
    @Test
    void validerAppel_shouldReturnAppel() {
        AppelOffre appel = new AppelOffre();
        Mockito.when(service.validerAppel(1L)).thenReturn(appel);

        AppelOffre result = controller.validerAppel(1L);

        assertNotNull(result);
        assertEquals(appel, result);
    }

    // =========================
    // Test refuser un appel (Admin)
    // =========================
    @Test
    void refuserAppel_shouldReturnAppel() {
        AppelOffre appel = new AppelOffre();
        Mockito.when(service.refuserAppel(1L)).thenReturn(appel);

        AppelOffre result = controller.refuserAppel(1L);

        assertNotNull(result);
        assertEquals(appel, result);
    }

    // =========================
    // Test récupérer tous les appels (Admin)
    // =========================
    @Test
    void getAllAppels_shouldReturnList() {
        AppelOffre appel = new AppelOffre();
        Mockito.when(service.getAllAppels()).thenReturn(List.of(appel));

        List<AppelOffre> result = controller.getAllAppels();

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
