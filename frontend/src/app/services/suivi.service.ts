import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {SuiviProjetResponse} from './suivi-projet-response.dto';


@Injectable({
  providedIn: 'root'
})
export class SuiviProjetService {
  private apiUrl = 'http://localhost:8080/api/suivi-projet';

  constructor(private http: HttpClient) { }

  getSuiviProjet(nomProjet: string): Observable<SuiviProjetResponse> {
    return this.http.get<SuiviProjetResponse>(`${this.apiUrl}/${encodeURIComponent(nomProjet)}`);
  }

  // suivi.service.ts
  getAllProjets(): Observable<string[]> {
    return this.http.get<string[]>('http://localhost:8080/api/suivi-projet/projets-chef');
  }



}
