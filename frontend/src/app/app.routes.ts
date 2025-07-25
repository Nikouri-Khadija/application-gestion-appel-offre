import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { AdminComponent } from './pages/admin/admin.component';
import { ChefComponent } from './pages/chef/chef.component';
import { ConsultantComponent } from './pages/consultant/consultant.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', component: LoginComponent },
  { path: 'login/admin', component: AdminComponent, canActivate: [authGuard], data: { role: 'ADMIN' } },
  { path: 'login/chef', component: ChefComponent, canActivate: [authGuard], data: { role: 'CHEF_DE_PROJET' } },
  { path: 'login/consultant', component: ConsultantComponent, canActivate: [authGuard], data: { role: 'CONSULTANT' } },
];

