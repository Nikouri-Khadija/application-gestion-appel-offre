package com.example.backend.controller;

import com.example.backend.dto.ProjetRequest;
import com.example.backend.entity.Projet;
import com.example.backend.service.ProjetService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projets")
@CrossOrigin(origins = "*") // autoriser les appels depuis Angular
public class ProjetController {

    private final ProjetService projetService;

    public ProjetController(ProjetService projetService) {
        this.projetService = projetService;
    }

    @PostMapping("/ajouter")
    public ResponseEntity<Projet> ajouterProjet(@RequestBody ProjetRequest request) {
        Projet saved = projetService.ajouterProjet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }


    @GetMapping("/all")
    public List<Projet> getAllProjets() {
        return projetService.getAllProjets();
    }

    @DeleteMapping("/{id}")
    public void supprimerProjet(@PathVariable Long id) {
        projetService.supprimerProjet(id);
    }

    @PutMapping("/modifier/{id}")
    public Projet modifierProjet(@PathVariable Long id, @RequestBody ProjetRequest request) {
        return projetService.modifierProjet(id, request);
    }
    @GetMapping("/chef/{chefId}")
    public List<Projet> getProjetsParChef(@PathVariable Long chefId) {
        return projetService.getProjetsParChef(chefId);
    }
    @GetMapping("/compteurs")
    public ResponseEntity<Map<String, Integer>> getCompteurs() {
        return ResponseEntity.ok(projetService.getCompteurs());
    }


}
