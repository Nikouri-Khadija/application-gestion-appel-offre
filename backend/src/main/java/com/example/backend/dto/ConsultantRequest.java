package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter

public class ConsultantRequest {
    private String nomComplet;
    private String email;
    private String organisme;
    private String nomProjet;       // un seul projet sélectionné
    private String dateAffectation;
    private String descriptionProjet;


}
