import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Sidebar } from '../shared/sidebar/sidebar';
import { AdoptanteResumen } from '../../models/adoptante';
import { AdoptanteService } from '../../services/adoptante.service';
import { AuthService } from '../../services/auth.service';
import { FeedbackService } from '../../services/feedback.service';

@Component({
  selector: 'app-lista-adoptantes',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, Sidebar],
  templateUrl: './lista-adoptantes.html',
  styleUrls: ['./lista-adoptantes.css'],
})
export class ListaAdoptantes implements OnInit {
  adoptantes: AdoptanteResumen[] = [];
  adoptanteDetalle: AdoptanteResumen | null = null;
  loading = true;
  cargandoDetalle = false;
  adoptanteProcesandoId: number | null = null;
  rol = '';
  busqueda = '';
  filtroHistorial = '';
  filtroEstado = '';
  paginaActual = 1;
  elementosPorPagina = 10;

  filtrosHistorial = [
    { label: 'Todos', value: '' },
    { label: 'Con adopciones', value: 'FINALIZADA' },
    { label: 'Con no asistencias', value: 'NO_ASISTIO' },
    { label: 'Con pendientes', value: 'PENDIENTE' },
    { label: 'Sin adopciones', value: 'SIN_FINALIZADAS' },
  ];

  constructor(
    private route: ActivatedRoute,
    private adoptanteService: AdoptanteService,
    private authService: AuthService,
    private feedback: FeedbackService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.rol = this.authService.getUserRole();
    this.cargarAdoptantes();
  }

