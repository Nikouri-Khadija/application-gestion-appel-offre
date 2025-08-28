package com.example.backend.dto;

import com.example.backend.entity.Priority;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TacheRequest {
    private String nomTache;
    private Long consultantId;
    private Long projetId;
    private Priority priorite;
    private String dateAffectation;
    private String dateLimite;
    private String description;
}
