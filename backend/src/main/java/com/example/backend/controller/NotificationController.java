package com.example.backend.controller;

import com.example.backend.entity.Notification;
import com.example.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    public void envoyerNotification(@RequestBody Map<String, String> data) {
        String contenu = data.get("contenu");
        String destinataire = data.get("destinataire");
        notificationService.envoyer(contenu, destinataire);
    }

    @GetMapping("/{destinataire}")
    public List<Notification> getNotifications(@PathVariable String destinataire) {
        return notificationService.getNotifications(destinataire);
    }
    @PutMapping("/lu/{id}")
    public void marquerCommeLue(@PathVariable Long id) {
        notificationService.marquerCommeLue(id);
    }

    @PostMapping("/chef-selection")
    public void notificationSelectionChef(@RequestBody Map<String, String> data) {
        String email = data.get("email");
        String titre = data.get("titre");
        notificationService.envoyerParUtilisateur(email, titre);
    }


}
