package com.example.backend.serviceTest;


import com.example.backend.entity.Notification;
import com.example.backend.repository.NotificationRepository;
import com.example.backend.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testEnvoyer() {
        notificationService.envoyer("Contenu test", "destinataire@test.com");

        // Vérifie que save() a été appelé avec un objet Notification correct
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getContenu()).isEqualTo("Contenu test");
        assertThat(saved.getDestinataire()).isEqualTo("destinataire@test.com");
        assertThat(saved.isLu()).isFalse();
        assertThat(saved.getDateEnvoi()).isNotNull();
    }

    @Test
    void testGetNotifications() {
        Notification n1 = new Notification();
        n1.setDestinataire("user@test.com");
        Notification n2 = new Notification();
        n2.setDestinataire("user@test.com");

        when(notificationRepository.findByDestinataireOrderByDateEnvoiDesc("user@test.com"))
                .thenReturn(Arrays.asList(n1, n2));

        List<Notification> result = notificationService.getNotifications("user@test.com");

        assertThat(result).hasSize(2);
        verify(notificationRepository).findByDestinataireOrderByDateEnvoiDesc("user@test.com");
    }

    @Test
    void testMarquerCommeLue() {
        Notification notif = new Notification();
        notif.setId(1L);
        notif.setLu(false);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notif));

        notificationService.marquerCommeLue(1L);

        assertThat(notif.isLu()).isTrue();
        verify(notificationRepository).save(notif);
    }

    @Test
    void testMarquerCommeLueNotFound() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        notificationService.marquerCommeLue(999L);

        // verify save n'est jamais appelé
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void testEnvoyerParUtilisateur() {
        notificationService.envoyerParUtilisateur("chef@test.com", "Projet X");

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getDestinataire()).isEqualTo("admin");
        assertThat(saved.getContenu()).contains("Chef chef a sélectionné l'appel : Projet X");
        assertThat(saved.isLu()).isFalse();
        assertThat(saved.getDateEnvoi()).isNotNull();
    }
}
