package com.example.backend.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class AppelOffreRequest {
    private String titre;
    private String organisme;
    private LocalDate dateCreation;
    private LocalDate dateLimite;
    private Long idChefProjet;
    private MultipartFile fichier; // PDF
}
