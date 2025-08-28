import { Component, OnInit } from '@angular/core';
import { AppelOffreService } from '../../services/appel-offre.service';
import { NotificationService } from '../../services/notification.service';
import { Router } from '@angular/router';
import { CommonModule, NgClass, NgFor, NgIf } from '@angular/common';
import { FilterAppelPipe } from '../../services/filterAppel.pipe';

@Component({
  selector: 'app-chef-selection',
  templateUrl: './chef-selection.component.html',
  styleUrls: ['./chef-selection.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    NgIf,
    NgFor,
    NgClass,

  ]
})
export class ChefSelectionComponent implements OnInit {
  appels: any[] = [];
  message = '';
  notifications: any[] = [];
  popupVisible = false;
  clocheActive = false;
  protected readonly encodeURIComponent = encodeURIComponent;

  constructor(
    private appelService: AppelOffreService,
    private notificationService: NotificationService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadAppels();
    this.getNotifications();
  }

  toggleDetails(index: number): void {
    this.appels[index].showDetails = !this.appels[index].showDetails;
  }

  loadAppels(): void {
    this.appelService.getAppelsPourChef().subscribe({
      next: (data) => {
        this.appels = data.map(appel => ({
          ...appel,
          showDetails: false // Initialise la propriété
        }));
      },
      error: (err) => {
        console.error("Erreur de chargement des appels pour chef", err);
      }
    });
  }

  selectionnerAppel(id: number, titre: string): void {
    this.appelService.selectionnerAppel(id).subscribe(() => {
      this.message = '✅ Appel sélectionné avec succès';

      const chefEmail = localStorage.getItem('email') || '';
      this.notificationService.envoyerSelectionParChef({
        email: chefEmail,
        titre: titre
      }).subscribe();

      setTimeout(() => this.message = '', 3500);
      this.loadAppels();
    });
  }

  getNotifications(): void {
    this.notificationService.getNotifications('chef').subscribe((data: any[]) => {
      this.notifications = data;
      this.clocheActive = data.some(n => !n.lu);
    });
  }

  toggleNotif(): void {
    this.popupVisible = !this.popupVisible;
    if (this.popupVisible) {
      this.notifications.forEach(n => {
        if (!n.lu) this.lireNotification(n.id);
      });
    }
  }

  lireNotification(id: number): void {
    this.notificationService.marquerCommeLue(id).subscribe(() => {
      this.getNotifications();
    });
  }

  retourMenu(): void {
    this.router.navigate(['/login/chef']); // Navigation propre
  }
}
