package com.example.backend.controllerTest;

import com.example.backend.controller.NotificationController;
import com.example.backend.entity.Notification;
import com.example.backend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;


import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    @InjectMocks
    private NotificationController notificationController;

    @Mock
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testEnvoyerNotification() {
        Map<String, String> data = new HashMap<>();
        data.put("contenu", "Test notification");
        data.put("destinataire", "user@example.com");

        doNothing().when(notificationService).envoyer("Test notification", "user@example.com");

        notificationController.envoyerNotification(data);

        verify(notificationService, times(1)).envoyer("Test notification", "user@example.com");
    }

    @Test
    void testGetNotifications() {
        String destinataire = "user@example.com";
        Notification n1 = new Notification(1L, "Message 1", destinataire, LocalDateTime.now(), false);
        Notification n2 = new Notification(2L, "Message 2", destinataire, LocalDateTime.now(), true);

        when(notificationService.getNotifications(destinataire)).thenReturn(Arrays.asList(n1, n2));

        List<Notification> result = notificationController.getNotifications(destinataire);

        assertEquals(2, result.size());
        assertEquals("Message 1", result.get(0).getContenu());
        assertFalse(result.get(0).isLu());
        assertTrue(result.get(1).isLu());
        verify(notificationService, times(1)).getNotifications(destinataire);
    }

    @Test
    void testMarquerCommeLue() {
        Long id = 1L;

        doAnswer(invocation -> {
            Notification n = new Notification();
            n.setId(id);
            n.setLu(true);
            return null;
        }).when(notificationService).marquerCommeLue(id);

        notificationController.marquerCommeLue(id);

        verify(notificationService, times(1)).marquerCommeLue(id);
    }

    @Test
    void testNotificationSelectionChef() {
        Map<String, String> data = new HashMap<>();
        data.put("email", "chef@example.com");
        data.put("titre", "Titre de notification");

        doNothing().when(notificationService).envoyerParUtilisateur("chef@example.com", "Titre de notification");

        notificationController.notificationSelectionChef(data);

        verify(notificationService, times(1)).envoyerParUtilisateur("chef@example.com", "Titre de notification");
    }
}
