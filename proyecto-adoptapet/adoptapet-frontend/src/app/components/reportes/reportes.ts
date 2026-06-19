import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { MascotaService } from '../../services/mascota.service';
import { SolicitudService } from '../../services/solicitud.service';
import { AuthService } from '../../services/auth.service';
import { ReporteService } from '../../services/reporte.service';
import { Mascota } from '../../models/mascota';
import { Solicitud } from '../../models/solicitud';
import { Sidebar } from '../shared/sidebar/sidebar';
import { FeedbackService } from '../../services/feedback.service';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule, FormsModule, Sidebar],
  templateUrl: './reportes.html',
  styleUrls: ['./reportes.css'],
})
export class Reportes implements OnInit {
  rol = '';
  mascotas: Mascota[] = [];
  solicitudes: Solicitud[] = [];
  opcionesEstadoSolicitud: string[] = [];
  opcionesEstadoMascota: string[] = [];
  opcionesEspecie: string[] = [];
  filtroEstadoSolicitud = '';
  filtroEstadoMascota = '';
  filtroEspecie = '';
  paginaSolicitudes = 1;
  solicitudesPorPagina = 10;
  descargando = false;

  constructor(
    private mascotaService: MascotaService,
    private solicitudService: SolicitudService,
    private authService: AuthService,
    private reporteService: ReporteService,
    private cdr: ChangeDetectorRef,
    private feedback: FeedbackService,
  ) {}

  ngOnInit(): void {
    this.rol = this.authService.getUserRole();
    forkJoin({
      mascotas: this.mascotaService.listarMascotas(),
      solicitudes: this.solicitudService.listar(),
    }).subscribe(({ mascotas, solicitudes }) => {
      this.mascotas = mascotas;
      this.solicitudes = solicitudes;
      this.opcionesEstadoSolicitud = this.opciones(solicitudes, 'estado_solicitud');
      this.opcionesEstadoMascota = this.opciones(mascotas, 'est_adopcion');
      this.opcionesEspecie = this.opciones(mascotas, 'especie');
      this.cdr.detectChanges();
    });
  }

  get mascotasFiltradas(): Mascota[] {
    return this.mascotas.filter((mascota) => {
      const coincideEstado = !this.filtroEstadoMascota || mascota.est_adopcion === this.filtroEstadoMascota;
      const coincideEspecie = !this.filtroEspecie || mascota.especie === this.filtroEspecie;
      return coincideEstado && coincideEspecie;
    });
  }

  get solicitudesFiltradas(): Solicitud[] {
    return this.solicitudes.filter((solicitud) => {
      return !this.filtroEstadoSolicitud || solicitud.estado_solicitud === this.filtroEstadoSolicitud;
    });
  }

  agrupar(items: any[], campo: string): { label: string; value: number }[] {
    const resumen = new Map<string, number>();
    items.forEach((item) => {
      const key = item[campo] || 'SIN DATO';
      resumen.set(key, (resumen.get(key) || 0) + 1);
    });
    return Array.from(resumen.entries()).map(([label, value]) => ({ label, value }));
  }

  opciones(items: any[], campo: string): string[] {
    return Array.from(new Set(items.map((item) => item[campo]).filter(Boolean))).sort();
  }

  porcentaje(value: number, total: number): number {
    return total === 0 ? 0 : Math.round((value / total) * 100);
  }

  tasaAprobacion(): number {
    return this.tasaPorEstado('APROBADA');
  }

  tasaRechazo(): number {
    return this.tasaPorEstado('RECHAZADA');
  }

  tasaFinalizacion(): number {
    return this.tasaPorEstado('FINALIZADA');
  }

  tasaPorEstado(estado: string): number {
    const total = this.solicitudesFiltradas.length;
    const cantidad = this.solicitudesFiltradas.filter((item) => item.estado_solicitud === estado).length;
    return total === 0 ? 0 : Math.round((cantidad / total) * 100);
  }

  ultimasFinalizadas(): Solicitud[] {
    return this.solicitudes
      .filter((item) => item.estado_solicitud === 'FINALIZADA')
      .slice(-5)
      .reverse();
  }

  limpiarFiltros() {
    this.filtroEstadoSolicitud = '';
    this.filtroEstadoMascota = '';
    this.filtroEspecie = '';
    this.paginaSolicitudes = 1;
  }

  alCambiarFiltros() {
    this.paginaSolicitudes = 1;
  }

  solicitudesReportePaginadas(): Solicitud[] {
    const inicio = (this.paginaSolicitudes - 1) * this.solicitudesPorPagina;
    return this.solicitudesFiltradas.slice(inicio, inicio + this.solicitudesPorPagina);
  }

  totalPaginasSolicitudes(): number {
    return Math.max(1, Math.ceil(this.solicitudesFiltradas.length / this.solicitudesPorPagina));
  }

  paginasSolicitudes(): number[] {
    return Array.from({ length: this.totalPaginasSolicitudes() }, (_, index) => index + 1);
  }

  cambiarPaginaSolicitudes(pagina: number) {
    if (pagina < 1 || pagina > this.totalPaginasSolicitudes()) {
      return;
    }
    this.paginaSolicitudes = pagina;
  }

  rangoSolicitudes(): string {
    const total = this.solicitudesFiltradas.length;
    if (total === 0) {
      return '0 de 0';
    }
    const inicio = (this.paginaSolicitudes - 1) * this.solicitudesPorPagina + 1;
    const fin = Math.min(this.paginaSolicitudes * this.solicitudesPorPagina, total);
    return `${inicio}-${fin} de ${total}`;
  }

  statusClass(estado?: string): string {
    const normalizado = (estado || '').toLowerCase().replaceAll('_', '-');
    return normalizado ? `status-${normalizado}` : 'status-muted';
  }

  descargarPdf() {
    this.descargando = true;
    this.reporteService.descargarReporteGeneral({
      estadoSolicitud: this.filtroEstadoSolicitud,
      estadoMascota: this.filtroEstadoMascota,
      especie: this.filtroEspecie,
    }).subscribe((pdf) => {
      const url = window.URL.createObjectURL(pdf);
      const link = document.createElement('a');
      link.href = url;
      link.download = `reporte_adopciones_${new Date().toISOString().slice(0, 10)}.pdf`;
      link.click();
      window.URL.revokeObjectURL(url);
      this.feedback.success('Reporte descargado');
    }, () => {
      this.feedback.error('No se pudo descargar el reporte');
    }).add(() => {
      this.descargando = false;
      this.cdr.detectChanges();
    });
  }
}
