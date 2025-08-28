import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { Projet } from '../models/projet.model';
import { User } from '../models/user.model';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class ProjetService {
  private baseUrl = 'http://localhost:8080/api/projets';

  compteurEnCours$ = new BehaviorSubject<number>(0);
  compteurProjets$ = new BehaviorSubject<number>(0);

  private projetCreeSubject = new Subject<void>();
  projetCree$ = this.projetCreeSubject.asObservable();

  constructor(private http: HttpClient) {

  }

  getProjets(): Observable<Projet[]> {
    return this.http.get<Projet[]>(`${this.baseUrl}/all`);
  }

  ajouterProjet(projet: any): Observable<Projet> {
    return this.http.post<Projet>(`${this.baseUrl}/ajouter`, projet).pipe(
      tap(() => this.notifierProjetCree()) // Notifie après succès
    );
  }

  supprimerProjet(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  modifierProjet(id: number, projet: any): Observable<Projet> {
    return this.http.put<Projet>(`${this.baseUrl}/modifier/${id}`, projet);
  }

  getChefs(): Observable<User[]> {
    return this.http.get<User[]>('http://localhost:8080/api/users/chefs');
  }



  notifierProjetCree() {
    this.projetCreeSubject.next();
    this.refreshCompteurs();
  }
  refreshCompteurs() {
    this.projetCreeSubject.next();
  }

  getUserByEmail(email: string): Observable<User> {
    return this.http.get<User>(`http://localhost:8080/api/users/email/${email}`);
  }

  getProjetsByChef(chefId: number): Observable<Projet[]> {
    return this.http.get<Projet[]>(`${this.baseUrl}/chef/${chefId}`);
  }
  getCompteurs(): Observable<{ enCours: number, projets: number }> {
    return this.http.get<{ enCours: number, projets: number }>(`${this.baseUrl}/compteurs`);
  }


}
