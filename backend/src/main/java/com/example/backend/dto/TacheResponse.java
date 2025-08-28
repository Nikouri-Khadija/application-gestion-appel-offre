package com.example.backend.dto;

import com.example.backend.entity.Priority;
import com.example.backend.entity.StatutTache;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TacheResponse {
    private Long id;
    private Long consultantId;
    private Long projetId;
    private String nomTache;
    private String nomConsultant;
    private String nomProjet;
    private Priority priorite;
    private StatutTache statut;
    private String dateCreation;
    private String dateAffectation;
    private String dateLimite;
    private String description;
    private String commentaire;


}
