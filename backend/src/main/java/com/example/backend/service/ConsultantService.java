package com.example.backend.service;

import com.example.backend.dto.ConsultantRequest;
import com.example.backend.dto.ConsultantResponse;
import com.example.backend.dto.ProjetResponse;
import com.example.backend.entity.Consultant;
import com.example.backend.entity.Projet;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.repository.ConsultantRepository;
import com.example.backend.repository.ProjetRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ConsultantService {

    private final ConsultantRepository consultantRepository;
    private final UserRepository userRepository;
    private final ProjetRepository projetRepository;
    private final NotificationService notificationService;

    public ConsultantService(ConsultantRepository consultantRepository,
                             UserRepository userRepository,
                             ProjetRepository projetRepository,
                             NotificationService notificationService) {
        this.consultantRepository = consultantRepository;
        this.userRepository = userRepository;
        this.projetRepository = projetRepository;
        this.notificationService = notificationService;
    }

    /**
     * Création d'un nouveau consultant avec notification
     */
    public Consultant saveConsultant(ConsultantRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User non trouvé avec email : " + request.getEmail()));

        Projet projet = projetRepository.findByNomProjet(request.getNomProjet())
                .orElseThrow(() -> new RuntimeException("Projet non trouvé : " + request.getNomProjet()));

        Consultant consultant = new Consultant();
        consultant.setNomComplet(request.getNomComplet());
        consultant.setUser(user);
        consultant.setProjet(projet);
        consultant.setDescriptionProjet(request.getDescriptionProjet());
        consultant.setOrganisme(request.getOrganisme());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        consultant.setDateAffectation(LocalDate.parse(request.getDateAffectation(), formatter));

        Consultant saved = consultantRepository.save(consultant);

        // Notification d'affectation
        notificationService.envoyer("Vous avez été affecté au projet : " + projet.getNomProjet(), user.getEmail());

        return saved;
    }

    /**
     * Mise à jour d'un consultant avec notifications personnalisées
     */
    public Consultant updateConsultant(Long consultantId, ConsultantRequest request) {
        Consultant consultant = consultantRepository.findById(consultantId)
                .orElseThrow(() -> new RuntimeException("Consultant non trouvé avec l'id : " + consultantId));

        StringBuilder changements = new StringBuilder();

        // Nom complet
        if (!consultant.getNomComplet().equals(request.getNomComplet())) {
            changements.append("Votre nom complet a été mis à jour. ");
            consultant.setNomComplet(request.getNomComplet());
        }

        // Email (user)
        if (!consultant.getUser().getEmail().equals(request.getEmail())) {
            changements.append("Votre compte utilisateur a été changé. ");
            User newUser = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User non trouvé : " + request.getEmail()));
            consultant.setUser(newUser);
        }
        if (request.getOrganisme() != null && !request.getOrganisme().equals(consultant.getOrganisme())) {
            changements.append("L'organisme a été mis à jour. ");
            consultant.setOrganisme(request.getOrganisme());
        }

        // Projet
        if (!consultant.getProjet().getNomProjet().equals(request.getNomProjet())) {
            Projet projet = projetRepository.findByNomProjet(request.getNomProjet())
                    .orElseThrow(() -> new RuntimeException("Projet non trouvé : " + request.getNomProjet()));
            changements.append("Votre projet a été changé vers : ").append(projet.getNomProjet()).append(". ");
            consultant.setProjet(projet);
        }

        // Description
        if (request.getDescriptionProjet() != null &&
                !request.getDescriptionProjet().equals(consultant.getDescriptionProjet())) {
            changements.append("La description du projet a été mise à jour. ");
            consultant.setDescriptionProjet(request.getDescriptionProjet());
        }

        // Date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate newDate = LocalDate.parse(request.getDateAffectation(), formatter);
        if (!consultant.getDateAffectation().equals(newDate)) {
            changements.append("La date d'affectation a été mise à jour. ");
            consultant.setDateAffectation(newDate);
        }

        Consultant updated = consultantRepository.save(consultant);

        // Envoi notification si au moins un changement
        if (changements.isEmpty()) {
            notificationService.envoyer(changements.toString(), consultant.getUser().getEmail());
        }

        return updated;
    }

    /**
     * Suppression d'un consultant avec notification
     */
    public void deleteConsultant(Long consultantId) {
        Consultant consultant = consultantRepository.findById(consultantId)
                .orElseThrow(() -> new RuntimeException("Consultant non trouvé avec l'id : " + consultantId));

        consultantRepository.delete(consultant);

        // Notification de suppression
        notificationService.envoyer(
                "Votre affectation au projet " + consultant.getProjet().getNomProjet() + " a été supprimée.",
                consultant.getUser().getEmail()
        );
    }

    /**
     * Récupérer tous les emails des consultants
     */
    public List<String> getEmailsConsultants() {
        return userRepository.findByRole(Role.CONSULTANT)
                .stream()
                .map(User::getEmail)
                .collect(Collectors.toList());
    }



    /**
     * Récupérer tous les noms de projets
     */
    public List<String> getNomProjets() {
        return projetRepository.findAll()
                .stream()
                .map(Projet::getNomProjet)
                .toList();
    }

    public List<ConsultantResponse> getAllConsultantsDetailed() {
        List<Consultant> consultants = consultantRepository.findAll();

        return consultants.stream().map(consultant -> {
            ConsultantResponse dto = new ConsultantResponse();

            dto.setId(consultant.getId()); // ✅ il faut renvoyer l'ID

            dto.setNomComplet(consultant.getNomComplet());
            dto.setOrganisme(consultant.getOrganisme()); // ✅ ajouté


            User user = consultant.getUser();
            if (user != null) {
                dto.setEmail(user.getEmail());
            }

            Projet projet = consultant.getProjet();
            if (projet != null) {
                dto.setNomProjet(projet.getNomProjet());
            }

            if (consultant.getDateAffectation() != null) {
                dto.setDateAffectation(
                        consultant.getDateAffectation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }

            dto.setDescriptionProjet(consultant.getDescriptionProjet());

            return dto;
        }).toList();
    }

    public List<ProjetResponse> getProjetsByConsultantEmail(String email) {
        List<Consultant> affectations = consultantRepository.findByUserEmail(email);

        return affectations.stream().map(c -> {
            ProjetResponse dto = new ProjetResponse();
            dto.setNomProjet(c.getProjet().getNomProjet());
            dto.setCodeProjet(c.getProjet().getCodeProjet());
            dto.setDescriptionProjet(c.getDescriptionProjet());
            dto.setDateAffectation(
                    c.getDateAffectation() != null
                            ? c.getDateAffectation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            : null
            );

            // ✅ Utiliser l'organisme saisi par le chef pour ce consultant
            dto.setOrganisme(c.getOrganisme());

            return dto;
        }).toList();
    }






}
