package com.example.backend.controllerTest;

import com.example.backend.controller.ConsultantController;
import com.example.backend.dto.ConsultantRequest;
import com.example.backend.dto.ConsultantResponse;
import com.example.backend.dto.ProjetResponse;
import com.example.backend.entity.Consultant;
import com.example.backend.service.ConsultantService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ConsultantControllerTest {

    @InjectMocks
    private ConsultantController consultantController;

    @Mock
    private ConsultantService consultantService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testGetEmailsConsultants() {
        List<String> emails = List.of("consultant1@example.com", "consultant2@example.com");
        when(consultantService.getEmailsConsultants()).thenReturn(emails);

        ResponseEntity<List<String>> response = consultantController.getEmailsConsultants();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).size());
        verify(consultantService, times(1)).getEmailsConsultants();
    }

    @Test
    void testGetNomProjets() {
        List<String> projets = List.of("Projet1", "Projet2");
        when(consultantService.getNomProjets()).thenReturn(projets);

        ResponseEntity<List<String>> response = consultantController.getNomProjets();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Projet1", Objects.requireNonNull(response.getBody()).get(0));
        verify(consultantService, times(1)).getNomProjets();
    }

    @Test
    void testAddConsultant() {
        ConsultantRequest request = new ConsultantRequest();
        request.setNomComplet("Consultant1");

        Consultant saved = new Consultant();
        saved.setNomComplet("Consultant1");

        when(consultantService.saveConsultant(request)).thenReturn(saved);

        ResponseEntity<Consultant> response = consultantController.addConsultant(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Consultant1", Objects.requireNonNull(response.getBody()).getNomComplet());
        verify(consultantService, times(1)).saveConsultant(request);
    }

    @Test
    void testUpdateConsultant() {
        Long id = 1L;
        ConsultantRequest request = new ConsultantRequest();
        request.setNomComplet("Consultant Updated");

        Consultant updated = new Consultant();
        updated.setNomComplet("Consultant Updated");

        when(consultantService.updateConsultant(id, request)).thenReturn(updated);

        ResponseEntity<Consultant> response = consultantController.updateConsultant(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Consultant Updated", Objects.requireNonNull(response.getBody()).getNomComplet());
        verify(consultantService, times(1)).updateConsultant(id, request);
    }

    @Test
    void testDeleteConsultant() {
        Long id = 1L;
        doNothing().when(consultantService).deleteConsultant(id);

        ResponseEntity<Void> response = consultantController.deleteConsultant(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(consultantService, times(1)).deleteConsultant(id);
    }

    @Test
    void testGetAllConsultantsDetailed() {
        ConsultantResponse c1 = new ConsultantResponse();
        c1.setNomComplet("Consultant1");

        when(consultantService.getAllConsultantsDetailed()).thenReturn(List.of(c1));

        ResponseEntity<List<ConsultantResponse>> response = consultantController.getAllConsultantsDetailed();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Consultant1", Objects.requireNonNull(response.getBody()).get(0).getNomComplet());
        verify(consultantService, times(1)).getAllConsultantsDetailed();
    }

    @Test
    void testGetMesProjets() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("consultant@example.com");

        ProjetResponse p1 = new ProjetResponse();
        p1.setNomProjet("Projet1");

        when(consultantService.getProjetsByConsultantEmail("consultant@example.com"))
                .thenReturn(List.of(p1));

        ResponseEntity<List<ProjetResponse>> response = consultantController.getMesProjets(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Projet1", Objects.requireNonNull(response.getBody()).get(0).getNomProjet());
        verify(consultantService, times(1)).getProjetsByConsultantEmail("consultant@example.com");
    }
}
