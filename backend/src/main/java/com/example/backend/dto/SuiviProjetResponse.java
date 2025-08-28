package com.example.backend.dto;

import lombok.*;

import java.util.List;
@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuiviProjetResponse {
    private String nomProjet;
    private double progressionGlobale;   // % progression
    private int nbTaches;
    private int nbConsultants;
    private long joursRestants;
    private long nbAFaire;
    private long nbEnCours;
    private long nbTermine;
    private long nbBloque;
    private long nbRetarde;
    private List<TacheResponse> taches; // tableau détaillé des tâches
}
