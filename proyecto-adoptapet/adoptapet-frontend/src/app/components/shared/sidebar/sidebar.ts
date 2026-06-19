import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
})
export class Sidebar {
  rol = '';
  roleLabel = '';
  puedeVerPortal = false;

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {
    this.rol = this.authService.getUserRole();
    this.roleLabel = this.authService.getRoleLabel(this.rol);
    this.puedeVerPortal = ['ROLE_ADMIN', 'ROLE_TRABAJADOR'].includes(this.rol);
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
