package com.example.backend.service;

import com.example.backend.entity.Notification;
import com.example.backend.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void envoyer(String contenu, String destinataire) {
        Notification notif = new Notification();
        notif.setContenu(contenu);
        notif.setDestinataire(destinataire);
        notif.setDateEnvoi(LocalDateTime.now());
        notif.setLu(false);
        notificationRepository.save(notif);
    }

    public List<Notification> getNotifications(String destinataire) {
        return notificationRepository.findByDestinataireOrderByDateEnvoiDesc(destinataire);
    }
    public void marquerCommeLue(Long id) {
        Notification notif = notificationRepository.findById(id).orElse(null);
        if (notif != null) {
            notif.setLu(true);
            notificationRepository.save(notif);
        }
    }


    public void envoyerParUtilisateur(String emailExpediteur, String titreAppel) {
        String username = emailExpediteur.split("@")[0]; // extrait le nom du chef
        String contenu = "Chef " + username + " a sélectionné l'appel : " + titreAppel;
        Notification notif = new Notification();
        notif.setContenu(contenu);
        notif.setDestinataire("admin");
        notif.setDateEnvoi(LocalDateTime.now());
        notif.setLu(false);
        notificationRepository.save(notif);
    }


}

