import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class TacheService {
  private apiUrl = 'http://localhost:8080/api/taches';

  constructor(private http: HttpClient) {}

  getTaches(): Observable<any[]> {
    return this.http.get<any[]>(this.apiUrl);
  }

  createTache(tache: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, tache);
  }

  deleteTache(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }



  getConsultants(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/consultants`);
  }

  getProjetsByConsultant(consultantId: number): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/consultants/${consultantId}/projets`);
  }

  updateStatut(id: number, statut: string, commentaire?: string): Observable<any> {
    const params: any = { statut };
    if (commentaire) params.commentaire = commentaire;

    return this.http.put<any>(`${this.apiUrl}/${id}/statut`, null, { params });
  }


  updateDetails(id: number, payload: any): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, payload);
  }

  getMesTaches(email?: string): Observable<any[]> {
    let url = this.apiUrl + '/mes-taches';
    if (email) {
      url += `?email=${email}`;
    }
    return this.http.get<any[]>(url);
  }

  // Appelle le contrôleur /statistiques/{nomProjet}
  getStatistiques(nomProjet: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/statistiques/${nomProjet}`);
  }


}
