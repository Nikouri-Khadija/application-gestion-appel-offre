import { Component, OnInit } from '@angular/core';
import { ProjetService } from '../../services/projet.service';
import { AuthService } from '../../services/auth.service';
import { Router } from '@angular/router';
import {FormsModule} from '@angular/forms';
import {CommonModule, DatePipe, NgClass} from '@angular/common';
import {FilterPipe} from '../../services/filter.pipe';

@Component({
  selector: 'app-mes-projets-chef',
  templateUrl: './mes-projets-chef.component.html',
  imports: [
    CommonModule,
    FormsModule,
    DatePipe,
    NgClass,
    FilterPipe
  ],
  styleUrls: ['./mes-projets-chef.component.scss']
})
export class MesProjetsChefComponent implements OnInit {
  nomChef: string = '';
  projets: any[] = [];
  searchCode: string = '';

  constructor(
    private authService: AuthService,
    private projetService: ProjetService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.nomChef = localStorage.getItem('email') || ''; // récupérer email pour afficher dans UI (ou pour requête backend)
    this.loadChefIdAndProjets();
  }

  loadChefIdAndProjets(): void {
    if (!this.nomChef) return;

    // Appeler backend pour récupérer l'utilisateur complet avec id, nom complet...
    this.projetService.getUserByEmail(this.nomChef).subscribe(user => {
      if (user && user.id) {
        this.loadMesProjets(user.id);
      }
    });
  }

  loadMesProjets(chefId: number): void {
    this.projetService.getProjetsByChef(chefId).subscribe(data => {
      this.projets = data;
    });
  }

  afficherDescription(projet: any) {
    projet.showDescription = !projet.showDescription;
  }



  retourMenu(): void {
    this.router.navigate(['/login/chef']); // Navigation propre
  }
}
