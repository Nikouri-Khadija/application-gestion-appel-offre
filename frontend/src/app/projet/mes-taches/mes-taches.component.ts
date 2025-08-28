import { Component, OnInit } from '@angular/core';
import { TacheService } from '../../services/tache.service';
import { FormsModule } from '@angular/forms';
import { CommonModule, NgClass } from '@angular/common';
import {Router} from '@angular/router';

@Component({
  selector: 'app-mes-taches',
  templateUrl: './mes-taches.component.html',
  imports: [FormsModule,  CommonModule],
  styleUrls: ['./mes-taches.component.scss']
})
export class MesTachesComponent implements OnInit {
  taches: any[] = [];
  tachesFiltres: any[] = [];
  projets: string[] = [];
  priorites: string[] = ['BASSE', 'MOYENNE', 'ELEVEE'];

  // Liste complète des statuts pour le filtre (avec tous les statuts)
  statutsFiltre: string[] = ['A_FAIRE', 'EN_COURS', 'EN_ATTENTE_VALIDATION', 'BLOQUE', 'TERMINE'];

  // Liste des statuts disponibles pour la modification (seulement ceux autorisés)
  statutsModification: string[] = ['EN_COURS', 'EN_ATTENTE_VALIDATION', 'BLOQUE'];

  selectedProjet = '';
  selectedPriorite = '';
  selectedStatut = '';

  tacheEnCours: any = null;
  nouveauStatut: string = '';
  commentaire: string = '';

  constructor(private tacheService: TacheService ,
              private router: Router) {}

  ngOnInit(): void {
    this.loadTaches();
    // Formater les noms des statuts pour l'affichage
    this.statutsFiltre = this.statutsFiltre.map(statut => this.formatStatutName(statut));
    this.statutsModification = this.statutsModification.map(statut => this.formatStatutName(statut));
  }

  loadTaches() {
    const email = localStorage.getItem('email');
    this.tacheService.getMesTaches(email!).subscribe(data => {
      this.taches = data.map(t => ({
        ...t,
        projet: t.nomProjet,
        nom: t.nomTache,
        dateCreation: this.formatDateForTable(t.dateCreation),
        dateAffectation: this.formatDateForTable(t.dateAffectation),
        dateLimite: this.formatDateForTable(t.dateLimite),
        showDetails: false
      }));
      this.projets = [...new Set(this.taches.map(t => t.projet))];
      this.tachesFiltres = [...this.taches];
    });
  }

  // ➡️ méthode de formatage réutilisable
  formatDateForTable(dateStr: string): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    return `${day}/${month}/${year}`;
  }

  // Méthode pour formater les noms de statut pour l'affichage
  formatStatutName(statut: string): string {
    const statutMap: { [key: string]: string } = {
      'A_FAIRE': 'A faire',
      'EN_COURS': 'En cours',
      'EN_ATTENTE_VALIDATION': 'En attente validation',
      'BLOQUE': 'Bloqué',
      'TERMINE': 'Terminé'
    };

    return statutMap[statut] || statut;
  }

  // Méthode pour retrouver la clé originale à partir du nom formaté
  getStatutKey(statutName: string): string {
    const reverseMap: { [key: string]: string } = {
      'A faire': 'A_FAIRE',
      'En cours': 'EN_COURS',
      'En attente validation': 'EN_ATTENTE_VALIDATION',
      'Bloqué': 'BLOQUE',
      'Terminé': 'TERMINE'
    };

    return reverseMap[statutName] || statutName;
  }

  filtrerTaches() {
    this.tachesFiltres = this.taches.filter(t =>
      (this.selectedProjet === '' || t.projet === this.selectedProjet) &&
      (this.selectedPriorite === '' || t.priorite === this.selectedPriorite) &&
      (this.selectedStatut === '' || t.statut === this.selectedStatut)
    );
  }

  afficherDetails(tache: any) {
    tache.showDetails = !tache.showDetails;
  }

  toggleModifier(tache: any) {
    if (this.tacheEnCours && this.tacheEnCours.id === tache.id) {
      this.annulerModification();
    } else {
      this.tacheEnCours = tache;
      // Formater le statut pour l'affichage dans le formulaire
      this.nouveauStatut = this.formatStatutName(tache.statut);
      this.commentaire = tache.commentaire || '';
    }
  }

  annulerModification() {
    this.tacheEnCours = null;
    this.nouveauStatut = '';
    this.commentaire = '';
  }

  validerModification() {
    // Convertir le statut formaté en clé technique
    const statutKey = this.getStatutKey(this.nouveauStatut);

    if (statutKey === 'BLOQUE' && !this.commentaire.trim()) {
      alert('Un commentaire est obligatoire pour bloquer une tâche.');
      return;
    }

    this.tacheService.updateStatut(this.tacheEnCours.id, statutKey, this.commentaire)
      .subscribe(() => {
        this.tacheEnCours.statut = statutKey;
        this.tacheEnCours.commentaire = this.commentaire;
        this.annulerModification();
      });
  }

  public retourMenu(): void {
    this.router.navigate(['/login/consultant']);
  }

  getStatusClass(statut: string): string {
    if (!statut) return '';

    const normalizedStatut = statut.toLowerCase().replace(/_/g, '');

    if (normalizedStatut.includes('attente') || normalizedStatut.includes('validation')) {
      return 'status-en_attente_validation';
    } else if (normalizedStatut.includes('faire')) {
      return 'status-a_faire';
    } else if (normalizedStatut.includes('cours')) {
      return 'status-en_cours';
    } else if (normalizedStatut.includes('bloque')) {
      return 'status-bloque';
    } else if (normalizedStatut.includes('termine')) {
      return 'status-termine';
    }

    return '';
  }

  getPriorityClass(priorite: string): string {
    if (!priorite) return '';

    const normalizedPriorite = priorite.toLowerCase();

    if (normalizedPriorite.includes('elevee') || normalizedPriorite.includes('haute')) {
      return 'priority-elevee';
    } else if (normalizedPriorite.includes('moyenne')) {
      return 'priority-moyenne';
    } else if (normalizedPriorite.includes('basse')) {
      return 'priority-basse';
    }

    return '';
  }
}
