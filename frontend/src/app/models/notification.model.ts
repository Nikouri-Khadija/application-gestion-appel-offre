// src/app/models/notification.model.ts
export interface Notification {
  id: number;
  contenu: string;
  destinataire: string;
  dateEnvoi: string;
  lu: boolean;
}
