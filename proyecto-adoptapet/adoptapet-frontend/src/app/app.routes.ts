import { Routes } from '@angular/router';
import { Login } from './components/login/login';
import { Menu } from './components/menu/menu';
import { ListaMascotas } from './components/lista-mascotas/lista-mascotas';
import { ListaSolicitudes } from './components/lista-solicitudes/lista-solicitudes';
import { SolicitudDetalle } from './components/solicitud-detalle/solicitud-detalle';
import { ListaAdoptantes } from './components/lista-adoptantes/lista-adoptantes';
import { Trabajadores } from './components/trabajadores/trabajadores';
import { Reportes } from './components/reportes/reportes';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

const WEB_ROLES = ['ROLE_ADMIN', 'ROLE_TRABAJADOR'];
const ADMIN_ROLES = ['ROLE_ADMIN'];

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'dashboard', component: Menu, canActivate: [authGuard, roleGuard], data: { roles: WEB_ROLES } },
  { path: 'mascotas', component: ListaMascotas, canActivate: [authGuard, roleGuard], data: { roles: WEB_ROLES } },
  { path: 'mascotas/registrar', component: ListaMascotas, canActivate: [authGuard, roleGuard], data: { roles: WEB_ROLES } },
  { path: 'adoptantes', component: ListaAdoptantes, canActivate: [authGuard, roleGuard], data: { roles: WEB_ROLES } },
  { path: 'adoptantes/:id', component: ListaAdoptantes, canActivate: [authGuard, roleGuard], data: { roles: WEB_ROLES } },
  { path: 'solicitudes', component: ListaSolicitudes, canActivate: [authGuard, roleGuard], data: { roles: WEB_ROLES } },
  { path: 'solicitudes/detalle/:id', component: SolicitudDetalle, canActivate: [authGuard, roleGuard], data: { roles: WEB_ROLES } },
  { path: 'trabajadores', component: Trabajadores, canActivate: [authGuard, roleGuard], data: { roles: ADMIN_ROLES } },
  { path: 'reportes', component: Reportes, canActivate: [authGuard, roleGuard], data: { roles: ADMIN_ROLES } },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' },
];
