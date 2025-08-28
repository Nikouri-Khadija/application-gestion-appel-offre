import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ProjetService } from '../../services/projet.service';
import { NotificationService } from '../../services/notification.service';
import { CommonModule, DatePipe, NgClass } from '@angular/common';
import { FilterPipe } from '../../services/filter.pipe';
import { Router } from '@angular/router';

@Component({
  selector: 'app-creer-projet',
  templateUrl: './creer-projet.component.html',
  styleUrls: ['./creer-projet.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    NgClass,
    DatePipe,
    FilterPipe
  ]
})
export class CreerProjetComponent implements OnInit {
  projetForm!: FormGroup;
  formVisible = false;
  chefs: any[] = [];
  projets: any[] = [];
  message: string = '';
  searchCode: string = '';
  searchChef: string = '';
  projetEnEdition: any = null;

  constructor(
    private fb: FormBuilder,
    private projetService: ProjetService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.projetForm = this.fb.group({
      nom: ['', Validators.required],
      code: ['', Validators.required],
      chefId: ['', Validators.required],
      dateCreation: ['', Validators.required],
      dateLimite: ['', Validators.required],
      description: ['']
    });

    this.getChefs();
    this.getProjets();
  }

  showForm() {
    this.formVisible = !this.formVisible;
  }

  retour(): void {
    this.formVisible = false;
    this.router.navigate(['/login/admin']);
  }

  modifier(projet: any) {
    this.projetEnEdition = projet;
    this.formVisible = true;

    this.projetForm.patchValue({
      nom: projet.nomProjet,
      code: projet.codeProjet,
      chefId: projet.chefProjet?.id,
      dateCreation: projet.dateCreation,
      dateLimite: projet.dateLimite,
      description: projet.description
    });
  }

  submit() {
    if (this.projetForm.invalid) return;

    const formValue = this.projetForm.value;
    const projetPayload = {
      nomProjet: formValue.nom,
      codeProjet: formValue.code,
      dateCreation: formValue.dateCreation,
      dateLimite: formValue.dateLimite,
      description: formValue.description,
      chefId: formValue.chefId
    };

    if (this.projetEnEdition) {
      this.projetService.modifierProjet(this.projetEnEdition.id, projetPayload).subscribe(() => {
        this.message = 'Projet mis à jour avec succès';
        this.getProjets();
        this.projetForm.reset();
        this.formVisible = false;
        this.projetEnEdition = null;
        setTimeout(() => this.message = '', 3000);
      });
    } else {
      this.projetService.ajouterProjet(projetPayload).subscribe(() => {
        this.message = 'Projet créé avec succès';
        this.getProjets();
        this.projetForm.reset();
        this.formVisible = false;
        // Ne plus appeler refreshCompteurs ici pour ne pas toucher au compteur appel en cours ni au compteur projets inutilement.
        // La mise à jour du compteur projets se fait via abonnement dans admin.component.ts

        setTimeout(() => this.message = '', 3000);
      });
    }
  }

  getChefs() {
    this.projetService.getChefs().subscribe(data => {
      this.chefs = data;
    });
  }

  getProjets() {
    this.projetService.getProjets().subscribe(data => {
      this.projets = data;
    });
  }

  supprimerProjet(id: number) {
    this.projetService.supprimerProjet(id).subscribe(() => {
      this.message = 'Projet supprimé avec succès';
      this.getProjets();
      setTimeout(() => this.message = '', 3000);
    });
  }

  afficherDescription(projet: any) {
    projet.showDescription = !projet.showDescription;
  }
}
