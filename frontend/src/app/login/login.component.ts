import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators,  ReactiveFormsModule} from '@angular/forms';

import { Router } from '@angular/router';


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



import {CommonModule} from '@angular/common';
import {AuthService} from '../services/auth.service';

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
