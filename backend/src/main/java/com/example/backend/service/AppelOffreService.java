package com.example.backend.service;

import com.example.backend.dto.AppelOffreRequest;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppelOffreService {

    private final AppelOffreRepository repo;
    private final UserRepository userRepo;

    // Injection du chemin depuis application.properties
    @Value("${app.uploads.path}")
    private String uploadFolder;

    public AppelOffre create(AppelOffreRequest request) throws IOException {
        User chefProjet = userRepo.findById(request.getIdChefProjet())
                .orElseThrow(() -> new RuntimeException("Chef de projet non trouvé"));

        MultipartFile file = request.getFichier();

        // Chemin absolu basé sur le dossier du projet + nom du dossier uploads
        String uploadDir = System.getProperty("user.dir") + File.separator + uploadFolder;
        File uploadFolderFile = new File(uploadDir);
        if (!uploadFolderFile.exists()) {
            boolean created = uploadFolderFile.mkdirs();
            if (!created) {
                throw new IOException("Impossible de créer le dossier de stockage : " + uploadDir);
            }
        }

        // Sauvegarde du fichier dans le dossier
        String filePath = uploadDir + File.separator + file.getOriginalFilename();
        file.transferTo(new File(filePath));

        // Création de l'appel d'offre
        AppelOffre appel = new AppelOffre();
        appel.setTitre(request.getTitre());
        appel.setOrganisme(request.getOrganisme());
        appel.setDateCreation(request.getDateCreation());
        appel.setDateLimite(request.getDateLimite());

        // Chemin relatif pour affichage plus tard si besoin
        appel.setFichierPdfPath(uploadFolder + "/" + file.getOriginalFilename());
        appel.setStatut(Statut.EN_ATTENTE);
        appel.setDestinataire(chefProjet);

        return repo.save(appel);
    }

    public List<AppelOffre> getAppelsForChef(User chef) {
        return repo.findByDestinataireAndStatut(chef, Statut.EN_ATTENTE);
    }

    public AppelOffre selectionnerAppel(Long id) {
        AppelOffre appel = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Appel non trouvé"));
        appel.setStatut(Statut.ENVOYE);
        return repo.save(appel);
    }
}
