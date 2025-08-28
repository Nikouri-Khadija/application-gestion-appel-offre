import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import { SuiviProjetResponse, TacheResponse } from '../../services/suivi-projet-response.dto';
import { SuiviProjetService } from '../../services/suivi.service';
import { CommonModule, DatePipe, NgClass } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-suivi',
  templateUrl: './suivi.component.html',
  imports: [NgClass, DatePipe, CommonModule, FormsModule],
  styleUrls: ['./suivi.component.scss']
})
export class SuiviComponent implements OnInit {
  suiviProjet: SuiviProjetResponse | null = null;
  loading = false;
  error: string | null = null;
  nomProjet: string = '';
  selectedProjet: string = '';
  projetsAffectes: string[] = [];

  constructor(
    private route: ActivatedRoute,
    private suiviProjetService: SuiviProjetService
  ) {}

  ngOnInit(): void {
    this.suiviProjetService.getAllProjets().subscribe({
      next: noms => {
        this.projetsAffectes = noms;
      },
      error: err => {
        console.error('Erreur lors du chargement des projets:', err);
      }
    });
  }

  // Méthode déclenchée quand on change de projet
  onProjetChange(): void {
    if (this.selectedProjet) {
      this.nomProjet = this.selectedProjet;
      this.chargerSuiviProjet();
    }
  }

  chargerSuiviProjet(): void {
    this.loading = true;
    this.error = null;

    this.suiviProjetService.getSuiviProjet(this.nomProjet).subscribe({
      next: data => {
        // Assurer que la date d’affectation est toujours renseignée



        this.suiviProjet = data;
        this.loading = false;
      },
      error: err => {
        this.error = 'Erreur lors du chargement du suivi du projet';
        this.loading = false;
        console.error('Erreur:', err);
      }
    });
  }

  isRetarde(tache: TacheResponse): boolean {
    if (!tache.dateLimite) return false;
    const aujourdHui = new Date();
    const dateLimite = new Date(tache.dateLimite);
    return dateLimite < aujourdHui && tache.statut !== 'TERMINE';
  }

  genererPDF(): void {
    if (!this.suiviProjet) return;

    const doc = new jsPDF();

    // En-tête
    doc.setFontSize(20);
    doc.text(`SUIVI DU PROJET: ${this.suiviProjet.nomProjet}`, 105, 15, { align: 'center' });
    doc.setFontSize(12);
    doc.text(`Date de génération: ${new Date().toLocaleDateString('fr-FR')}`, 105, 22, { align: 'center' });

    // Statistiques
    doc.setFontSize(14);
    doc.text('STATISTIQUES DU PROJET', 14, 35);
    doc.setFontSize(10);

    const stats = [
      ['Progression globale', `${(this.suiviProjet.progressionGlobale ?? 0).toFixed(1)}%`],
      ['Nombre total de tâches', (this.suiviProjet.nbTaches ?? 0).toString()],
      ['Nombre de consultants', (this.suiviProjet.nbConsultants ?? 0).toString()],
      ['Jours restants', (this.suiviProjet.joursRestants ?? 0).toString()],
      ['À faire', (this.suiviProjet.nbAFaire ?? 0).toString()],
      ['En cours', (this.suiviProjet.nbEnCours ?? 0).toString()],
      ['Terminé', (this.suiviProjet.nbTermine ?? 0).toString()],
      ['Bloqué', (this.suiviProjet.nbBloque ?? 0).toString()],
      ['Retardé', (this.suiviProjet.nbRetarde ?? 0).toString()]
    ];

    autoTable(doc, {
      startY: 40,
      head: [['Métrique', 'Valeur']],
      body: stats,
      theme: 'grid',
      headStyles: { fillColor: [41, 128, 185] },
      margin: { top: 40 }
    });

    // Récupération sécurisée de finalY
    const finalYStats = (doc as any).lastAutoTable?.finalY ?? 60;

    // Section tâches
    // Section tâches
    doc.setFontSize(14);
    doc.text('DÉTAIL DES TÂCHES', 14, finalYStats + 15);

    const tachesData = (this.suiviProjet.taches ?? []).map(tache => [
      tache.nomTache || 'Sans titre',
      tache.statut ?? '',
      tache.priorite ?? '',
      tache.nomConsultant || 'Non assigné',
      tache.dateAffectation ? new Date(tache.dateAffectation).toLocaleDateString('fr-FR') : 'N/A',
      tache.dateLimite ? new Date(tache.dateLimite).toLocaleDateString('fr-FR') : 'N/A'
    ]);

    autoTable(doc, {
      startY: finalYStats + 20,
      head: [['Nom tâche', 'Statut', 'Priorité', 'Consultant', 'Date affectation', 'Date limite']],
      body: tachesData,
      theme: 'grid',
      headStyles: { fillColor: [41, 128, 185] },
      styles: { fontSize: 8 }
    });


    // Pied de page
    const pageCount = (doc as any).getNumberOfPages();
    for (let i = 1; i <= pageCount; i++) {
      doc.setPage(i);
      doc.setFontSize(8);
      doc.text(`Page ${i} sur ${pageCount}`, 105, doc.internal.pageSize.height - 10, { align: 'center' });
    }

    doc.save(`suivi-projet-${this.suiviProjet.nomProjet}-${new Date().toISOString().split('T')[0]}.pdf`);
  }

  getStatutClass(statut: string): string {
    const statutClasses: { [key: string]: string } = {
      'TERMINE': 'statut-termine',
      'EN_COURS': 'statut-encours',
      'A_FAIRE': 'statut-afaire',
      'BLOQUE': 'statut-bloque',
      'RETARDE': 'statut-retarde'
    };
    return statutClasses[statut] || 'statut-default';
  }
}
