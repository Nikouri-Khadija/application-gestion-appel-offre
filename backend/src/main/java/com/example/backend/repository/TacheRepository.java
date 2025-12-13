package com.example.backend.repository;

import com.example.backend.entity.Tache;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TacheRepository extends JpaRepository<Tache, Long> {
    List<Tache> findByProjet_NomProjet(String nomProjet);
    List<Tache> findByConsultant_User_Email(String email);
}
