package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatistiquesTacheResponse {
    private long nbEnCours;
    private long nbAFaire;
    private long nbTermine;
    private long nbBloque;
    private long nbRetarde;
}
