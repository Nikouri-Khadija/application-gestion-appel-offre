package com.example.backend.service;

import com.example.backend.dto.StatistiquesTacheResponse;
import com.example.backend.dto.TacheRequest;
import com.example.backend.dto.TacheResponse;
import com.example.backend.entity.*;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.ConsultantRepository;
import com.example.backend.repository.ProjetRepository;
import com.example.backend.repository.TacheRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class TacheService {

    private final TacheRepository tacheRepository;
    private final ConsultantRepository consultantRepository;
    private final ProjetRepository projetRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public TacheService(TacheRepository tacheRepository,
                        ConsultantRepository consultantRepository,
                        ProjetRepository projetRepository) {
        this.tacheRepository = tacheRepository;
        this.consultantRepository = consultantRepository;
        this.projetRepository = projetRepository;
    }

    public TacheResponse createTache(TacheRequest request) {
        Consultant consultant = consultantRepository.findByUserId(request.getConsultantId())
                .stream().findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Consultant non trouvé"));

        Projet projet = projetRepository.findById(request.getProjetId())
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé"));

        Tache tache = new Tache();
        tache.setNomTache(request.getNomTache());
        tache.setConsultant(consultant);
        tache.setProjet(projet);
        tache.setPriorite(request.getPriorite());
        tache.setDescription(request.getDescription());
        tache.setDateAffectation(parseDate(request.getDateAffectation()));
        tache.setDateLimite(parseDate(request.getDateLimite()));

        return mapToResponse(tacheRepository.save(tache));
    }

    private LocalDate parseDate(String dateStr) {
        return dateStr != null ? LocalDate.parse(dateStr, formatter) : null;
    }

    public List<TacheResponse> getAllTaches() {
        return tacheRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TacheResponse updateStatut(Long id, StatutTache statut, String commentaire, User user) {
        Tache tache = tacheRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tâche non trouvée"));

        // Gestion des permissions selon le rôle
        if (user.getRole() == Role.CONSULTANT) {
            handleConsultantUpdate(tache, statut, commentaire);
        } else if (user.getRole() == Role.CHEF_DE_PROJET) {
            handleChefDeProjetUpdate(tache, statut, commentaire);
        } else {
            throw new AccessDeniedException("Rôle non autorisé");
        }

        return mapToResponse(tacheRepository.save(tache));
    }

    private void handleConsultantUpdate(Tache tache, StatutTache newStatut, String commentaire) {
        // Vérifier les permissions du consultant
        if (newStatut != StatutTache.EN_COURS
                && newStatut != StatutTache.BLOQUE
                && newStatut != StatutTache.EN_ATTENTE_VALIDATION) {
            throw new AccessDeniedException("Un consultant ne peut pas changer ce statut");
        }

        // Vérifier commentaire pour BLOQUE
        if (newStatut == StatutTache.BLOQUE && (commentaire == null || commentaire.trim().isEmpty())) {
            throw new IllegalArgumentException("Un commentaire est obligatoire pour bloquer une tâche");
        }

        // Mettre à jour
        tache.setStatut(newStatut);
        if (newStatut == StatutTache.BLOQUE) {
            tache.setCommentaire(commentaire);
        }
    }

    private void handleChefDeProjetUpdate(Tache tache, StatutTache newStatut, String commentaire) {
        // Vérifier transition TERMINE
        if (newStatut == StatutTache.TERMINE && tache.getStatut() != StatutTache.EN_ATTENTE_VALIDATION) {
            throw new IllegalArgumentException("Une tâche ne peut être terminée que si elle est en attente de validation");
        }

        // Mettre à jour
        tache.setStatut(newStatut);
        if (newStatut == StatutTache.BLOQUE) {
            tache.setCommentaire(commentaire);
        }
    }

    public void deleteTache(Long id) {
        tacheRepository.deleteById(id);
    }

    public TacheResponse mapToResponse(Tache tache) {
        TacheResponse dto = new TacheResponse();
        dto.setId(tache.getId());
        dto.setConsultantId(tache.getConsultant().getUser().getId());
        dto.setProjetId(tache.getProjet().getId());
        dto.setNomTache(tache.getNomTache());
        dto.setNomConsultant(tache.getConsultant().getNomComplet());
        dto.setNomProjet(tache.getProjet().getNomProjet());
        dto.setPriorite(tache.getPriorite());
        dto.setStatut(tache.getStatut());

        // CORRECTION : LocalDateTime pour dateCreation
        dto.setDateCreation(formatDateTime(tache.getDateCreation()));

        dto.setDateAffectation(formatDate(tache.getDateAffectation()));
        dto.setDateLimite(formatDate(tache.getDateLimite()));
        dto.setDescription(tache.getDescription());
        dto.setCommentaire(tache.getCommentaire());
        return dto;
    }

    // CORRECTION : Paramètre LocalDateTime
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(formatter) : null;
    }

    public StatistiquesTacheResponse getStatistiquesParProjet(String nomProjet) {
        List<Tache> taches = tacheRepository.findByProjet_NomProjet(nomProjet);

        long nbAFaire = countByStatus(taches, StatutTache.A_FAIRE);
        long nbEnCours = countByStatus(taches, StatutTache.EN_COURS);
        long nbTermine = countByStatus(taches, StatutTache.TERMINE);
        long nbBloque = countByStatus(taches, StatutTache.BLOQUE);
        long nbRetarde = countRetardedTasks(taches);

        return new StatistiquesTacheResponse(nbEnCours, nbAFaire, nbTermine, nbBloque, nbRetarde);
    }

    private long countByStatus(List<Tache> taches, StatutTache status) {
        return taches.stream()
                .filter(t -> t.getStatut() == status)
                .count();
    }

    private long countRetardedTasks(List<Tache> taches) {
        LocalDate today = LocalDate.now();
        return taches.stream()
                .filter(t -> t.getDateLimite() != null)
                .filter(t -> t.getDateLimite().isBefore(today))
                .filter(t -> t.getStatut() != StatutTache.TERMINE)
                .count();
    }

    public List<TacheResponse> getTachesParEmailConsultant(String email) {
        return tacheRepository.findByConsultant_User_Email(email)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
}


