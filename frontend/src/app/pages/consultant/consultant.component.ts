import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NotificationBellComponent } from '../../notification-bell/notification-bell.component';

@Component({
  selector: 'app-consultant',
  templateUrl: './consultant.component.html',
  styleUrls: ['./consultant.component.scss'],
  standalone: true,
  imports: [
    NotificationBellComponent
  ]
})
export class ConsultantComponent implements OnInit {

  sidebarOpen: boolean = false;
  userEmail: string = '';

  constructor(private router: Router) {}

  ngOnInit(): void {
    // Récupérer l'email du consultant connecté depuis localStorage (ou autre source)
    this.userEmail = localStorage.getItem('userEmail') || '';
  }

  toggleSidebar(): void {
    this.sidebarOpen = !this.sidebarOpen;
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('userEmail');  // Penser à nettoyer aussi l'email
    this.router.navigate(['/']);
  }

  showProjectConsultant(): void {
    this.router.navigate(['/mes-projets-consultant']); // adapte la route selon ta config
  }

  MesTaches() {
    this.router.navigate(['/mes-taches-consultant']);
  }
}
