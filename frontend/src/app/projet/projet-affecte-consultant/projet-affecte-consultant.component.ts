import { Component, OnInit } from '@angular/core';
import { ConsultantService, Consultant } from '../../services/consultant.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import { CommonModule, NgClass, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FilterPipe } from '../../services/filter.pipe';

@Component({
  selector: 'app-projet-affecte-consultant',
  templateUrl: './projet-affecte-consultant.component.html',
  styleUrls: ['./projet-affecte-consultant.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    NgClass,
    FormsModule,
    DatePipe,
    FilterPipe
  ]
})
export class ProjetAffecteConsultantComponent implements OnInit {
  public projetsAffectes: (Consultant & { showDescription?: boolean; dateAffectation: Date; organisme?: string;})[] = [];
  public searchProjet: string = '';
  public emailConsultantConnecte: string = '';
  public role: string = '';
  public loading = false;
  public errorMessage = '';

  constructor(
    private consultantService: ConsultantService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.emailConsultantConnecte = localStorage.getItem('email') || '';
    this.role = localStorage.getItem('role') || '';

    if (this.emailConsultantConnecte) {
      this.loadProjetsAffectes();
    } else {
      this.projetsAffectes = [];
    }
  }

  public loadProjetsAffectes(): void {
    this.loading = true;
    this.errorMessage = '';

    let request$;
    if (this.role === 'CONSULTANT') {
      request$ = this.consultantService.getMesProjets();
    } else if (this.role === 'CHEF_PROJET') {
      request$ = this.consultantService.getAllConsultantsDetailed();
    } else {
      this.errorMessage = 'Rôle non reconnu.';
      this.loading = false;
      return;
    }

    request$.subscribe({
      next: (data: Consultant[]) => {
        const filtered = this.role === 'CHEF_PROJET'
          ? data.filter(projet => projet.email === this.emailConsultantConnecte)
          : data;

        this.projetsAffectes = filtered.map(projet => ({
          ...projet,
          dateAffectation: this.parseDateFr(projet.dateAffectation as string),
          showDescription: false ,
          organisme: projet.organisme || ''
        }));

        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur de chargement des projets :', err);
        this.errorMessage = `Impossible de charger les projets (${err.status}).`;
        this.projetsAffectes = [];
        this.loading = false;
      }
    });
  }

  private parseDateFr(dateStr: string): Date {
    const [day, month, year] = dateStr.split('/');
    return new Date(+year, +month - 1, +day);
  }

  public toggleDescription(projet: Consultant & { showDescription?: boolean }): void {
    projet.showDescription = !projet.showDescription;
  }

  public retourMenu(): void {
    this.router.navigate(['/login/consultant']);
  }
}
