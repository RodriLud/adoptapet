import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AprobarSolicitudPayload, HorarioDisponibilidad, SolicitudService } from '../../services/solicitud.service';
import { Solicitud } from '../../models/solicitud';
import { AuthService } from '../../services/auth.service';
import { Sidebar } from '../shared/sidebar/sidebar';
import { FeedbackService } from '../../services/feedback.service';

@Component({
  selector: 'app-lista-solicitudes',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, Sidebar],
  templateUrl: './lista-solicitudes.html',
  styleUrls: ['./lista-solicitudes.css'],
})
export class ListaSolicitudes implements OnInit {
  private readonly minutosToleranciaCierre = 30;
  solicitudes: Solicitud[] = [];
  loading = true;
  filtroEstado = '';
  busqueda = '';
  filtroFechaEntrega = '';
  grupoEstados: 'ACTIVAS' | 'HISTORIAL' | 'TODAS' = 'ACTIVAS';
  paginaActual = 1;
  elementosPorPagina = 10;
  rol = '';
  solicitudAccion: Solicitud | null = null;
  modalAccion: 'aprobar' | 'rechazar' | null = null;
  horariosEntrega = [
    { label: '10:00 a.m. - 12:00 p.m.', inicio: '10:00', fin: '12:00' },
    { label: '2:00 p.m. - 4:00 p.m.', inicio: '14:00', fin: '16:00' },
    { label: '4:00 p.m. - 6:00 p.m.', inicio: '16:00', fin: '18:00' },
  ];
  programacionForm = {
    fecha_entrega: '',
    horario: '10:00|12:00',
    fecha_limite_recojo: '',
    observacion: '',
  };
  disponibilidadHorarios: HorarioDisponibilidad[] = [];
  cargandoHorarios = false;
  procesandoAccion = false;
  motivoForm = '';
  estadosActivos = [
    { label: 'Pendientes', value: 'PENDIENTE' },
    { label: 'Aprobadas', value: 'APROBADA' },
    { label: 'No asistio', value: 'NO_ASISTIO' },
  ];
  estadosHistorial = [
    { label: 'Finalizadas', value: 'FINALIZADA' },
    { label: 'Rechazadas', value: 'RECHAZADA' },
    { label: 'Canceladas', value: 'CANCELADA' },
  ];

  constructor(
    private solicitudService: SolicitudService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private feedback: FeedbackService,
  ) {}

  ngOnInit(): void {
    this.rol = this.authService.getUserRole();
    this.cargarSolicitudes();
  }

