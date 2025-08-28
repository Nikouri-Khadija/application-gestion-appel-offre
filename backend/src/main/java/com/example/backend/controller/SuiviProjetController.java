package com.example.backend.controller;

import com.example.backend.dto.SuiviProjetResponse;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.SuiviProjetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suivi-projet")
public class SuiviProjetController {

    private final SuiviProjetService suiviProjetService;
    private final UserRepository userRepository;

    public SuiviProjetController(SuiviProjetService suiviProjetService, UserRepository userRepository) {
        this.suiviProjetService = suiviProjetService;
        this.userRepository = userRepository;
    }


    // ✅ Endpoint pour récupérer le suivi d’un projet par son NOM
    @GetMapping("/{nomProjet}")
    @PreAuthorize("hasRole('CHEF_DE_PROJET')")
    public ResponseEntity<SuiviProjetResponse> getSuiviProjet(@PathVariable String nomProjet) {
        SuiviProjetResponse suivi = suiviProjetService.getSuiviParProjet(nomProjet);
        return ResponseEntity.ok(suivi);
    }
    @GetMapping("/projets-chef")
    @PreAuthorize("hasRole('CHEF_DE_PROJET')")
    public ResponseEntity<List<String>> getNomProjetsParChef(Authentication authentication) {
        String email = authentication.getName(); // récupère l'email du chef connecté
        User chef = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Chef introuvable"));

        List<String> noms = suiviProjetService.getAllProjets(chef.getId());
        return ResponseEntity.ok(noms);
    }



}
