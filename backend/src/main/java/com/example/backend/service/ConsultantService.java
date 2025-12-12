package com.example.backend.service;

import com.example.backend.dto.ConsultantRequest;
import com.example.backend.dto.ConsultantResponse;
import com.example.backend.dto.ProjetResponse;
import com.example.backend.entity.*;
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

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

        consultant.setDateAffectation(LocalDate.parse(request.getDateAffectation(), DATE_FORMATTER));

        Consultant saved = consultantRepository.save(consultant);

        notificationService.envoyer("Vous avez été affecté au projet : " + projet.getNomProjet(), user.getEmail());

        return saved;
    }

    public Consultant updateConsultant(Long consultantId, ConsultantRequest request) {
        Consultant consultant = consultantRepository.findById(consultantId)
                .orElseThrow(() -> new RuntimeException("Consultant non trouvé avec l'id : " + consultantId));

        StringBuilder changements = new StringBuilder();

        if (!consultant.getNomComplet().equals(request.getNomComplet())) {
            changements.append("Votre nom complet a été mis à jour. ");
            consultant.setNomComplet(request.getNomComplet());
        }

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

        if (!consultant.getProjet().getNomProjet().equals(request.getNomProjet())) {
            Projet projet = projetRepository.findByNomProjet(request.getNomProjet())
                    .orElseThrow(() -> new RuntimeException("Projet non trouvé : " + request.getNomProjet()));
            changements.append("Votre projet a été changé vers : ").append(projet.getNomProjet()).append(". ");
            consultant.setProjet(projet);
        }

        if (request.getDescriptionProjet() != null &&
                !request.getDescriptionProjet().equals(consultant.getDescriptionProjet())) {
            changements.append("La description du projet a été mise à jour. ");
            consultant.setDescriptionProjet(request.getDescriptionProjet());
        }

        LocalDate newDate = LocalDate.parse(request.getDateAffectation(), DATE_FORMATTER);
        if (!consultant.getDateAffectation().equals(newDate)) {
            changements.append("La date d'affectation a été mise à jour. ");
            consultant.setDateAffectation(newDate);
        }

        Consultant updated = consultantRepository.save(consultant);

        if (changements.isEmpty()) {
            notificationService.envoyer(changements.toString(), consultant.getUser().getEmail());
        }

        return updated;
    }

    // Dans les autres méthodes, remplacer toutes les occurrences de
    // DateTimeFormatter.ofPattern("dd/MM/yyyy") par DATE_FORMATTER
    public List<ConsultantResponse> getAllConsultantsDetailed() {
        return consultantRepository.findAll().stream().map(consultant -> {
            ConsultantResponse dto = new ConsultantResponse();
            dto.setId(consultant.getId());
            dto.setNomComplet(consultant.getNomComplet());
            dto.setOrganisme(consultant.getOrganisme());

            User user = consultant.getUser();
            if (user != null) dto.setEmail(user.getEmail());

            Projet projet = consultant.getProjet();
            if (projet != null) dto.setNomProjet(projet.getNomProjet());

            if (consultant.getDateAffectation() != null) {
                dto.setDateAffectation(consultant.getDateAffectation().format(DATE_FORMATTER));
            }

            dto.setDescriptionProjet(consultant.getDescriptionProjet());

            return dto;
        }).toList();
    }

    public List<ProjetResponse> getProjetsByConsultantEmail(String email) {
        return consultantRepository.findByUserEmail(email)
                .stream()
                .map(c -> {
                    ProjetResponse dto = new ProjetResponse();
                    dto.setNomProjet(c.getProjet().getNomProjet());
                    dto.setCodeProjet(c.getProjet().getCodeProjet());
                    dto.setDescriptionProjet(c.getDescriptionProjet());
                    dto.setDateAffectation(c.getDateAffectation() != null
                            ? c.getDateAffectation().format(DATE_FORMATTER)
                            : null);
                    dto.setOrganisme(c.getOrganisme());
                    return dto;
                }).toList();
    }
}
