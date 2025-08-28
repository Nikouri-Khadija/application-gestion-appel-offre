package com.example.backend.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;



@Getter
@Setter
public class AppelOffreRequest {
    private String titre;
    private String organisme;
    private String estimation;
    private String cautionProvisoire;
    private String dateCreation;
    private String dateLimite;


    private MultipartFile fichier1;
    private MultipartFile fichier2;
    private MultipartFile fichier3;
    private MultipartFile fichier4;
}