  cargarAdoptantes() {
    this.loading = true;
    this.adoptanteService.listar().subscribe({
      next: (data) => {
        this.adoptantes = data;
        this.abrirDetalleDesdeRuta();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  verDetalle(adoptante: AdoptanteResumen) {
    if (!adoptante.id_adoptante) {
      return;
    }
    this.cargarDetalle(adoptante.id_adoptante);
  }

  cargarDetalle(idAdoptante: number) {
    this.cargandoDetalle = true;
    this.adoptanteService.buscarResumen(idAdoptante).subscribe({
      next: (data) => {
        this.adoptanteDetalle = data;
        this.cargandoDetalle = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.cargandoDetalle = false;
        this.cdr.detectChanges();
      },
    });
  }

  cerrarDetalle() {
    this.adoptanteDetalle = null;
  }

  async desactivar(adoptante: AdoptanteResumen) {
    if (!this.esAdmin() || !adoptante.id_adoptante) {
      return;
    }
    const idAdoptante = adoptante.id_adoptante;
    const procesosActivos = (adoptante.pendientes || 0) + (adoptante.aprobadas || 0);
    const detalleProceso = procesosActivos > 0
      ? ` Tiene ${procesosActivos} solicitud${procesosActivos === 1 ? '' : 'es'} activa${procesosActivos === 1 ? '' : 's'}; el historial se conservara.`
      : ' El historial se conservara.';
    const confirmado = await this.feedback.confirm({
      title: 'Desactivar adoptante',
      message: `${this.nombreCompleto(adoptante) || 'Este adoptante'} no podra operar desde la app.${detalleProceso}`,
      confirmText: 'Desactivar',
      tone: 'danger',
    });
    if (!confirmado) {
      return;
    }
    this.adoptanteProcesandoId = idAdoptante;
    this.adoptanteService.desactivar(idAdoptante).subscribe({
      next: () => {
        this.feedback.success('Adoptante desactivado');
        this.cargarAdoptantes();
        if (this.adoptanteDetalle?.id_adoptante === idAdoptante) {
          this.cargarDetalle(idAdoptante);
        }
      },
      error: (error) => {
        this.adoptanteProcesandoId = null;
        this.feedback.error(this.mensajeError(error, 'No se pudo desactivar el adoptante'));
        this.cdr.detectChanges();
      },
      complete: () => {
        this.adoptanteProcesandoId = null;
        this.cdr.detectChanges();
      },
    });
  }

  async activar(adoptante: AdoptanteResumen) {
    if (!this.esAdmin() || !adoptante.id_adoptante) {
      return;
    }
    const idAdoptante = adoptante.id_adoptante;
    const confirmado = await this.feedback.confirm({
      title: 'Activar adoptante',
      message: `${this.nombreCompleto(adoptante) || 'Este adoptante'} recuperara acceso operativo desde la app.`,
      confirmText: 'Activar',
      tone: 'primary',
    });
    if (!confirmado) {
      return;
    }
    this.adoptanteProcesandoId = idAdoptante;
    this.adoptanteService.activar(idAdoptante).subscribe({
      next: () => {
        this.feedback.success('Adoptante activado');
        this.cargarAdoptantes();
        if (this.adoptanteDetalle?.id_adoptante === idAdoptante) {
          this.cargarDetalle(idAdoptante);
        }
      },
      error: (error) => {
        this.adoptanteProcesandoId = null;
        this.feedback.error(this.mensajeError(error, 'No se pudo activar el adoptante'));
        this.cdr.detectChanges();
      },
      complete: () => {
        this.adoptanteProcesandoId = null;
        this.cdr.detectChanges();
      },
    });
  }

  alCambiarFiltro() {
    this.paginaActual = 1;
  }

  adoptantesFiltrados(): AdoptanteResumen[] {
    const texto = this.normalizar(this.busqueda);
    return this.adoptantes.filter((adoptante) => {
      const coincideTexto = !texto || [
        adoptante.nom_adoptante,
        adoptante.ape_adoptante,
        adoptante.dni,
        adoptante.email,
        adoptante.telefono,
      ].some((valor) => this.normalizar(valor).includes(texto));
      return coincideTexto && this.coincideHistorial(adoptante) && this.coincideEstado(adoptante);
    });
  }

  adoptantesPaginados(): AdoptanteResumen[] {
    const inicio = (this.paginaActual - 1) * this.elementosPorPagina;
    return this.adoptantesFiltrados().slice(inicio, inicio + this.elementosPorPagina);
  }

  totalPaginas(): number {
    return Math.max(1, Math.ceil(this.adoptantesFiltrados().length / this.elementosPorPagina));
  }

  paginas(): number[] {
    return Array.from({ length: this.totalPaginas() }, (_, index) => index + 1);
  }

  cambiarPagina(pagina: number) {
    if (pagina < 1 || pagina > this.totalPaginas()) {
      return;
    }
    this.paginaActual = pagina;
  }

  rangoActual(): string {
    const total = this.adoptantesFiltrados().length;
    if (total === 0) {
      return '0 de 0';
    }
    const inicio = (this.paginaActual - 1) * this.elementosPorPagina + 1;
    const fin = Math.min(this.paginaActual * this.elementosPorPagina, total);
    return `${inicio}-${fin} de ${total}`;
  }

  nombreCompleto(adoptante?: AdoptanteResumen | null): string {
    return `${adoptante?.nom_adoptante || ''} ${adoptante?.ape_adoptante || ''}`.trim();
  }

  statusClass(estado?: string): string {
    const normalizado = (estado || '').toLowerCase().replaceAll('_', '-');
    return normalizado ? `status-${normalizado}` : 'status-muted';
  }

  esAdmin(): boolean {
    return this.rol === 'ROLE_ADMIN';
  }

  activos(): number {
    return this.adoptantes.filter((adoptante) => adoptante.activo !== false).length;
  }

  private coincideHistorial(adoptante: AdoptanteResumen): boolean {
    if (this.filtroHistorial === 'FINALIZADA') {
      return (adoptante.finalizadas || 0) > 0;
    }
    if (this.filtroHistorial === 'NO_ASISTIO') {
      return (adoptante.no_asistio || 0) > 0;
    }
    if (this.filtroHistorial === 'PENDIENTE') {
      return (adoptante.pendientes || 0) > 0;
    }
    if (this.filtroHistorial === 'SIN_FINALIZADAS') {
      return (adoptante.finalizadas || 0) === 0;
    }
    return true;
  }

  private coincideEstado(adoptante: AdoptanteResumen): boolean {
    return !this.filtroEstado
      || (this.filtroEstado === 'ACTIVO' && adoptante.activo !== false)
      || (this.filtroEstado === 'INACTIVO' && adoptante.activo === false);
  }

  private normalizar(valor?: string): string {
    return (valor || '').toLowerCase().trim();
  }

  private mensajeError(error: any, fallback: string): string {
    return error?.error?.mensaje || fallback;
  }

  private abrirDetalleDesdeRuta() {
    const idAdoptante = Number(this.route.snapshot.paramMap.get('id'));
    if (!idAdoptante) {
      return;
    }
    const adoptante = this.adoptantes.find((item) => item.id_adoptante === idAdoptante);
    if (adoptante) {
      this.busqueda = this.nombreCompleto(adoptante) || adoptante.dni || '';
    }
    this.cargarDetalle(idAdoptante);
  }
}
