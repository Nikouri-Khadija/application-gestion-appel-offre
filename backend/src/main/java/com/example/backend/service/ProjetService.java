package com.example.backend.service;

import com.example.backend.dto.ProjetRequest;
import com.example.backend.entity.AppelOffre;
import com.example.backend.entity.Projet;
import com.example.backend.entity.Statut;
import com.example.backend.entity.User;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.AppelOffreRepository;
import com.example.backend.repository.ProjetRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AppelOffreRepository appelOffreRepository;
    private static final Logger logger = LoggerFactory.getLogger(ProjetService.class);

    public ProjetService(ProjetRepository projetRepository,
                         UserRepository userRepository,
                         NotificationService notificationService,
                         AppelOffreRepository appelOffreRepository) {
        this.projetRepository = projetRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.appelOffreRepository = appelOffreRepository;
    }

    @Transactional
    public Projet ajouterProjet(ProjetRequest request) {
        if (projetRepository.existsByCodeProjet(request.getCodeProjet())) {
            throw new IllegalArgumentException("Code projet déjà utilisé");
        }

        User chef = userRepository.findById(request.getChefId())
                .orElseThrow(() -> new ResourceNotFoundException("Chef introuvable"));

        Projet projet = new Projet();
        projet.setNomProjet(request.getNomProjet());
        projet.setCodeProjet(request.getCodeProjet());
        projet.setChefProjet(chef);
        projet.setDateCreation(request.getDateCreation());
        projet.setDateLimite(request.getDateLimite());
        projet.setDescription(request.getDescription());

        Projet savedProjet = projetRepository.save(projet);

        // Envoi notification à la création
        String contenu = "Vous avez été affecté au projet : " + savedProjet.getNomProjet();
        notificationService.envoyer(contenu, chef.getEmail());

        // Mise à jour automatique des compteurs (appels en cours / projets validés)
        List<AppelOffre> appelsValides = appelOffreRepository.findByStatut(Statut.VALIDE);
        int nbProjets = appelsValides.size();
        long nbEnCours = appelOffreRepository.countByStatut(Statut.EN_ATTENTE);

        logger.info("Compteurs mis à jour - En cours : {} / Projets : {}", nbEnCours, nbProjets);

        return savedProjet;
    }

    public List<Projet> getAllProjets() {
        return projetRepository.findAll();
    }

    public void supprimerProjet(Long id) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé avec l'id : " + id));
        projetRepository.delete(projet);
    }

    public Map<String, Integer> getCompteurs() {
        long nbEnCoursLong = appelOffreRepository.countByStatut(Statut.EN_ATTENTE);
        long nbProjetsLong = projetRepository.count();

        Map<String, Integer> result = new HashMap<>();
        result.put("enCours", (int) nbEnCoursLong);
        result.put("projets", (int) nbProjetsLong);
        return result;
    }

    public Projet modifierProjet(Long id, ProjetRequest request) {
        Projet projet = projetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projet non trouvé"));

        User chef = userRepository.findById(request.getChefId())
                .orElseThrow(() -> new ResourceNotFoundException("Chef non trouvé"));

        projet.setNomProjet(request.getNomProjet());
        projet.setCodeProjet(request.getCodeProjet());
        projet.setChefProjet(chef);
        projet.setDateCreation(request.getDateCreation());
        projet.setDateLimite(request.getDateLimite());
        projet.setDescription(request.getDescription());

        Projet updatedProjet = projetRepository.save(projet);

        // Envoi notification à la modification
        String contenu = "Le projet '" + updatedProjet.getNomProjet() + "' a été modifié.";
        notificationService.envoyer(contenu, chef.getEmail());

        return updatedProjet;
    }

    public List<Projet> getProjetsParChef(Long chefId) {
        return projetRepository.findByChefProjetId(chefId);
    }
}

