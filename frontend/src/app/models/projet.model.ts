import { User } from './user.model';

export interface Projet {
  id?: number; // optionnel pour la création
  nomProjet: string;
  codeProjet: string;
  chefProjet: User; // relation ManyToOne avec User
  dateCreation?: string; // peut être laissé vide à la création
  dateLimite: string;
  description?: string;

}
