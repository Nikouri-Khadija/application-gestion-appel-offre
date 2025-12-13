package com.example.backend.repository;

import com.example.backend.entity.Consultant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ConsultantRepository extends JpaRepository<Consultant, Long> {
    List<Consultant> findByUserEmail(String email);
    List<Consultant> findByUserId(Long userId);

}
