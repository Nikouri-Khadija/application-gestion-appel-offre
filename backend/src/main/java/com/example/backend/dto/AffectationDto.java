package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AffectationDto {
    private Long userId;
    private String nomConsultant;
    private Long projetId;
    private String nomProjet;}