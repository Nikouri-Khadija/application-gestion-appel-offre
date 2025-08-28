import { Component, Input, OnInit } from '@angular/core';
import { NotificationService } from '../services/notification.service';
import { CommonModule, NgFor, NgIf } from '@angular/common';

@Component({
  selector: 'app-notification-bell',
  templateUrl: './notification-bell.component.html',
  styleUrls: ['./notification-bell.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    NgIf,
    NgFor
  ]
})
export class NotificationBellComponent implements OnInit {
  @Input() destinataire: string = '';
  notifications: any[] = [];
  popupVisible = false;
  clocheActive = false;

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    // Récupérer l'email depuis le localStorage si non fourni
    if (!this.destinataire) {
      const storedEmail = localStorage.getItem('email');
      if (storedEmail) {
        this.destinataire = storedEmail;
      }
    }

    this.loadNotifications();
  }

  loadNotifications(): void {
    if (!this.destinataire) return;

    this.notificationService.getNotifications(this.destinataire).subscribe({
      next: (data: any[]) => {
        this.notifications = data;
        this.clocheActive = data.some(n => !n.lu);
      },
      error: (err) => {
        console.error('Erreur lors du chargement des notifications', err);
      }
    });
  }

  toggleNotif(): void {
    this.popupVisible = !this.popupVisible;

    if (this.popupVisible) {
      this.notifications.forEach(n => {
        if (!n.lu) {
          this.lireNotification(n.id);
        }
      });
    }
  }

  lireNotification(id: number): void {
    this.notificationService.marquerCommeLue(id).subscribe({
      next: () => this.loadNotifications(),
      error: (err) => console.error('Erreur lors de la mise à jour de la notification', err)
    });
  }
}
