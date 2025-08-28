package com.example.backend.dto;
import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsultantResponse {
    private Long id;
    private String nomComplet;
    private String email;
    private String organisme;
    private String nomProjet;
    private String dateAffectation;
    private String descriptionProjet;

}

