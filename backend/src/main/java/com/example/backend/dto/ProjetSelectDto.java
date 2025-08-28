package com.example.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjetSelectDto {
    private Long id;
    private String nomProjet;

    public ProjetSelectDto(Long id, String nomProjet) {
        this.id = id;
        this.nomProjet = nomProjet;
    }
}

