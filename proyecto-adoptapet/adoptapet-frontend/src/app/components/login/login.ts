import { ChangeDetectorRef, Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css'],
})
export class Login {
  credentials = { username: '', password: '' };
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  onLogin() {
    this.errorMessage = '';
    this.authService.login(this.credentials).subscribe({
      next: (user) => {
        if (!['ROLE_ADMIN', 'ROLE_TRABAJADOR'].includes(user?.rol)) {
          this.authService.logout();
          this.errorMessage = 'El portal web es solo para administradores y trabajadores';
          this.cdr.detectChanges();
          return;
        }
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.errorMessage = 'Usuario o contrasena incorrectos';
        this.cdr.detectChanges();
      },
    });
  }
}
