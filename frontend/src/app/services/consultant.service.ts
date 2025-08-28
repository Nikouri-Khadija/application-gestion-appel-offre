import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import {Observable, of} from 'rxjs';
import { NotificationService } from './notification.service';

export interface Consultant {
  organisme: string;
  id?: number;
  nomComplet: string;
  email: string;
  nomProjet: string;
  dateAffectation: string | Date;
  descriptionProjet?: string;
  showDescription?: boolean; // uniquement pour l'affichage côté front
}

@Injectable({
  providedIn: 'root'
})
export class ConsultantService {
  private api = '/api/consultants';

  constructor(
    private http: HttpClient,
    private notificationService: NotificationService
  ) {}

  getEmailsConsultants(): Observable<string[]> {
    return this.http.get<string[]>(`${this.api}/emails`);
  }

  getNomProjets(): Observable<string[]> {
    return this.http.get<string[]>(`${this.api}/projets`);
  }

  getAllConsultantsDetailed(): Observable<Consultant[]> {
    return this.http.get<Consultant[]>(`${this.api}/details`);
  }

  addConsultant(data: Consultant): Observable<Consultant> {
    return this.http.post<Consultant>(this.api, data);
  }

  updateConsultant(id: number, data: Consultant): Observable<Consultant> {
    return this.http.put<Consultant>(`${this.api}/${id}`, data);
  }

  deleteConsultant(id: number): Observable<void> {
    return this.http.delete<void>(`${this.api}/${id}`);
  }


  getMesProjets() {
    const role = localStorage.getItem('role'); // Récupération du rôle

    if (role === 'CONSULTANT') {
      return this.http.get<Consultant[]>('/api/consultants/mes-projets');
    }
    else if (role === 'CHEF_DE_PROJET') {
      return this.http.get<Consultant[]>('/api/consultants/details');
    }
    else {
      console.error('Rôle non reconnu :', role);
      return of([]); // Observable vide
    }
  }

}