  cargarSolicitudes(estado = '') {
    this.loading = true;
    this.filtroEstado = estado;
    this.paginaActual = 1;
    if (estado) {
      this.grupoEstados = this.esEstadoHistorial(estado) ? 'HISTORIAL' : 'ACTIVAS';
    }
    this.solicitudService.listar(estado || undefined).subscribe({
      next: (data) => {
        this.solicitudes = data;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  aprobar(solicitud: Solicitud) {
    if (!solicitud.id_solicitud) {
      return;
    }
    this.solicitudAccion = solicitud;
    this.abrirModalAprobar();
  }

  confirmarAprobacion() {
    if (!this.solicitudAccion?.id_solicitud) {
      return;
    }
    const programacion = this.construirProgramacionEntrega();
    if (!programacion) {
      return;
    }
    this.procesandoAccion = true;
    this.solicitudService.aprobar(this.solicitudAccion.id_solicitud, programacion).subscribe({
      next: () => {
        this.cargarSolicitudes(this.filtroEstado);
        this.feedback.success('Solicitud aprobada', 'La entrega quedo programada.');
      },
      error: (error) => {
        this.procesandoAccion = false;
        this.feedback.error(this.mensajeError(error, 'No se pudo aprobar la solicitud'));
        this.cdr.detectChanges();
      },
      complete: () => {
        this.procesandoAccion = false;
        this.cerrarModal();
      },
    });
  }

  rechazar(solicitud: Solicitud) {
    if (!solicitud.id_solicitud) {
      return;
    }
    this.solicitudAccion = solicitud;
    this.modalAccion = 'rechazar';
    this.motivoForm = '';
  }

  confirmarRechazo() {
    if (!this.solicitudAccion?.id_solicitud) {
      return;
    }
    if (!this.motivoForm.trim()) {
      this.feedback.warning('Motivo requerido', 'Ingresa el motivo de rechazo antes de continuar.');
      return;
    }
    this.procesandoAccion = true;
    this.solicitudService.rechazar(this.solicitudAccion.id_solicitud, this.motivoForm.trim()).subscribe({
      next: () => {
        this.cargarSolicitudes(this.filtroEstado);
        this.feedback.success('Solicitud rechazada');
      },
      error: (error) => {
        this.procesandoAccion = false;
        this.feedback.error(this.mensajeError(error, 'No se pudo rechazar la solicitud'));
        this.cdr.detectChanges();
      },
      complete: () => {
        this.procesandoAccion = false;
        this.cerrarModal();
      },
    });
  }

  abrirModalAprobar() {
    this.modalAccion = 'aprobar';
    const opcion = this.proximaOpcionEntrega();
    this.programacionForm = {
      fecha_entrega: opcion.fecha,
      horario: `${opcion.inicio}|${opcion.fin}`,
      fecha_limite_recojo: this.fechaLimiteConTolerancia(opcion.fecha, opcion.fin),
      observacion: '',
    };
    this.cargarDisponibilidadHorarios(true);
  }

  cerrarModal() {
    this.modalAccion = null;
    this.solicitudAccion = null;
    this.procesandoAccion = false;
    this.motivoForm = '';
    this.disponibilidadHorarios = [];
  }

  actualizarFechaEntrega() {
    this.actualizarLimitePorHorario();
    this.cargarDisponibilidadHorarios();
  }

  actualizarLimitePorHorario() {
    if (!this.programacionForm.fecha_entrega) {
      return;
    }
    const [, horaFin] = this.programacionForm.horario.split('|');
    this.programacionForm.fecha_limite_recojo = this.fechaLimiteConTolerancia(this.programacionForm.fecha_entrega, horaFin);
  }

  cargarDisponibilidadHorarios(buscarSiguiente = false) {
    if (!this.programacionForm.fecha_entrega) {
      this.disponibilidadHorarios = [];
      return;
    }
    this.cargandoHorarios = true;
    this.solicitudService.disponibilidad(this.programacionForm.fecha_entrega).subscribe({
      next: (horarios) => {
        if (buscarSiguiente && !this.tieneHorarioDisponible(horarios)) {
          this.buscarSiguienteFechaDisponible(this.programacionForm.fecha_entrega);
          return;
        }
        this.disponibilidadHorarios = horarios;
        this.ajustarHorarioDisponible();
        this.cargandoHorarios = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.cargandoHorarios = false;
        this.cdr.detectChanges();
      },
    });
  }

  horariosVisibles(): HorarioDisponibilidad[] {
    if (this.disponibilidadHorarios.length) {
      return this.disponibilidadHorarios;
    }
    return this.horariosEntrega.map((horario) => ({
      label: horario.label,
      hora_inicio: horario.inicio,
      hora_fin: horario.fin,
      capacidad: 2,
      reservadas: 0,
      disponibles: 2,
      disponible: true,
      vencido: false,
    }));
  }

  valorHorario(horario: HorarioDisponibilidad): string {
    return `${horario.hora_inicio}|${horario.hora_fin}`;
  }

  etiquetaHorario(horario: HorarioDisponibilidad): string {
    if (horario.vencido) {
      return `${horario.label} - vencido`;
    }
    if (!horario.disponible) {
      return `${horario.label} - lleno`;
    }
    return `${horario.label} - ${horario.disponibles} cupo${horario.disponibles === 1 ? '' : 's'}`;
  }

  sinHorariosDisponibles(): boolean {
    return this.disponibilidadHorarios.length > 0 && !this.disponibilidadHorarios.some((horario) => horario.disponible);
  }

  cambiarGrupoEstados(grupo: 'ACTIVAS' | 'HISTORIAL' | 'TODAS') {
    this.grupoEstados = grupo;
    this.filtroEstado = '';
    this.paginaActual = 1;
    this.cargarSolicitudes('');
  }

  cambiarEstado(estado: string) {
    this.cargarSolicitudes(estado);
  }

  alCambiarBusqueda() {
    this.paginaActual = 1;
  }

  alCambiarFechaEntrega() {
    this.paginaActual = 1;
  }

  limpiarFiltroFechaEntrega() {
    this.filtroFechaEntrega = '';
    this.paginaActual = 1;
  }

  solicitudesFiltradas(): Solicitud[] {
    const texto = this.normalizar(this.busqueda);
    return this.solicitudes.filter((solicitud) => {
      const coincideGrupo = this.coincideGrupo(solicitud);
      const coincideTexto = !texto || [
        solicitud.adoptante?.nom_adoptante,
        solicitud.adoptante?.ape_adoptante,
        solicitud.adoptante?.email,
        solicitud.mascota?.nombre,
      ].some((valor) => this.normalizar(valor).includes(texto));
      const coincideFechaEntrega = !this.filtroFechaEntrega || this.fechaEntregaNormalizada(solicitud) === this.filtroFechaEntrega;
      return coincideGrupo && coincideTexto && coincideFechaEntrega;
    });
  }

  solicitudesPaginadas(): Solicitud[] {
    const inicio = (this.paginaActual - 1) * this.elementosPorPagina;
    return this.solicitudesFiltradas().slice(inicio, inicio + this.elementosPorPagina);
  }

  totalPaginas(): number {
    return Math.max(1, Math.ceil(this.solicitudesFiltradas().length / this.elementosPorPagina));
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
    const total = this.solicitudesFiltradas().length;
    if (total === 0) {
      return '0 de 0';
    }
    const inicio = (this.paginaActual - 1) * this.elementosPorPagina + 1;
    const fin = Math.min(this.paginaActual * this.elementosPorPagina, total);
    return `${inicio}-${fin} de ${total}`;
  }

  estadosVisibles() {
    if (this.grupoEstados === 'HISTORIAL') {
      return this.estadosHistorial;
    }
    if (this.grupoEstados === 'TODAS') {
      return [...this.estadosActivos, ...this.estadosHistorial];
    }
    return this.estadosActivos;
  }

  statusClass(estado?: string): string {
    const normalizado = (estado || '').toLowerCase().replaceAll('_', '-');
    return normalizado ? `status-${normalizado}` : 'status-muted';
  }

  esPrioritaria(solicitud: Solicitud): boolean {
    return solicitud.estado_solicitud === 'PENDIENTE';
  }

  private construirProgramacionEntrega(): AprobarSolicitudPayload | null {
    if (!this.programacionForm.fecha_entrega) {
      this.feedback.warning('Fecha requerida', 'Selecciona una fecha de entrega.');
      return null;
    }
    if (!this.programacionForm.fecha_limite_recojo) {
      this.feedback.warning('Limite requerido', 'Define la fecha limite de cierre.');
      return null;
    }
    const [hora_inicio, hora_fin] = this.programacionForm.horario.split('|');
    const horarioDisponible = this.disponibilidadHorarios.find((horario) => this.valorHorario(horario) === this.programacionForm.horario);
    if (horarioDisponible && !horarioDisponible.disponible) {
      this.feedback.warning('Horario no disponible', 'Selecciona un horario con cupos disponibles.');
      return null;
    }
    const inicio = new Date(`${this.programacionForm.fecha_entrega}T${hora_inicio}:00`);
    if (inicio <= new Date()) {
      this.feedback.warning('Horario vencido', 'No se puede programar una entrega en un horario que ya inicio o vencio.');
      return null;
    }
    return {
      fecha_entrega: this.programacionForm.fecha_entrega,
      hora_inicio,
      hora_fin,
      fecha_limite_recojo: this.programacionForm.fecha_limite_recojo,
      observacion: this.programacionForm.observacion.trim(),
    };
  }

  private proximaOpcionEntrega(): { fecha: string; inicio: string; fin: string } {
    const ahora = new Date();
    const hoy = this.fechaInput(ahora);
    const disponibleHoy = this.horariosEntrega.find((horario) => {
      const inicio = new Date(`${hoy}T${horario.inicio}:00`);
      return inicio > ahora;
    });
    if (disponibleHoy) {
      return { fecha: hoy, inicio: disponibleHoy.inicio, fin: disponibleHoy.fin };
    }
    const manana = new Date(ahora);
    manana.setDate(manana.getDate() + 1);
    const primerHorario = this.horariosEntrega[0];
    return { fecha: this.fechaInput(manana), inicio: primerHorario.inicio, fin: primerHorario.fin };
  }

  private fechaInput(fecha: Date): string {
    const year = fecha.getFullYear();
    const month = String(fecha.getMonth() + 1).padStart(2, '0');
    const day = String(fecha.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  private fechaLimiteConTolerancia(fecha: string, horaFin: string): string {
    const limite = new Date(`${fecha}T${horaFin}:00`);
    limite.setMinutes(limite.getMinutes() + this.minutosToleranciaCierre);
    return this.fechaHoraInput(limite);
  }

  private fechaHoraInput(fecha: Date): string {
    const year = fecha.getFullYear();
    const month = String(fecha.getMonth() + 1).padStart(2, '0');
    const day = String(fecha.getDate()).padStart(2, '0');
    const hours = String(fecha.getHours()).padStart(2, '0');
    const minutes = String(fecha.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  private ajustarHorarioDisponible() {
    const seleccionado = this.disponibilidadHorarios.find((horario) => this.valorHorario(horario) === this.programacionForm.horario);
    if (!seleccionado?.disponible) {
      const primerDisponible = this.disponibilidadHorarios.find((horario) => horario.disponible);
      if (primerDisponible) {
        this.programacionForm.horario = this.valorHorario(primerDisponible);
      }
    }
    this.actualizarLimitePorHorario();
  }

  private buscarSiguienteFechaDisponible(fechaBase: string, diasRevisados = 1) {
    if (diasRevisados > 14) {
      this.cargandoHorarios = false;
      this.cdr.detectChanges();
      return;
    }
    const siguiente = new Date(`${fechaBase}T00:00:00`);
    siguiente.setDate(siguiente.getDate() + 1);
    const fecha = this.fechaInput(siguiente);
    this.solicitudService.disponibilidad(fecha).subscribe({
      next: (horarios) => {
        if (this.tieneHorarioDisponible(horarios)) {
          this.programacionForm.fecha_entrega = fecha;
          this.disponibilidadHorarios = horarios;
          this.ajustarHorarioDisponible();
          this.cargandoHorarios = false;
          this.cdr.detectChanges();
          return;
        }
        this.buscarSiguienteFechaDisponible(fecha, diasRevisados + 1);
      },
      error: () => {
        this.cargandoHorarios = false;
        this.cdr.detectChanges();
      },
    });
  }

  private tieneHorarioDisponible(horarios: HorarioDisponibilidad[]): boolean {
    return horarios.some((horario) => horario.disponible);
  }

  private coincideGrupo(solicitud: Solicitud): boolean {
    if (this.filtroEstado || this.grupoEstados === 'TODAS') {
      return true;
    }
    const estados = this.grupoEstados === 'HISTORIAL' ? this.estadosHistorial : this.estadosActivos;
    return estados.some((estado) => estado.value === solicitud.estado_solicitud);
  }

  private esEstadoHistorial(estado: string): boolean {
    return this.estadosHistorial.some((item) => item.value === estado);
  }

  private normalizar(valor?: string): string {
    return (valor || '').toLowerCase().trim();
  }

  private fechaEntregaNormalizada(solicitud: Solicitud): string {
    return (solicitud.programacionEntrega?.fecha_entrega || '').slice(0, 10);
  }

  private mensajeError(error: any, fallback: string): string {
    return error?.error?.mensaje || fallback;
  }
}
