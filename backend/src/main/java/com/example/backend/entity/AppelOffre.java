package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppelOffre {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    private String organisme;

    private LocalDate dateCreation;

    private LocalDate dateLimite;

    private String fichierPdfPath; // ex : "uploads/appel123.pdf"

    @Enumerated(EnumType.STRING)
    private Statut statut;

    @ManyToOne
    private User destinataire; // Le chef de projet ciblé
}
