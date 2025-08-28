import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Consultant, ConsultantService } from '../../services/consultant.service';
import { NotificationService } from '../../services/notification.service';
import { Router } from '@angular/router';
import { CommonModule, DatePipe, NgClass } from '@angular/common';
import { FilterPipe } from '../../services/filter.pipe';

type ConsultantDisplay = Omit<Consultant, 'dateAffectation'> & {
  dateAffectation: Date;
  showDescription: boolean;
  organisme?: string;
};

@Component({
  selector: 'app-affecter-consultant',
  templateUrl: './affecter-consultant.component.html',
  imports: [
    CommonModule,
    NgClass,
    FormsModule,
    ReactiveFormsModule,
    DatePipe,
    FilterPipe
  ],
  styleUrls: ['./affecter-consultant.component.scss']
})
export class AffecterConsultantComponent implements OnInit {
  formVisible = false;
  consultantForm: FormGroup;
  emails: string[] = [];
  projets: string[] = [];
  consultants: ConsultantDisplay[] = [];
  message = '';
  searchNom = '';
  searchProjet = '';
  editingId: number | null = null;



  constructor(
    private fb: FormBuilder,
    private consultantService: ConsultantService,
    private notificationService: NotificationService ,
    private router: Router
) {
    this.consultantForm = this.fb.group({
      nomComplet: ['', Validators.required],
      email: ['', Validators.required],
      nomProjet: ['', Validators.required],
      dateAffectation: ['', Validators.required],
      description: [''],
      organisme: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.consultantService.getEmailsConsultants().subscribe({
      next: (emails) => this.emails = emails,
      error: () => this.emails = []
    });

    this.consultantService.getNomProjets().subscribe({
      next: (projets) => this.projets = projets,
      error: () => this.projets = []
    });

    this.loadConsultants();
  }

  loadConsultants(): void {
    this.consultantService.getMesProjets().subscribe({
      next: (data: Consultant[]) => {
        this.consultants = data.map(c => ({
          ...c,
          dateAffectation: this.parseDateFr(c.dateAffectation as string),

          showDescription: false ,
          organisme: c.organisme
        }));
      },
      error: () => this.consultants = []
    });
  }

  parseDateFr(dateStr: string): Date {
    const [day, month, year] = dateStr.split('/');
    return new Date(+year, +month - 1, +day);
  }

  showForm(): void {
    this.formVisible = true;
    this.editingId = null;
    this.consultantForm.reset();
  }

  cancel(): void {
    this.router.navigate(['/login/chef']);
  }

  cancelForm(): void {
    this.formVisible = false;
    this.editingId = null;
    this.consultantForm.reset();
  }

  private formatDateToInputFromDate(date: Date): string {
    const yyyy = date.getFullYear();
    const mm = String(date.getMonth() + 1).padStart(2, '0');
    const dd = String(date.getDate()).padStart(2, '0');
    return `${yyyy}-${mm}-${dd}`;
  }

  private formatDateToBackend(dateStr: string): string {
    if (!dateStr) return '';
    const parts = dateStr.split('-');
    return parts.length === 3
      ? `${parts[2].padStart(2, '0')}/${parts[1].padStart(2, '0')}/${parts[0]}`
      : '';
  }

  modifier(consultant: ConsultantDisplay): void {
    this.formVisible = true;
    this.editingId = consultant.id || null;

    this.consultantForm.patchValue({
      nomComplet: consultant.nomComplet,
      email: consultant.email,
      nomProjet: consultant.nomProjet,
      dateAffectation: this.formatDateToInputFromDate(consultant.dateAffectation),
      description: consultant.descriptionProjet || '' ,
      organisme: consultant.organisme || '' // ✅ ajouté
    });
  }

  submit(): void {
    if (this.consultantForm.invalid) return;

    const formValue = this.consultantForm.value;
    const payload = {
      nomComplet: formValue.nomComplet,
      email: formValue.email,
      nomProjet: formValue.nomProjet,
      dateAffectation: this.formatDateToBackend(formValue.dateAffectation),
      descriptionProjet: formValue.description ,
      organisme: formValue.organisme // ✅ ajouté
    };

    if (this.editingId) {
      this.updateConsultant(payload);
    } else {
      this.addConsultant(payload);
    }
  }

  private addConsultant(payload: any): void {
    this.consultantService.addConsultant(payload).subscribe({
      next: (consultant) => {
        this.showSuccess(`Consultant ${consultant.nomComplet} ajouté avec succès`);

        this.notificationService.envoyerNotification({
          contenu: `Vous avez été affecté au projet ${consultant.nomProjet}`,
          destinataire: consultant.email
        }).subscribe();

        this.resetAfterAction();
      },
      error: () => this.showError('Erreur lors de l\'ajout du consultant')
    });
  }

  private updateConsultant(payload: any): void {
    if (!this.editingId) return;

    this.consultantService.updateConsultant(this.editingId, payload).subscribe({
      next: (consultant) => {
        this.showSuccess(`Consultant ${consultant.nomComplet} modifié avec succès`);

        const changes = this.getChangesNotification(payload);
        if (changes) {
          this.notificationService.envoyerNotification({
            contenu: changes,
            destinataire: consultant.email
          }).subscribe();
        }

        this.resetAfterAction();
      },
      error: () => this.showError('Erreur lors de la modification du consultant')
    });
  }

  private getChangesNotification(payload: any): string {
    const changes = [];
    const formValues = this.consultantForm.value;
    const originalConsultant = this.consultants.find(c => c.id === this.editingId);

    if (!originalConsultant) return '';

    if (formValues.nomComplet !== originalConsultant.nomComplet) {
      changes.push(`Nom complet modifié: ${formValues.nomComplet}`);
    }
    if (formValues.email !== originalConsultant.email) {
      changes.push(`Email modifié: ${formValues.email}`);
    }
    if (formValues.nomProjet !== originalConsultant.nomProjet) {
      changes.push(`Projet modifié: ${formValues.nomProjet}`);
    }
    if (formValues.description !== originalConsultant.descriptionProjet) {
      changes.push('Description du projet modifiée');
    }
    if (formValues.organisme !== originalConsultant.organisme) {
      changes.push(`Organisme modifié: ${formValues.organisme}`);
    }


    return changes.length > 0
      ? `Modifications apportées à votre affectation: ${changes.join(', ')}`
      : '';
  }

  supprimerConsultant(id?: number): void {
    if (!id) return;

    const consultant = this.consultants.find(c => c.id === id);
    if (!consultant) return;

    if (!confirm(`Confirmer la suppression de ${consultant.nomComplet} ?`)) return;

    this.consultantService.deleteConsultant(id).subscribe({
      next: () => {
        this.showSuccess('Consultant supprimé avec succès');

        this.notificationService.envoyerNotification({
          contenu: `Votre affectation au projet ${consultant.nomProjet} a été supprimée`,
          destinataire: consultant.email
        }).subscribe();

        this.loadConsultants();
      },
      error: () => this.showError('Erreur lors de la suppression')
    });
  }

  private resetAfterAction(): void {
    this.formVisible = false;
    this.editingId = null;
    this.consultantForm.reset();
    this.loadConsultants();
  }

  private showSuccess(message: string): void {
    this.message = message;
    setTimeout(() => this.message = '', 3000);
  }

  private showError(message: string): void {
    this.message = message;
    setTimeout(() => this.message = '', 3000);
  }

  toggleDescription(consultant: ConsultantDisplay): void {
    consultant.showDescription = !consultant.showDescription;
  }
}
