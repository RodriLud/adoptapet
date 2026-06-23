import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const allowedRoles = route.data?.['roles'] as string[] | undefined;

  if (!authService.isLoggedIn()) {
    return router.createUrlTree(['/login']);
  }

  if (!allowedRoles?.length || allowedRoles.includes(authService.getUserRole())) {
    return true;
  }

  return authService.getUserRole() === 'ROLE_ADOPTANTE'
    ? router.createUrlTree(['/login'])
    : router.createUrlTree(['/dashboard']);
};
