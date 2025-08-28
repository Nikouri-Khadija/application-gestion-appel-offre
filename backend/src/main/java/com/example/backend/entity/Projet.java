package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Projet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomProjet;
    @ManyToOne
    @JoinColumn(name = "appel_offre_id")
    private AppelOffre appelOffre;


    @Column(nullable = false, unique = true)
    private String codeProjet;

    @ManyToOne
    @JoinColumn(name = "chef_id", nullable = false)
    private User chefProjet;  // suppose que User est l'entité représentant les chefs

    private LocalDate dateCreation;
    private LocalDate dateLimite;

    @Column(length = 1000)
    private String description;





}
