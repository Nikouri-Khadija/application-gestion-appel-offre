package com.example.backend.service;

import com.example.backend.dto.SuiviProjetResponse;
import com.example.backend.dto.TacheResponse;
import com.example.backend.entity.Projet;
import com.example.backend.entity.Tache;
import com.example.backend.entity.StatutTache;
import com.example.backend.repository.ProjetRepository;
import com.example.backend.repository.TacheRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuiviProjetService {

    private final ProjetRepository projetRepository;
    private final TacheRepository tacheRepository;
    private final TacheService tacheService;

    public SuiviProjetService(ProjetRepository projetRepository,
                              TacheRepository tacheRepository,
                              TacheService tacheService) {
        this.projetRepository = projetRepository;
        this.tacheRepository = tacheRepository;
        this.tacheService = tacheService;
    }

    // ✅ Récupérer le suivi par ID
    public SuiviProjetResponse getSuiviProjet(Long projetId) {
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));
        return getSuiviParProjet(projet.getNomProjet());
    }

    // ✅ Récupérer le suivi par NOM du projet
    public SuiviProjetResponse getSuiviParProjet(String nomProjet) {
        Projet projet = projetRepository.findByNomProjet(nomProjet)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        // Récupération des tâches liées au projet
        List<Tache> taches = tacheRepository.findByProjet_NomProjet(projet.getNomProjet());
        long total = taches.size();

        // Progression globale
        long terminees = taches.stream()
                .filter(t -> t.getStatut() == StatutTache.TERMINE)
                .count();
        double progression = total > 0 ? (terminees * 100.0 / total) : 0;

        // Nombre de consultants distincts affectés au projet via les tâches
        int nbConsultants = (int) taches.stream()
                .filter(t -> t.getConsultant() != null)
                .map(t -> t.getConsultant().getId())
                .distinct()
                .count();

        // Jours restants avant la deadline
        long joursRestants = projet.getDateLimite() != null ?
                ChronoUnit.DAYS.between(LocalDate.now(), projet.getDateLimite()) : 0;

        // Comptage par statut
        long nbAFaire = taches.stream().filter(t -> t.getStatut() == StatutTache.A_FAIRE).count();
        long nbEnCours = taches.stream().filter(t -> t.getStatut() == StatutTache.EN_COURS).count();
        long nbBloque = taches.stream().filter(t -> t.getStatut() == StatutTache.BLOQUE).count();

        // Retardé = deadline dépassée et pas terminé
        long nbRetarde = taches.stream()
                .filter(t -> t.getDateLimite() != null &&
                        t.getDateLimite().isBefore(LocalDate.now()) &&
                        t.getStatut() != StatutTache.TERMINE)
                .count();

        // Mapping des tâches en DTO
        List<TacheResponse> tacheDtos = taches.stream()
                .map(tacheService::mapToResponse)
                .toList();

        // Retour DTO enrichi avec toutes les cartes
        return new SuiviProjetResponse(
                projet.getNomProjet(),
                progression,
                (int) total,
                nbConsultants,
                joursRestants,
                nbAFaire,
                nbEnCours,
                terminees,
                nbBloque,
                nbRetarde,
                tacheDtos
        );
    }

    public List<String> getAllProjets(Long chefId) {
        return projetRepository.findByChefProjetId(chefId).stream()
                .map(Projet::getNomProjet)
                .collect(Collectors.toList());
    }
}
