import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';

import {CommonModule, NgClass} from '@angular/common';
import {TacheService} from '../../services/tache.service';
import {Router} from '@angular/router';
import {FilterPipe} from '../../services/filter.pipe';

@Component({
  selector: 'app-creer-tache',
  templateUrl: './creer-tache.component.html',
  imports: [
    ReactiveFormsModule,
    FormsModule,
    NgClass,
    CommonModule,
    FilterPipe
  ],
  styleUrls: ['./creer-tache.component.scss']
})
export class CreerTacheComponent implements OnInit {
  tacheForm!: FormGroup;
  formVisible = false;
  message: string | null = null;
  consultants: any[] = [];
  projets: any[] = [];
  taches: any[] = [];
  editingTacheId: number | null = null;

  searchNomTache = '';
  searchProjet = '';

  constructor(private fb: FormBuilder, private tacheService: TacheService , private router: Router) {}

  ngOnInit(): void {
    this.loadConsultants();
    this.loadTaches();
    this.tacheForm = this.fb.group({
      nomTache: ['', Validators.required],
      consultantId: ['', Validators.required],
      projetId: ['', Validators.required],
      priorite: ['', Validators.required],
      dateAffectation: ['', Validators.required],
      dateLimite: ['', Validators.required],
      description: [''] ,
      commentaire: ['']
    });
  }

  formatDateForInput(dateStr: string | Date): string {
    if (!dateStr) return '';

    // Si déjà au format yyyy-MM-dd, retourne tel quel
    if (typeof dateStr === 'string' && dateStr.includes('-')) {
      return dateStr;
    }

    // Sinon, transforme en yyyy-MM-dd
    const d = new Date(dateStr);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }



  formatDateForTable(dateStr: string): string {
    // transforme 'yyyy-MM-dd' ou ISO en 'dd/MM/yyyy' pour affichage tableau
    if (!dateStr) return '';
    const d = new Date(dateStr);
    const day = String(d.getDate()).padStart(2,'0');
    const month = String(d.getMonth()+1).padStart(2,'0');
    const year = d.getFullYear();
    return `${day}/${month}/${year}`;
  }

  showForm() {
    this.formVisible = true;
  }

  cancelForm() {
    this.formVisible = false;
    this.editingTacheId = null;
    this.tacheForm.reset();
  }

  goBack() {
    this.router.navigate(['/login/chef']);
  }

  loadConsultants() {
    this.tacheService.getConsultants().subscribe(data => {
      this.consultants = data.map((c: any) => ({
        id: c.id,                       // ✅ vrai ID venant du backend
        nomComplet: c.nomComplet.trim() // nom complet propre
      }));
    });
  }

  onConsultantChange(event: any) {
    const consultantId = event.target.value;
    this.tacheService.getProjetsByConsultant(consultantId).subscribe(data => {
      this.projets = data.map((p: any) => ({
        id: p.id,                          // ✅ vrai ID projet venant du backend
        nomProjet: p.nomProjet.trim()      // nom projet propre
      }));
    });
  }

  loadTaches() {
    this.tacheService.getTaches().subscribe(data => {
      this.taches = data.map((t: any) => ({
        ...t,
        dateCreation: t.dateCreation.split(' ')[0],
        dateAffectationRaw: t.dateAffectation, // ✅ pour backend
        dateLimiteRaw: t.dateLimite,
        dateAffectation: this.formatDateForTable(t.dateAffectation), // ✅ pour affichage
        dateLimite: this.formatDateForTable(t.dateLimite)
      }));


    });
  }

  submit() {
    if (this.tacheForm.invalid) return;

    // Copie de la valeur du formulaire
    const formValue = { ...this.tacheForm.value };

    // ✅ Conversion des dates en ISO si elles sont au format dd/MM/yyyy
    formValue.dateAffectation = this.formatDateForInput(this.tacheForm.value.dateAffectation);
    formValue.dateLimite = this.formatDateForInput(this.tacheForm.value.dateLimite);

    if (this.editingTacheId) {
      // Mise à jour
      this.tacheService.updateDetails(this.editingTacheId, formValue).subscribe(() => {
        this.message = 'Tâche modifiée avec succès !';
        this.loadTaches();
        this.cancelForm();
      }, error => {
        alert("Erreur lors de la mise à jour : " + error.error.message);
      });
    } else {
      // Création
      this.tacheService.createTache(formValue).subscribe(() => {
        this.message = 'Tâche ajoutée avec succès !';
        this.loadTaches();
        this.cancelForm();
      });
    }

    // Faire disparaître le message après 3 secondes
    setTimeout(() => {
      this.message = null;
    }, 3000);
  }

  modifier(tache: any) {
    this.formVisible = true;
    this.editingTacheId = tache.id;

    this.onConsultantChange({target: {value: tache.consultantId}});
    setTimeout(() => { // attendre que les projets soient chargés
      this.tacheForm.patchValue({
        projetId: tache.projetId
      });
    }, 100);

    this.tacheForm.patchValue({
      nomTache: tache.nomTache,
      consultantId: tache.consultantId,
      projetId: tache.projetId,
      priorite: tache.priorite,
      dateAffectation: this.formatDateForInput(tache.dateAffectationRaw),
      dateLimite: this.formatDateForInput(tache.dateLimiteRaw),

      description: tache.description,
      commentaire: tache.commentaire
    });
  }

  supprimerTache(id: number) {
    if (confirm('Voulez-vous vraiment supprimer cette tâche ?')) {
      this.tacheService.deleteTache(id).subscribe(() => {
        this.loadTaches();
      });
    }
  }

  toggleDetails(tache: any) {
    tache.showDetails = !tache.showDetails;
  }

  // ✅ Méthodes ajoutées pour gérer les classes CSS des badges
  getStatusClass(statut: string): string {
    if (!statut) return '';

    // Normaliser le statut pour la comparaison
    const normalizedStatut = statut.toLowerCase().replace(/_/g, '');

    if (normalizedStatut.includes('attente') || normalizedStatut.includes('validation')) {
      return 'status-en_attente_validation';
    } else if (normalizedStatut.includes('faire')) {
      return 'status-a_faire';
    } else if (normalizedStatut.includes('cours')) {
      return 'status-en_cours';
    } else if (normalizedStatut.includes('termine')) {
      return 'status-termine';
    } else if (normalizedStatut.includes('annule')) {
      return 'status-annule';
    }

    return '';
  }

  getPriorityClass(priorite: string): string {
    if (!priorite) return '';

    // Normaliser la priorité pour la comparaison
    const normalizedPriorite = priorite.toLowerCase();

    if (normalizedPriorite.includes('elevee') || normalizedPriorite.includes('haute')) {
      return 'priority-elevee';
    } else if (normalizedPriorite.includes('moyenne') || normalizedPriorite.includes('moyenne')) {
      return 'priority-moyenne';
    } else if (normalizedPriorite.includes('basse') || normalizedPriorite.includes('basse')) {
      return 'priority-basse';
    }

    return '';
  }

  marquerCommeTerminee(tache: any) {
    if (confirm("Voulez-vous vraiment marquer cette tâche comme terminée ?")) {
      this.tacheService.updateStatut(tache.id, 'TERMINE').subscribe(() => {
        this.message = 'Tâche marquée comme terminée !';
        this.loadTaches();

        // ✅ Disparition du message après 3 secondes
        setTimeout(() => {
          this.message = null;
        }, 3000);
      }, error => {
        alert("Erreur : " + error.error.message);
      });
    }
  }



}
