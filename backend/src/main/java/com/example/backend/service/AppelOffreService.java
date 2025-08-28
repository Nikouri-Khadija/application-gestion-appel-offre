package com.example.backend.service;

import com.example.backend.dto.AppelOffreRequest;
import com.example.backend.entity.AppelOffre;
import com.example.backend.entity.Role;
import com.example.backend.entity.Statut;
import com.example.backend.entity.User;
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
    private String uploadFolder;

    public AppelOffre create(AppelOffreRequest request) throws IOException {
        // 📁 Créer le dossier s’il n’existe pas
        String uploadDir = System.getProperty("user.dir") + File.separator + uploadFolder;
        File uploadFolderFile = new File(uploadDir);
        if (!uploadFolderFile.exists()) uploadFolderFile.mkdirs();

        // 📥 Enregistrer les fichiers
        String fichier1 = enregistrerFichier(request.getFichier1(), uploadDir);
        String fichier2 = enregistrerFichier(request.getFichier2(), uploadDir);
        String fichier3 = enregistrerFichier(request.getFichier3(), uploadDir);
        String fichier4 = enregistrerFichier(request.getFichier4(), uploadDir);

        // 📬 Récupérer les chefs de projet
        List<User> chefs = userRepo.findByRole(Role.CHEF_DE_PROJET);

        // 🧾 Créer l’appel
        AppelOffre appel = new AppelOffre();
        appel.setTitre(request.getTitre());
        appel.setOrganisme(request.getOrganisme());

        // ✅ Conversion des dates
        try {
            appel.setDateCreation(LocalDate.parse(request.getDateCreation()));
            appel.setDateLimite(LocalDate.parse(request.getDateLimite()));
        } catch (Exception e) {
            throw new RuntimeException("Format de date invalide. Utiliser le format 'yyyy-MM-dd'.", e);
        }

        // ✅ Conversion des montants
        try {
            appel.setEstimation(nettoyerEtConvertirMontant(request.getEstimation()));
            appel.setCautionProvisoire(nettoyerEtConvertirMontant(request.getCautionProvisoire()));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Valeur numérique invalide pour estimation ou caution.", e);
        }

        // ✅ Fichiers
        appel.setFichier1(fichier1);
        appel.setFichier2(fichier2);
        appel.setFichier3(fichier3);
        appel.setFichier4(fichier4);

        // 📌 Statut et métadonnées
        appel.setStatut(Statut.EN_ATTENTE);
        appel.setDestinataires(chefs);
        appel.setEnvoyeParAdmin(false);
        appel.setSelectionneParChef(false);
        appel.setNomChefSelectionneur(null);

        // 💾 Sauvegarde en base
        AppelOffre saved = repo.save(appel);

        // 📢 Envoi d'une seule notification aux chefs
        String contenu = "Un nouvel appel d’offres a été créé : " + saved.getTitre();
        notificationService.envoyer(contenu, "chef");

        return saved;
    }


    // ✅ Méthode réutilisable pour enregistrer un fichier
    private String enregistrerFichier(MultipartFile file, String uploadDir) throws IOException {
        if (file == null || file.isEmpty()) return null;
        String filePath = uploadDir + File.separator + file.getOriginalFilename();
        file.transferTo(new File(filePath));
        return file.getOriginalFilename();
    }

    // ✅ Nettoyage et conversion des montants
    private double nettoyerEtConvertirMontant(String montant) {
        if (montant == null || montant.trim().isEmpty()) throw new NumberFormatException("Champ vide");
        montant = montant
                .replace(" ", "")       // Supprimer les espaces
                .replace(",", ".")      // Remplacer virgule par point (ex: 1 234,56 → 1234.56)
                .replaceAll("[^\\d.]", ""); // Supprimer tout sauf chiffres et point
        return Double.parseDouble(montant);
    }

    // ✅ Appels visibles pour un chef
    public List<AppelOffre> getAppelsForChef(User chef) {
        return repo.findByDestinatairesContainingAndStatut(chef, Statut.EN_ATTENTE);
    }

    // ✅ Sélection par chef
    public AppelOffre selectionnerAppel(Long id) {
        AppelOffre appel = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Appel non trouvé"));
        appel.setStatut(Statut.ENVOYE);
        return repo.save(appel);
    }

    // ✅ Tous les appels
    public List<AppelOffre> getAllAppels() {
        return repo.findAll();
    }

    // ✅ Validation d’un appel
    public AppelOffre validerAppel(Long id) {
        AppelOffre appel = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Appel introuvable"));
        appel.setStatut(Statut.VALIDE);
        System.out.println(">>> Appel validé avec ID " + id);
        return repo.save(appel);
    }

    // ✅ Rejet d’un appel
    public AppelOffre refuserAppel(Long id) {
        AppelOffre appel = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Appel non trouvé"));
        appel.setStatut(Statut.REJETE);
        System.out.println(">>> Appel refusé avec ID " + id);
        return repo.save(appel);
    }
}
