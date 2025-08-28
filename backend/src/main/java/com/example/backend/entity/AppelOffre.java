package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;
@Setter
@Getter
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
    private Double estimation;
    private Double cautionProvisoire;
    private String fichier1;
    private String fichier2;
    private String fichier3;
    private String fichier4;
    @Column(name = "envoye_par_admin")
    private boolean envoyeParAdmin = false;

    @Column(name = "selectionne_par_chef")
    private boolean selectionneParChef = false;

    @Column(name = "nom_chef_selectionneur")
    private String nomChefSelectionneur;

    @Enumerated(EnumType.STRING)
    private Statut statut;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "appel_destinataires",
            joinColumns = @JoinColumn(name = "appel_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> destinataires;
}

