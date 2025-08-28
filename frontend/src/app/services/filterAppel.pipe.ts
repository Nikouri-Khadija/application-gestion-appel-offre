import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'filterAppel',
  standalone: true
})
export class FilterAppelPipe implements PipeTransform {
  transform(appels: any[], titre: string, statut: string): any[] {
    if (!appels) return [];

    titre = titre?.toLowerCase() || '';
    statut = statut?.toLowerCase() || '';

    return appels.filter(appel =>
      appel.titre.toLowerCase().includes(titre) &&
      appel.statut.toLowerCase().includes(statut)
    );
  }
}
