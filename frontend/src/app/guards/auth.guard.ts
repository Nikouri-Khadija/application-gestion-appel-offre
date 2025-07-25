import {CanActivateFn, Router} from '@angular/router';
import {inject} from '@angular/core';

export const authGuard: CanActivateFn = (route, state) => {
  const token = localStorage.getItem('token');
  const role = localStorage.getItem('role');
  const expectedRole = route.data?.['role'];

  if (!token || role !== expectedRole) {
    alert('Accès non autorisé');
    const router = inject(Router);
    router.navigate(['/']);
    return false;
  }
  return true;
};


