import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { AdminComponent } from './pages/admin/admin.component';
import { ChefComponent } from './pages/chef/chef.component';
import { ConsultantComponent } from './pages/consultant/consultant.component';
import { authGuard } from './guards/auth.guard';
import {AdminGestionComponent} from './appels/admin-gestion/admin-gestion.component';
import {ChefSelectionComponent} from './appels/chef-selection/chef-selection.component';
import {CreerProjetComponent} from './projet/creer-projet/creer-projet.component';
import {MesProjetsChefComponent} from './projet/mes-projets-chef/mes-projets-chef.component';
import {AffecterConsultantComponent} from './projet/affecter-consultant/affecter-consultant.component';
import {ProjetAffecteConsultantComponent} from './projet/projet-affecte-consultant/projet-affecte-consultant.component';
import {CreerTacheComponent} from './projet/creer-tache/creer-tache.component';
import {MesTachesComponent} from './projet/mes-taches/mes-taches.component';
import {SuiviComponent} from './projet/suivi/suivi.component';



export const routes: Routes = [
  {path : 'chef/Suivi-Projets' , component:SuiviComponent           },
  {path : 'mes-taches-consultant' , component:MesTachesComponent            },
  {path: 'chef/gestion-taches', component: CreerTacheComponent },
  {path : 'mes-projets-consultant' , component:ProjetAffecteConsultantComponent            },
  {path : 'chef/Mes-projetsConsultants', component: AffecterConsultantComponent},
  {path : 'chef/Mes-projetsChef', component: MesProjetsChefComponent},
  {path : 'admin/creation-projects', component: CreerProjetComponent },
  { path: 'admin/gestion-appels', component: AdminGestionComponent },
  { path: 'chef/selection-appels', component: ChefSelectionComponent },
  { path: '', component: LoginComponent },
  { path: 'login/admin', component: AdminComponent, canActivate: [authGuard], data: { role: 'ADMIN' } },
  { path: 'login/chef', component: ChefComponent, canActivate: [authGuard], data: { role: 'CHEF_DE_PROJET' } },
  { path: 'login/consultant', component: ConsultantComponent, canActivate: [authGuard], data: { role: 'CONSULTANT' } },
];

