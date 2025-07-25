import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators,  ReactiveFormsModule} from '@angular/forms';

import { Router } from '@angular/router';
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Interface pour envoyer les données au backend
export interface AuthRequest {
  email: string;
  password: string;
}

// Interface pour recevoir la réponse du backend
export interface AuthResponse {
  token: string;
  role: 'ADMIN' | 'CHEF_DE_PROJET' | 'CONSULTANT';
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  // URL de ton backend Spring Boot (à adapter si nécessaire)
  private apiUrl = 'http://localhost:8080/api/auth/login';

  constructor(private http: HttpClient) {}

  // Méthode pour faire la requête de login (POST)
  login(credentials: AuthRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(this.apiUrl, credentials);
  }
}

import {CommonModule} from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  loginForm: FormGroup;

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onLogin() {
    // 🔴 Supprime le token expiré AVANT le login
    localStorage.removeItem('token');
    localStorage.removeItem('role');

    if (this.loginForm.invalid) return;

    this.auth.login(this.loginForm.value).subscribe({
      next: (res: AuthResponse) => {
        localStorage.setItem('token', res.token);
        localStorage.setItem('role', res.role);

        switch (res.role) {
          case 'ADMIN':
            this.router.navigate(['/login/admin']);
            break;
          case 'CHEF_DE_PROJET':
            this.router.navigate(['/login/chef']);
            break;
          case 'CONSULTANT':
            this.router.navigate(['/login/consultant']);
            break;
          default:
            alert("Rôle inconnu");
        }
      },
      error: () => {
        alert("Identifiants incorrects !");
      }
    });
  }

}
