import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule, NgClass } from '@angular/common';
import { Router } from '@angular/router';

import { AppelOffreService } from '../../services/appel-offre.service';
import { NotificationService } from '../../services/notification.service';
import { FilterAppelPipe } from '../../services/filterAppel.pipe';

@Component({
  selector: 'app-admin-gestion',
  templateUrl: './admin-gestion.component.html',
  styleUrls: ['./admin-gestion.component.scss'],
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    CommonModule,
    NgClass,
    FilterAppelPipe
  ]
})
export class AdminGestionComponent implements OnInit {
  appels: any[] = [];
  formVisible = false;
  appelForm!: FormGroup;
  formFiles: { [key: string]: File } = {};
  message = '';

  searchTitre = '';
  searchStatut = '';

  constructor(
    private appelService: AppelOffreService,
    private notificationService: NotificationService,
    private fb: FormBuilder,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initForm();
    this.loadAppels();
  }
  toggleDetails(appel: any): void {
    appel.showDetails = !appel.showDetails;
  }


  initForm(): void {
    this.appelForm = this.fb.group({
      titre: ['', Validators.required],
      organisme: ['', Validators.required],
      dateCreation: ['', Validators.required],
      dateLimite: ['', Validators.required],
      estimation: ['', Validators.required],
      cautionProvisoire: ['', Validators.required],
      fichier1: [null],
      fichier2: [null],
      fichier3: [null],
      fichier4: [null]
    });
  }

  onFileChange(event: any, fileKey: string): void {
    const file = event.target.files[0];
    if (file) {
      this.formFiles[fileKey] = file;
      this.appelForm.patchValue({ [fileKey]: file });
    }
  }

  submit(): void {
    const formData = new FormData();

    Object.keys(this.appelForm.controls).forEach(key => {
      const controlValue = this.appelForm.get(key)?.value;
      if (this.formFiles[key]) {
        formData.append(key, this.formFiles[key]);
      } else if (controlValue !== null && controlValue !== undefined) {
        formData.append(key, controlValue);
      }
    });

    this.appelService.createAppel(formData).subscribe(() => {
      this.message = '✅ Appel d\'offre créé avec succès';
      setTimeout(() => (this.message = ''), 3500);

      this.notificationService.envoyerNotification({
        contenu: `Admin a créé un appel : ${this.appelForm.value.titre}`,
        destinataire: 'chef'
      }).subscribe();

      this.appelForm.reset();
      this.formVisible = false;
      this.loadAppels();
    });
  }

  loadAppels(): void {
    this.appelService.getAllAppels().subscribe(data => {
      this.appels = data;
    });
  }

  showForm(): void {
    this.formVisible = !this.formVisible;
    this.appelForm.reset();
  }

  retour(): void {
    this.formVisible = false;
    this.router.navigate(['/login/admin']);
  }

  valider(id: number): void {
    this.appelService.validerAppel(id).subscribe(() => {
      this.message = '✅ Appel validé';
      setTimeout(() => (this.message = ''), 3500);
      this.loadAppels();
    });
  }

  refuser(id: number): void {
    this.appelService.refuserAppel(id).subscribe(() => {
      this.message = '❌ Appel rejeté';
      setTimeout(() => (this.message = ''), 3500);
      this.loadAppels();
    });
  }

  protected readonly encodeURIComponent = encodeURIComponent;
}

