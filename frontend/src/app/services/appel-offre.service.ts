import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AppelOffreService {
  private api = '/api/appels';

  constructor(private http: HttpClient) {}

  createAppel(data: FormData) {
    return this.http.post(`${this.api}/create`, data);
  }

  getAllAppels() {
    return this.http.get<any[]>(`${this.api}/admin`);
  }

  validerAppel(id: number) {
    return this.http.put(`${this.api}/valider/${id}`, {});
  }

  refuserAppel(id: number) {
    return this.http.put(`${this.api}/refuser/${id}`, {});
  }

  getAppelsPourChef() {
    return this.http.get<any[]>(`${this.api}/received`);
  }

  selectionnerAppel(id: number) {
    return this.http.post(`${this.api}/select/${id}`, {});
  }
}
