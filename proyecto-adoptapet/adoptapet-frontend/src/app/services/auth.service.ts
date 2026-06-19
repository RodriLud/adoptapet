import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/auth';

  constructor(private http: HttpClient) {}

  login(credentials: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, credentials).pipe(
      tap((user: any) => {
        localStorage.setItem(
          'user_session',
          JSON.stringify({ ...user, password: credentials.password }),
        );
      }),
    );
  }

  logout() {
    localStorage.removeItem('user_session');
  }

  isLoggedIn(): boolean {
    if (typeof window !== 'undefined' && window.localStorage) {
      return localStorage.getItem('user_session') !== null;
    }
    return false;
  }

  getCurrentUser(): any {
    if (typeof window === 'undefined') {
      return null;
    }
    return JSON.parse(localStorage.getItem('user_session') || 'null');
  }

  getUserRole(): string {
    return this.getCurrentUser()?.rol || '';
  }

  getRoleLabel(role = this.getUserRole()): string {
    const labels: Record<string, string> = {
      ROLE_ADMIN: 'Administrador',
      ROLE_TRABAJADOR: 'Trabajador',
      ROLE_ADOPTANTE: 'Adoptante',
    };
    return labels[role] || 'Usuario';
  }
}
