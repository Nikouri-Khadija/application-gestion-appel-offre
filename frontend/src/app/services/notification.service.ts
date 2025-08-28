// 8. notification.service.ts
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private api = '/api/notifications';

  constructor(private http: HttpClient) {}

  getNotifications(destinataire: string) {
    return this.http.get<any[]>(`${this.api}/${destinataire}`);
  }

  envoyerNotification(data: { contenu: string; destinataire: string }) {
    return this.http.post(`${this.api}/send`, data);
  }

  marquerCommeLue(id: number) {
    return this.http.put(`${this.api}/lu/${id}`, {});
  }
  envoyerSelectionParChef(data: { email: string; titre: string }) {
    return this.http.post(`/api/notifications/chef-selection`, data);
  }

}
