package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.entity.*;
import com.example.backend.repository.ConsultantRepository;
import com.example.backend.repository.ProjetRepository;
import com.example.backend.repository.TacheRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.TacheService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/taches")
public class TacheController {

    private final TacheService tacheService;
    private final ConsultantRepository consultantRepository;
    private final UserRepository userRepository;
    private final TacheRepository tacheRepository;
    private final ProjetRepository projetRepository;

    public TacheController(TacheService tacheService,
                           ConsultantRepository consultantRepository,
                           UserRepository userRepository,
                           TacheRepository tacheRepository,
                           ProjetRepository projetRepository) {
        this.tacheService = tacheService;
        this.consultantRepository = consultantRepository;
        this.userRepository = userRepository;
        this.tacheRepository = tacheRepository;
        this.projetRepository = projetRepository;
    }

    // ✅ Création de tâche
    @PostMapping
    @PreAuthorize("hasRole('CHEF_DE_PROJET')")
    public ResponseEntity<TacheResponse> createTache(@RequestBody TacheRequest request) {
        return ResponseEntity.ok(tacheService.createTache(request));
    }


    @GetMapping
    @PreAuthorize("hasAnyRole('CHEF_DE_PROJET','CONSULTANT')")
    public ResponseEntity<List<TacheResponse>> getAllTaches() {
        return ResponseEntity.ok(tacheService.getAllTaches());
    }

    // ✅ Suppression
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CHEF_DE_PROJET')")
    public ResponseEntity<Void> deleteTache(@PathVariable Long id) {
        tacheService.deleteTache(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ Statistiques par projet
    @GetMapping("/statistiques/{nomProjet}")
    @PreAuthorize("hasRole('CHEF_DE_PROJET')")
    public ResponseEntity<StatistiquesTacheResponse> getStatistiques(@PathVariable String nomProjet) {
        return ResponseEntity.ok(tacheService.getStatistiquesParProjet(nomProjet));
    }

    @GetMapping("/affectations")
    @PreAuthorize("hasRole('CHEF_DE_PROJET')")
    public ResponseEntity<List<AffectationDto>> getAffectations() {
        List<Consultant> affectations = consultantRepository.findAll();

        List<AffectationDto> dtos = affectations.stream()
                .map(a -> new AffectationDto(
                        a.getUser().getId(),
                        a.getUser().getNomComplet(),
                        a.getProjet().getId(),
                        a.getProjet().getNomProjet()
                ))
                .toList(); // ✅ Remplacement Collectors.toList()

        return ResponseEntity.ok(dtos);
    }

    // Liste des consultants sans doublons
    @GetMapping("/consultants")
    public ResponseEntity<List<Map<String, Object>>> getConsultants() {
        List<Map<String, Object>> consultants = consultantRepository.findAll()
                .stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getUser().getId());
                    map.put("nomComplet", c.getNomComplet());
                    return map;
                })
                .distinct()
                .toList(); // ✅ Remplacement

        return ResponseEntity.ok(consultants);
    }

    @GetMapping("/consultants/{consultantId}/projets")
    @PreAuthorize("hasRole('CHEF_DE_PROJET')")
    public ResponseEntity<List<Map<String, Object>>> getProjetsByConsultant(@PathVariable Long consultantId) {
        List<Consultant> affectations = consultantRepository.findByUserId(consultantId);

        List<Map<String, Object>> projets = affectations.stream()
                .map(c -> {
                    Projet p = c.getProjet();
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("nomProjet", p.getNomProjet());
                    return map;
                })
                .distinct()
                .toList(); // ✅ Remplacement

        return ResponseEntity.ok(projets);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CHEF_DE_PROJET')")
    public ResponseEntity<TacheResponse> updateDetails(
            @PathVariable Long id,
            @RequestBody TacheUpdateRequest request) {

        Tache tache = tacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tâche non trouvée"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (request.getDateAffectation() != null && !request.getDateAffectation().isBlank()) {
            tache.setDateAffectation(LocalDate.parse(request.getDateAffectation(), formatter));
        }

        if (request.getDateLimite() != null && !request.getDateLimite().isBlank()) {
            tache.setDateLimite(LocalDate.parse(request.getDateLimite(), formatter));
        }

        if (request.getConsultantId() != null) {
            Consultant consultant = consultantRepository.findByUserId(request.getConsultantId())
                    .stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Consultant non trouvé"));
            tache.setConsultant(consultant);
        }

        if (request.getProjetId() != null) {
            Projet projet = projetRepository.findById(request.getProjetId())
                    .orElseThrow(() -> new RuntimeException("Projet non trouvé"));
            tache.setProjet(projet);
        }

        if (request.getPriorite() != null) {
            tache.setPriorite(request.getPriorite());
        }

        tache.setDescription(request.getDescription());
        tache.setCommentaire(request.getCommentaire());

        try {
            return ResponseEntity.ok(tacheService.mapToResponse(tacheRepository.save(tache)));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la mise à jour", e);
        }
    }

    @PutMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('CHEF_DE_PROJET','CONSULTANT')")
    public ResponseEntity<TacheResponse> updateStatut(
            @PathVariable Long id,
            @RequestParam StatutTache statut,
            @RequestParam(required = false) String commentaire) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return ResponseEntity.ok(tacheService.updateStatut(id, statut, commentaire, user));
    }

    @GetMapping("/mes-taches")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<List<TacheResponse>> getMesTaches(
            @RequestParam(required = false) String email) {

        if (email == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            email = authentication.getName();
        }

        return ResponseEntity.ok(tacheService.getTachesParEmailConsultant(email));
    }
}
