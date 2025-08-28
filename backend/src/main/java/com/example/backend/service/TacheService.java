package com.example.backend.service;

import com.example.backend.dto.ProjetSelectDto;
import com.example.backend.dto.StatistiquesTacheResponse;
import com.example.backend.dto.TacheRequest;
import com.example.backend.dto.TacheResponse;
import com.example.backend.entity.*;
import com.example.backend.repository.ConsultantRepository;
import com.example.backend.repository.ProjetRepository;
import com.example.backend.repository.TacheRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TacheService {

    private final TacheRepository tacheRepository;
    private final ConsultantRepository consultantRepository;
    private final ProjetRepository projetRepository;


    public TacheService(TacheRepository tacheRepository,
                        ConsultantRepository consultantRepository,
                        ProjetRepository projetRepository) {
        this.tacheRepository = tacheRepository;
        this.consultantRepository = consultantRepository;
        this.projetRepository = projetRepository;
    }

    // ✅ Création tâche (CHEF_DE_PROJET)
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public TacheResponse createTache(TacheRequest request) {
        Consultant consultant = consultantRepository.findByUserId(request.getConsultantId())
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Consultant non trouvé pour cet utilisateur"));

        Projet projet = projetRepository.findById(request.getProjetId())
                .orElseThrow(() -> new RuntimeException("Projet non trouvé"));

        Tache tache = new Tache();
        tache.setNomTache(request.getNomTache());
        tache.setConsultant(consultant);
        tache.setProjet(projet);
        tache.setPriorite(request.getPriorite());
        tache.setDescription(request.getDescription());

        // Dates saisies dans le formulaire
        if (request.getDateAffectation() != null) {
            tache.setDateAffectation(LocalDate.parse(request.getDateAffectation(), formatter));
        }
        if (request.getDateLimite() != null) {
            tache.setDateLimite(LocalDate.parse(request.getDateLimite(), formatter));
        }

        // dateCreation = automatiquement LocalDateTime.now() dans l'entité
        return mapToResponse(tacheRepository.save(tache));
    }



    public List<TacheResponse> getAllTaches() {
        return tacheRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ✅ Mise à jour statut avec vérification des rôles
    public TacheResponse updateStatut(Long id, StatutTache statut, String commentaire, User user) {
        Tache tache = tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));

        if (user.getRole() == Role.CONSULTANT) {
            // Consultant peut modifier uniquement le statut vers EN_COURS, BLOQUE ou EN_ATTENTE_VALIDATION
            if (!(statut == StatutTache.EN_COURS || statut == StatutTache.BLOQUE || statut == StatutTache.EN_ATTENTE_VALIDATION)) {
                throw new AccessDeniedException("Un consultant ne peut pas changer ce statut");
            }
            if (statut == StatutTache.BLOQUE && (commentaire == null || commentaire.isBlank())) {
                throw new RuntimeException("Un commentaire est obligatoire pour bloquer une tâche");
            }
            tache.setStatut(statut);
            if (statut == StatutTache.BLOQUE) tache.setCommentaire(commentaire);
        }

        if (user.getRole() == Role.CHEF_DE_PROJET) {
            // Chef de projet peut modifier tous les champs et mettre TERMINÉ seulement si EN_ATTENTE_VALIDATION
            if (statut == StatutTache.TERMINE && tache.getStatut() != StatutTache.EN_ATTENTE_VALIDATION) {
                throw new RuntimeException("Une tâche ne peut être terminée que si elle est en attente de validation");
            }
            tache.setStatut(statut);
            if (statut == StatutTache.BLOQUE) tache.setCommentaire(commentaire);
        }

        return mapToResponse(tacheRepository.save(tache));
    }

    public void deleteTache(Long id) {
        tacheRepository.deleteById(id);
    }

    public TacheResponse mapToResponse(Tache tache) {
        TacheResponse dto = new TacheResponse();
        dto.setId(tache.getId());
        dto.setConsultantId(tache.getConsultant().getUser().getId()); // ou getId() selon ta structure
        dto.setProjetId(tache.getProjet().getId());

        dto.setNomTache(tache.getNomTache());
        dto.setNomConsultant(tache.getConsultant().getNomComplet());
        dto.setNomProjet(tache.getProjet().getNomProjet());
        dto.setPriorite(tache.getPriorite());
        dto.setStatut(tache.getStatut());
        dto.setDateCreation(tache.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        if (tache.getDateAffectation() != null)
            dto.setDateAffectation(tache.getDateAffectation().format(formatter));
        if (tache.getDateLimite() != null)
            dto.setDateLimite(tache.getDateLimite().format(formatter));
        dto.setDescription(tache.getDescription());
        dto.setCommentaire(tache.getCommentaire());
        return dto;
    }

    // ✅ Statistiques par projet avec statut retardé
    public StatistiquesTacheResponse getStatistiquesParProjet(String nomProjet) {
        List<Tache> taches = tacheRepository.findByProjet_NomProjet(nomProjet);

        LocalDate today = LocalDate.now();

        long nbAFaire = taches.stream()
                .filter(t -> t.getStatut() == StatutTache.A_FAIRE)
                .count();

        long nbEnCours = taches.stream()
                .filter(t -> t.getStatut() == StatutTache.EN_COURS)
                .count();

        long nbTermine = taches.stream()
                .filter(t -> t.getStatut() == StatutTache.TERMINE)
                .count();

        long nbBloque = taches.stream()
                .filter(t -> t.getStatut() == StatutTache.BLOQUE)
                .count();

        // Retardé = date limite dépassée et pas terminé
        long nbRetarde = taches.stream()
                .filter(t -> t.getDateLimite() != null &&
                        t.getDateLimite().isBefore(today) &&
                        t.getStatut() != StatutTache.TERMINE)
                .count();

        return new StatistiquesTacheResponse(nbEnCours, nbAFaire, nbTermine, nbBloque, nbRetarde);
    }


    // Récupérer toutes les tâches assignées à un consultant
    public List<TacheResponse> getTachesParEmailConsultant(String email) {
        List<Tache> taches = tacheRepository.findByConsultant_User_Email(email);
        return taches.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }







}
