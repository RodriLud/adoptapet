import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { MascotaService } from '../../services/mascota.service';
import { SolicitudService } from '../../services/solicitud.service';
import { Solicitud } from '../../models/solicitud';
import { Sidebar } from '../shared/sidebar/sidebar';

@Component({
  selector: 'app-menu',
  standalone: true,
  imports: [CommonModule, Sidebar],
  styleUrls: ['./menu.css'],
  templateUrl: './menu.html',
})
export class Menu implements OnInit {
  rol = '';
  roleLabel = '';
  username = '';
  totalMascotas = 0;
  disponibles = 0;
  reservadas = 0;
  adoptadas = 0;
  pendientes = 0;
  aprobadas = 0;
  noAsistio = 0;
  finalizadas = 0;
  rechazadas = 0;
  canceladas = 0;
  solicitudesPendientes: Solicitud[] = [];

  constructor(
    private authService: AuthService,
    private mascotaService: MascotaService,
    private solicitudService: SolicitudService,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit() {
    const user = this.authService.getCurrentUser();
    this.rol = user?.rol || '';
    this.roleLabel = this.authService.getRoleLabel(this.rol);
    this.username = user?.username || '';
    this.cargarResumen();
  }

  cargarResumen() {
    forkJoin({
      mascotas: this.mascotaService.listarMascotas(),
      solicitudes: this.solicitudService.listar(),
    }).subscribe(({ mascotas, solicitudes }) => {
      this.totalMascotas = mascotas.length;
      this.disponibles = mascotas.filter((m) => m.est_adopcion === 'DISPONIBLE').length;
      this.reservadas = mascotas.filter((m) => m.est_adopcion === 'RESERVADO').length;
      this.adoptadas = mascotas.filter((m) => m.est_adopcion === 'ADOPTADO').length;
      this.pendientes = solicitudes.filter((s) => s.estado_solicitud === 'PENDIENTE').length;
      this.aprobadas = solicitudes.filter((s) => s.estado_solicitud === 'APROBADA').length;
      this.noAsistio = solicitudes.filter((s) => s.estado_solicitud === 'NO_ASISTIO').length;
      this.finalizadas = solicitudes.filter((s) => s.estado_solicitud === 'FINALIZADA').length;
      this.rechazadas = solicitudes.filter((s) => s.estado_solicitud === 'RECHAZADA').length;
      this.canceladas = solicitudes.filter((s) => s.estado_solicitud === 'CANCELADA').length;
      this.solicitudesPendientes = solicitudes
        .filter((s) => s.estado_solicitud === 'PENDIENTE')
        .slice(-5)
        .reverse();
      this.cdr.detectChanges();
    });
  }

  irMascotas() {
    this.router.navigate(['/mascotas']);
  }

  irRegistrarMascota() {
    this.router.navigate(['/mascotas/registrar']);
  }

  irSolicitudes() {
    this.router.navigate(['/solicitudes']);
  }

  irTrabajadores() {
    this.router.navigate(['/trabajadores']);
  }

  verDetalle(id?: number) {
    if (id) {
      this.router.navigate(['/solicitudes/detalle', id]);
    }
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
