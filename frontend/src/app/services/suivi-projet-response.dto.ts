export interface TacheResponse {
  nomTache: string;         // Nom de la tâche

  nomConsultant: string;       // Nom du consultant affecté
  dateAffectation: Date;  // Date d'affectation
  dateLimite: Date;
  statut: string;
  priorite: string;

}

export interface SuiviProjetResponse {
  nomProjet: string;
  progressionGlobale: number;
  nbTaches: number;
  nbConsultants: number;
  joursRestants: number;
  nbAFaire: number;
  nbEnCours: number;
  nbTermine: number;
  nbBloque: number;
  nbRetarde: number;
  taches: TacheResponse[];
}
