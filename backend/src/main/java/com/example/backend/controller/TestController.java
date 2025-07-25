package com.example.backend.controller;


import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminAccess() {
        return "Bienvenue ADMIN";
    }

    @GetMapping("/chef")
    @PreAuthorize("hasRole('CHEF')")
    public String chefProjetAccess() {
        return "Bienvenue CHEF DE PROJET";
    }

    @GetMapping("/consultant")
    @PreAuthorize("hasRole('CONSULTANT')")
    public String consultantAccess() {
        return "Bienvenue CONSULTANT";
    }
}
