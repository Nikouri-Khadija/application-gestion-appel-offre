package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjetResponse {
    private String nomProjet;
    private String codeProjet;
    private String descriptionProjet;
    private String dateAffectation;
    private String organisme;


}
