package com.example.backend.controller;

import com.example.backend.service.NotificationService;
import org.springframework.web.bind.annotation.*;



import com.example.backend.dto.AppelOffreRequest;
import com.example.backend.entity.AppelOffre;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.AppelOffreService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;



import java.io.IOException;

import java.util.List;

@RestController
@RequestMapping("/api/appels")
@RequiredArgsConstructor
public class AppelOffreController {

    private final AppelOffreService service;
    private final UserRepository userRepo;


    // Création par Admin
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public AppelOffre createAppel(@ModelAttribute AppelOffreRequest request) throws IOException {

        return service.create(request); // retourne directement sans variable temporaire inutile
    }


    // Liste reçue par le chef de projet
    @GetMapping("/received")
    @PreAuthorize("hasRole('CHEF_DE_PROJET')")
    public List<AppelOffre> getAppelsForChef(Authentication authentication) {
        String email = authentication.getName();
        User chef = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return service.getAppelsForChef(chef);
    }

    // Sélectionner un appel
    @PostMapping("/select/{id}")
    @PreAuthorize("hasRole('CHEF_DE_PROJET')")
    public AppelOffre selectionnerAppel(@PathVariable Long id) {
        return service.selectionnerAppel(id);
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public List<AppelOffre> getAllAppels() {
        return service.getAllAppels();
    }

    // Valider un appel (par l'admin)
    @PutMapping("/valider/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AppelOffre validerAppel(@PathVariable Long id) {
        return service.validerAppel(id);
    }

    // Refuser un appel (par l'admin)
    @PutMapping("/refuser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AppelOffre refuserAppel(@PathVariable Long id) {
        return service.refuserAppel(id);
    }















}
