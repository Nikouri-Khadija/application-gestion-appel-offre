package com.example.backend.repository;

import com.example.backend.entity.AppelOffre;
import com.example.backend.entity.User;
import com.example.backend.entity.Statut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppelOffreRepository extends JpaRepository<AppelOffre, Long> {

    List<AppelOffre> findByDestinatairesContainingAndStatut(User chef, Statut statut);

    List<AppelOffre> findByStatut(Statut statut);  // utiliser l’enum Statut

    long countByStatut(Statut statut);
}

