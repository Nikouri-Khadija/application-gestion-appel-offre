// src/app/components/admin/admin.component.ts
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationService } from '../../services/notification.service';
import { Notification as NotifModel } from '../../models/notification.model';
import { AppelOffreService } from '../../services/appel-offre.service';
import { CommonModule } from '@angular/common';
import { NotificationBellComponent } from '../../notification-bell/notification-bell.component';
import { ProjetService } from '../../services/projet.service';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss'],
  standalone: true,
  imports: [CommonModule, NotificationBellComponent]
})
export class AdminComponent implements OnInit {
  popupVisible = false;
  sidebarOpen = false;
  notifications: NotifModel[] = [];
  clocheActive = false;

  compteurEnCours = 0;
  compteurProjets = 0;

  constructor(
    protected router: Router,
    private notificationService: NotificationService,
    private appelService: AppelOffreService,
    private projetService: ProjetService
  ) {}

  ngOnInit(): void {
    // Charger initialement les compteurs
    this.chargerCompteurs();

    // Mise à jour en temps réel du compteur appel en cours (ne pas modifier)
    this.projetService.compteurEnCours$.subscribe(val => this.compteurEnCours = val);

    // Mise à jour en temps réel du compteur projets (ne pas modifier)
    this.projetService.compteurProjets$.subscribe(val => this.compteurProjets = val);

    // S’abonner uniquement à la création d’un projet pour mettre à jour le compteur projets
    this.projetService.projetCree$.subscribe(() => {
      this.projetService.getCompteurs().subscribe(data => {
        this.compteurProjets = data.projets; // mise à jour SEULEMENT du compteur projets
        // NE PAS toucher au compteur appel en cours ici
      });
    });

    this.chargerNotifications();
  }

  chargerCompteurs(): void {
    this.projetService.getCompteurs().subscribe(data => {
      this.compteurEnCours = data.enCours;
      this.compteurProjets = data.projets;
    });
  }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    this.router.navigate(['/']);
  }

  goToGestionAppels(): void {
    this.router.navigate(['/admin/gestion-appels']);
  }

  chargerNotifications(): void {
    this.notificationService.getNotifications('admin').subscribe(data => {
      this.notifications = data;
      this.clocheActive = this.notifications.some(n => !n.lu);
    });
  }

  lireNotification(id: number): void {
    this.notificationService.marquerCommeLue(id).subscribe(() => {
      this.chargerNotifications();
    });
  }

  goToCreationProjetcts(): void {
    this.router.navigate(['/admin/creation-projects']);
  }
}
