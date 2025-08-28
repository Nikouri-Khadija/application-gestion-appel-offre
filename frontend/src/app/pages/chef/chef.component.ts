// src/app/components/chef/chef.component.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationService } from '../../services/notification.service';
import { Notification as NotifModel } from '../../models/notification.model';
import { CommonModule } from '@angular/common';
import { NotificationBellComponent } from '../../notification-bell/notification-bell.component';
import { ProjetService } from '../../services/projet.service';
import { TacheService } from '../../services/tache.service'; // ✅ importe le service des tâches
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-chef',
  templateUrl: './chef.component.html',
  styleUrls: ['./chef.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule, NotificationBellComponent]
})
export class ChefComponent implements OnInit {
  sidebarOpen: boolean = false;
  notifications: NotifModel[] = [];
  clocheActive: boolean = false;
  popupVisible: boolean = false;

  // Projets affectés
  projetsAffectes: any[] = [];
  selectedProjet: string = '';

  // Statistiques
  statistiques: any = null;

  constructor(
    private router: Router,
    private notificationService: NotificationService,
    private projetService: ProjetService,
    private tacheService: TacheService
  ) {}

  ngOnInit(): void {
    this.chargerNotifications();
    this.getMesProjets();
    setInterval(() => this.chargerNotifications(), 5000);
  }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('email');
    this.router.navigate(['/']);
  }

  goToSelectionAppels(): void {
    this.router.navigate(['/chef/selection-appels']);
  }

  toggleNotif(): void {
    this.popupVisible = !this.popupVisible;
  }

  chargerNotifications(): void {
    this.notificationService.getNotifications('chef').subscribe(data => {
      this.notifications = data;
      this.clocheActive = this.notifications.some(n => !n.lu);
    });
  }

  lireNotification(id: number): void {
    this.notificationService.marquerCommeLue(id).subscribe(() => {
      this.chargerNotifications();
    });
  }

  getMesProjets(): void {
    const emailChef = this.getChefEmailConnecte();
    this.projetService.getProjets().subscribe(projets => {
      this.projetsAffectes = projets.filter(p => p.chefProjet?.email === emailChef);
    });
  }

  getChefEmailConnecte(): string {
    return localStorage.getItem('email') || '';
  }

  showMesProjets(): void {
    this.router.navigate(['/chef/Mes-projetsChef']);
  }

  showConsultants() {
    this.router.navigate(['/chef/Mes-projetsConsultants']);
  }

  showTaches() {
    this.router.navigate(['/chef/gestion-taches']);
  }

  // ✅ Charge les statistiques pour le projet sélectionné
  chargerStatistiques(): void {
    if (!this.selectedProjet) {
      this.statistiques = null;
      return;
    }
    this.tacheService.getStatistiques(this.selectedProjet).subscribe(res => {
      this.statistiques = res;
    });
  }

  suivi() {
    this.router.navigate(['/chef/Suivi-Projets']);
  }
}
