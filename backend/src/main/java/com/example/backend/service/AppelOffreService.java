package com.example.backend.service;

import com.example.backend.dto.AppelOffreRequest;
import com.example.backend.entity.AppelOffre;
import com.example.backend.entity.Role;
import com.example.backend.entity.Statut;
import com.example.backend.entity.User;
import com.example.backend.exception.InvalidAmountException;
import com.example.backend.exception.InvalidDateException;
import com.example.backend.exception.AppelOffreNotFoundException;
import com.example.backend.repository.AppelOffreRepository;
import com.example.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppelOffreService {

    private final AppelOffreRepository repo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;

    @Value("${app.uploads.path}")
    public String uploadFolder;

    public AppelOffre create(AppelOffreRequest request) throws IOException {

        String uploadDir = System.getProperty("user.dir") + File.separator + uploadFolder;
        File uploadFolderFile = new File(uploadDir);
        if (!uploadFolderFile.exists()) uploadFolderFile.mkdirs();

        String fichier1 = enregistrerFichier(request.getFichier1(), uploadDir);
        String fichier2 = enregistrerFichier(request.getFichier2(), uploadDir);
        String fichier3 = enregistrerFichier(request.getFichier3(), uploadDir);
        String fichier4 = enregistrerFichier(request.getFichier4(), uploadDir);

        List<User> chefs = userRepo.findByRole(Role.CHEF_DE_PROJET);

        AppelOffre appel = new AppelOffre();
        appel.setTitre(request.getTitre());
        appel.setOrganisme(request.getOrganisme());

        try {
            appel.setDateCreation(LocalDate.parse(request.getDateCreation()));
            appel.setDateLimite(LocalDate.parse(request.getDateLimite()));
        } catch (Exception e) {
            throw new InvalidDateException("Format de date invalide. Utiliser 'yyyy-MM-dd'.");
        }

        try {
            appel.setEstimation(nettoyerEtConvertirMontant(request.getEstimation()));
            appel.setCautionProvisoire(nettoyerEtConvertirMontant(request.getCautionProvisoire()));
        } catch (NumberFormatException e) {
            throw new InvalidAmountException("Valeur numérique invalide pour estimation ou caution.");
        }

        appel.setFichier1(fichier1);
        appel.setFichier2(fichier2);
        appel.setFichier3(fichier3);
        appel.setFichier4(fichier4);

        appel.setStatut(Statut.EN_ATTENTE);
        appel.setDestinataires(chefs);
        appel.setEnvoyeParAdmin(false);
        appel.setSelectionneParChef(false);
        appel.setNomChefSelectionneur(null);

        AppelOffre saved = repo.save(appel);

        String contenu = "Un nouvel appel d’offres a été créé : " + saved.getTitre();
        notificationService.envoyer(contenu, "chef");

        return saved;
    }

    private String enregistrerFichier(MultipartFile file, String uploadDir) throws IOException {
        if (file == null || file.isEmpty()) return null;
        String filePath = uploadDir + File.separator + file.getOriginalFilename();
        file.transferTo(new File(filePath));
        return file.getOriginalFilename();
    }

    private double nettoyerEtConvertirMontant(String montant) {
        if (montant == null || montant.trim().isEmpty()) {
            throw new NumberFormatException("Champ vide");
        }

        montant = montant
                .replace(" ", "")
                .replace(",", ".")
                .replaceAll("[^\\d.]", "");

        return Double.parseDouble(montant);
    }

    public List<AppelOffre> getAppelsForChef(User chef) {
        return repo.findByDestinatairesContainingAndStatut(chef, Statut.EN_ATTENTE);
    }

    public AppelOffre selectionnerAppel(Long id) {
        AppelOffre appel = repo.findById(id)
                .orElseThrow(() -> new AppelOffreNotFoundException("Appel non trouvé"));

        appel.setStatut(Statut.ENVOYE);
        return repo.save(appel);
    }

    public List<AppelOffre> getAllAppels() {
        return repo.findAll();
    }

    public AppelOffre validerAppel(Long id) {
        AppelOffre appel = repo.findById(id)
                .orElseThrow(() -> new AppelOffreNotFoundException("Appel introuvable"));

        appel.setStatut(Statut.VALIDE);
        return repo.save(appel);
    }

    public AppelOffre refuserAppel(Long id) {
        AppelOffre appel = repo.findById(id)
                .orElseThrow(() -> new AppelOffreNotFoundException("Appel non trouvé"));

        appel.setStatut(Statut.REJETE);
        return repo.save(appel);
    }
}
