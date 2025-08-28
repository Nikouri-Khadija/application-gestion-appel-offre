package com.example.backend.dto;

import java.time.LocalDate;
import lombok.*;
@Getter
@Setter
public class ProjetRequest {

    private String nomProjet;
    private String codeProjet;
    private Long chefId;
    private LocalDate dateCreation;
    private LocalDate dateLimite;
    private String description;

    // Nouveau champ pour lier l'appel d'offre
    private Long appelOffreId;


}
