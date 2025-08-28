package com.example.backend.controller;

import com.example.backend.dto.ConsultantRequest;
import com.example.backend.dto.ConsultantResponse;
import com.example.backend.dto.ProjetResponse;
import com.example.backend.entity.Consultant;
import com.example.backend.service.ConsultantService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultants")
public class ConsultantController {

    private final ConsultantService consultantService;

    public ConsultantController(ConsultantService consultantService) {
        this.consultantService = consultantService;
    }

    // Pour récupérer la liste des emails des users qui ont le rôle consultant
    @GetMapping("/emails")
    @PreAuthorize("hasRole('CHEF_DE_PROJET')")
    public ResponseEntity<List<String>> getEmailsConsultants() {
        List<String> emails = consultantService.getEmailsConsultants();
        return ResponseEntity.ok(emails);
    }

    // Pour récupérer la liste des noms des projets
    @GetMapping("/projets")
    @PreAuthorize("hasRole('CHEF_DE_PROJET')")
    public ResponseEntity<List<String>> getNomProjets() {
        List<String> projets = consultantService.getNomProjets();
        return ResponseEntity.ok(projets);
    }

    // Pour enregistrer un consultant à partir de la requête du formulaire
    @PostMapping
    @PreAuthorize("hasRole('CHEF_DE_PROJET')")
    public ResponseEntity<Consultant> addConsultant(@RequestBody ConsultantRequest request) {
        Consultant saved = consultantService.saveConsultant(request);
        return ResponseEntity.ok(saved);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CHEF_DE_PROJET')")
    public ResponseEntity<Consultant> updateConsultant(@PathVariable Long id, @RequestBody ConsultantRequest request) {
        Consultant updated = consultantService.updateConsultant(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CHEF_DE_PROJET')")
    public ResponseEntity<Void> deleteConsultant(@PathVariable Long id) {
        consultantService.deleteConsultant(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/details")
    @PreAuthorize("hasRole('CHEF_DE_PROJET')")
    public ResponseEntity<List<ConsultantResponse>> getAllConsultantsDetailed() {
        List<ConsultantResponse> detailedList = consultantService.getAllConsultantsDetailed();
        return ResponseEntity.ok(detailedList);
    }

    @GetMapping("/mes-projets")
    @PreAuthorize("hasRole('CONSULTANT')")
    public ResponseEntity<List<ProjetResponse>> getMesProjets(Authentication authentication) {
        String email = authentication.getName();
        List<ProjetResponse> projets = consultantService.getProjetsByConsultantEmail(email);
        return ResponseEntity.ok(projets);
    }





}
