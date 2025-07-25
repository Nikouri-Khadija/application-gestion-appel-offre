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
