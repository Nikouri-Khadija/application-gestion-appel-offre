package com.example.backend.repository;

import com.example.backend.entity.Consultant;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.entity.Projet;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjetRepository extends JpaRepository<Projet, Long> {
    boolean existsByCodeProjet(String codeProjet);
    List<Projet> findByChefProjetId(Long chefId);
    Optional<Projet> findByNomProjet(String nomProjet);

}
