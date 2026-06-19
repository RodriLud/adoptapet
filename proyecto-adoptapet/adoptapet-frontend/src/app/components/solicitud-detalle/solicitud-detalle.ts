import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AprobarSolicitudPayload, HorarioDisponibilidad, SolicitudService } from '../../services/solicitud.service';
import { Solicitud } from '../../models/solicitud';
import { AuthService } from '../../services/auth.service';
import { Sidebar } from '../shared/sidebar/sidebar';
import { AdoptanteResumen } from '../../models/adoptante';
import { AdoptanteService } from '../../services/adoptante.service';
import { FeedbackService } from '../../services/feedback.service';

@Component({
  selector: 'app-solicitud-detalle',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, Sidebar],
  templateUrl: './solicitud-detalle.html',
  styleUrls: ['./solicitud-detalle.css'],
})
export class SolicitudDetalle implements OnInit {
  private readonly minutosToleranciaCierre = 30;
  private readonly maxReprogramaciones = 2;
  solicitud: Solicitud | null = null;
  loading = true;
  rol = '';
  modalAccion: 'aprobar' | 'rechazar' | 'cancelar' | 'reprogramar' | 'contingencia' | null = null;
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
  procesandoAccion = '';
  motivoForm = '';
  archivoActaFirmada: File | null = null;
  historialAdoptante: AdoptanteResumen | null = null;

  constructor(
    private route: ActivatedRoute,
    private solicitudService: SolicitudService,
    private adoptanteService: AdoptanteService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private feedback: FeedbackService,
  ) {}

  ngOnInit(): void {
    this.rol = this.authService.getUserRole();
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.solicitudService.buscarPorId(id).subscribe({
      next: (data) => {
        this.solicitud = data;
        this.cargarResumenAdoptante();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  aprobar() {
    if (!this.solicitud?.id_solicitud) {
      return;
    }
    this.abrirModalAprobar();
  }

  confirmarAprobacion() {
    if (!this.solicitud?.id_solicitud) {
      return;
    }
    const programacion = this.construirProgramacionEntrega();
    if (!programacion) {
      return;
    }
    this.procesandoAccion = 'aprobar';
    this.solicitudService.aprobar(this.solicitud.id_solicitud, programacion).subscribe({
      next: (data) => {
        this.solicitud = data;
        this.cerrarModal();
        this.feedback.success('Solicitud aprobada', 'La entrega quedo programada.');
        this.cdr.detectChanges();
      },
      error: (error) => this.finalizarProcesoConError(error, 'No se pudo aprobar la solicitud'),
      complete: () => {
        this.procesandoAccion = '';
        this.cdr.detectChanges();
      },
    });
  }

  confirmarReprogramacion() {
    if (!this.solicitud?.id_solicitud) {
      return;
    }
    const programacion = this.construirProgramacionEntrega();
    if (!programacion) {
      return;
    }
    this.procesandoAccion = 'reprogramar';
    this.solicitudService.reprogramar(this.solicitud.id_solicitud, programacion).subscribe({
      next: (data) => {
        this.solicitud = data;
        this.cerrarModal();
        this.feedback.success('Cita reprogramada');
        this.cdr.detectChanges();
      },
      error: (error) => this.finalizarProcesoConError(error, 'No se pudo reprogramar la cita'),
      complete: () => {
        this.procesandoAccion = '';
        this.cdr.detectChanges();
      },
    });
  }

  finalizarContingencia() {
    if (!this.solicitud?.id_solicitud) {
      return;
    }
    if (!this.puedeFinalizarContingencia()) {
      this.feedback.warning('Contingencia no disponible', 'La contingencia solo aplica para administradores, con acta generada, acta firmada y cita ya vencida.');
      return;
    }
    this.modalAccion = 'contingencia';
    this.motivoForm = '';
  }

  confirmarContingencia() {
    if (!this.solicitud?.id_solicitud) {
      return;
    }
    if (!this.motivoForm.trim()) {
      this.feedback.warning('Motivo requerido', 'Ingresa el motivo de contingencia.');
      return;
    }
    this.procesandoAccion = 'contingencia';
    this.solicitudService.finalizarPorContingencia(this.solicitud.id_solicitud, this.motivoForm.trim()).subscribe({
      next: (data) => {
        this.solicitud = data;
        this.cerrarModal();
        this.feedback.success('Solicitud finalizada por contingencia');
        this.cdr.detectChanges();
      },
      error: (error) => this.finalizarProcesoConError(error, 'No se pudo finalizar por contingencia'),
      complete: () => {
        this.procesandoAccion = '';
        this.cdr.detectChanges();
      },
    });
  }

  rechazar() {
    if (!this.solicitud?.id_solicitud) {
      return;
    }
    this.modalAccion = 'rechazar';
    this.motivoForm = '';
  }

  confirmarRechazo() {
    if (!this.solicitud?.id_solicitud) {
      return;
    }
    if (!this.motivoForm.trim()) {
      this.feedback.warning('Motivo requerido', 'Ingresa el motivo de rechazo.');
      return;
    }
    this.procesandoAccion = 'rechazar';
    this.solicitudService.rechazar(this.solicitud.id_solicitud, this.motivoForm.trim()).subscribe({
      next: (data) => {
        this.solicitud = data;
        this.cerrarModal();
        this.feedback.success('Solicitud rechazada');
        this.cdr.detectChanges();
      },
      error: (error) => this.finalizarProcesoConError(error, 'No se pudo rechazar la solicitud'),
      complete: () => {
        this.procesandoAccion = '';
        this.cdr.detectChanges();
      },
    });
  }

  generarDocumento() {
    if (!this.solicitud?.id_solicitud) {
      return;
    }
    this.procesandoAccion = 'documento';
    this.solicitudService.generarDocumento(this.solicitud.id_solicitud).subscribe({
      next: (documento) => {
        const url = URL.createObjectURL(documento);
        window.open(url, '_blank');
        if (this.solicitud?.id_solicitud) {
          this.solicitud.acta_pdf = `documentos/acta_adopcion_${this.solicitud.id_solicitud}.pdf`;
        }
        this.feedback.success('Acta generada', 'El documento se abrio en una nueva pestana.');
        setTimeout(() => URL.revokeObjectURL(url), 60_000);
      },
      error: (error) => this.finalizarProcesoConError(error, 'No se pudo generar el documento'),
      complete: () => {
        this.procesandoAccion = '';
        this.cdr.detectChanges();
      },
    });
  }

  seleccionarActaFirmada(event: Event) {
    const input = event.target as HTMLInputElement;
    this.archivoActaFirmada = input.files?.[0] || null;
  }

  subirActaFirmada() {
    if (!this.solicitud?.id_solicitud || !this.archivoActaFirmada) {
      this.feedback.warning('Archivo requerido', 'Selecciona el archivo del acta firmada.');
      return;
    }
    this.procesandoAccion = 'acta';
    this.solicitudService.subirActaFirmada(this.solicitud.id_solicitud, this.archivoActaFirmada).subscribe({
      next: (data) => {
        this.solicitud = data;
        this.archivoActaFirmada = null;
        this.feedback.success('Acta firmada subida');
        this.cdr.detectChanges();
      },
      error: (error) => this.finalizarProcesoConError(error, 'No se pudo subir el acta firmada'),
      complete: () => {
        this.procesandoAccion = '';
        this.cdr.detectChanges();
      },
    });
  }

  async finalizar() {
    if (!this.solicitud?.id_solicitud) {
      return;
    }
    if (!this.solicitud.ruta_acta_firmada) {
      this.feedback.warning('Acta firmada requerida', 'Sube el acta firmada antes de finalizar la adopcion.');
      return;
    }
    const confirmado = await this.feedback.confirm({
      title: 'Confirmar entrega',
      message: `${this.solicitud.mascota?.nombre || 'Esta mascota'} pasara a estado adoptado y la solicitud se cerrara como finalizada.`,
      confirmText: 'Confirmar entrega',
      tone: 'primary',
    });
    if (!confirmado) {
      return;
    }
    this.procesandoAccion = 'finalizar';
    this.solicitudService.finalizar(this.solicitud.id_solicitud).subscribe({
      next: (data) => {
        this.solicitud = data;
        this.feedback.success('Entrega confirmada');
        this.cdr.detectChanges();
      },
      error: (error) => this.finalizarProcesoConError(error, 'No se pudo finalizar la adopcion'),
      complete: () => {
        this.procesandoAccion = '';
        this.cdr.detectChanges();
      },
    });
  }

  async marcarNoAsistio() {
    if (!this.solicitud?.id_solicitud) {
      return;
    }
    const confirmado = await this.feedback.confirm({
      title: 'Marcar no asistencia',
      message: 'La mascota seguira reservada hasta reprogramar o cancelar definitivamente.',
      confirmText: 'Marcar no asistio',
      tone: 'warning',
    });
    if (!confirmado) {
      return;
    }
    this.procesandoAccion = 'no-asistio';
    this.solicitudService.marcarNoAsistio(this.solicitud.id_solicitud).subscribe({
      next: (data) => {
        this.solicitud = data;
        this.feedback.success('No asistencia registrada');
        this.cdr.detectChanges();
      },
      error: (error) => this.finalizarProcesoConError(error, 'No se pudo marcar como no asistio'),
      complete: () => {
        this.procesandoAccion = '';
        this.cdr.detectChanges();
      },
    });
  }

  reprogramar() {
    if (!this.solicitud?.id_solicitud) {
      return;
    }
    if (!this.puedeReprogramar()) {
      this.feedback.warning('Reprogramacion no disponible', 'Esta solicitud alcanzo el maximo de 2 reprogramaciones. Solo un administrador puede autorizar una excepcion.');
      return;
    }
    this.modalAccion = 'reprogramar';
    const opcion = this.proximaOpcionEntrega();
    this.programacionForm = {
      fecha_entrega: opcion.fecha,
      horario: `${opcion.inicio}|${opcion.fin}`,
      fecha_limite_recojo: this.fechaLimiteConTolerancia(opcion.fecha, opcion.fin),
      observacion: this.reprogramacionesAgotadas()
        ? 'Excepcion autorizada por administrador: '
        : this.solicitud.programacionEntrega?.observacion || '',
    };
    this.cargarDisponibilidadHorarios(true);
  }

  cancelar() {
    if (!this.solicitud?.id_solicitud) {
      return;
    }
    this.modalAccion = 'cancelar';
    this.motivoForm = '';
  }

  confirmarCancelacion() {
    if (!this.solicitud?.id_solicitud) {
      return;
    }
    if (!this.motivoForm.trim()) {
      this.feedback.warning('Motivo requerido', 'Ingresa el motivo de cancelacion.');
      return;
    }
    this.procesandoAccion = 'cancelar';
    this.solicitudService.cancelar(this.solicitud.id_solicitud, this.motivoForm.trim()).subscribe({
      next: (data) => {
        this.solicitud = data;
        this.cerrarModal();
        this.feedback.success('Solicitud cancelada');
        this.cdr.detectChanges();
      },
      error: (error) => this.finalizarProcesoConError(error, 'No se pudo cancelar la adopcion'),
      complete: () => {
        this.procesandoAccion = '';
        this.cdr.detectChanges();
      },
    });
  }

  abrirAdjunto(ruta?: string) {
    if (ruta) {
      window.open(`http://localhost:8080/uploads/${ruta}`, '_blank');
    }
  }

  cargarResumenAdoptante() {
    const idAdoptante = this.solicitud?.adoptante?.id_usuario;
    if (!idAdoptante) {
      this.historialAdoptante = null;
      return;
    }
    this.adoptanteService.buscarResumen(idAdoptante).subscribe({
      next: (data) => {
        this.historialAdoptante = data;
        this.cdr.detectChanges();
      },
      error: () => {
        this.historialAdoptante = null;
        this.cdr.detectChanges();
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
    this.procesandoAccion = '';
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

  statusClass(estado?: string): string {
    const normalizado = (estado || '').toLowerCase().replaceAll('_', '-');
    return normalizado ? `status-${normalizado}` : 'status-muted';
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
    if (this.reprogramacionesAgotadas() && this.esAdmin() && !this.programacionForm.observacion.trim()) {
      this.feedback.warning('Observacion requerida', 'La reprogramacion extra requiere una observacion del administrador.');
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

  formatHora(hora?: string): string {
    if (!hora) {
      return '';
    }
    const [hourText, minuteText] = hora.split(':');
    const hour = Number(hourText);
    const suffix = hour >= 12 ? 'p.m.' : 'a.m.';
    const displayHour = hour % 12 || 12;
    return `${displayHour}:${minuteText} ${suffix}`;
  }

  formatFechaHora(valor?: string): string {
    if (!valor) {
      return '';
    }
    const [fecha, hora] = valor.split('T');
    return `${fecha} ${this.formatHora(hora)}`;
  }

  puedeSubirActaFirmada(): boolean {
    const solicitud = this.solicitud;
    return solicitud?.estado_solicitud === 'APROBADA'
      && !!solicitud.acta_pdf
      && this.yaInicioCita();
  }

  puedeConfirmarEntrega(): boolean {
    const solicitud = this.solicitud;
    return solicitud?.estado_solicitud === 'APROBADA'
      && !!solicitud.ruta_acta_firmada
      && this.estaDentroVentanaCierre();
  }

  puedeFinalizarContingencia(): boolean {
    return this.motivoBloqueoContingencia() === '';
  }

  motivoBloqueoContingencia(): string {
    const solicitud = this.solicitud;
    if (!solicitud || solicitud.estado_solicitud !== 'APROBADA') {
      return 'La contingencia solo aplica a solicitudes aprobadas.';
    }
    if (!this.esAdmin()) {
      return 'Solo el administrador puede finalizar por contingencia.';
    }
    if (!solicitud.programacionEntrega) {
      return 'La solicitud no tiene una cita programada.';
    }
    if (!solicitud.acta_pdf) {
      return 'Debe existir un acta generada.';
    }
    if (!solicitud.ruta_acta_firmada) {
      return 'Debe existir un acta firmada subida.';
    }
    if (!this.yaInicioCita()) {
      return 'La cita todavia no inicia.';
    }
    if (this.estaDentroVentanaCierre()) {
      return 'La ventana normal sigue vigente; usa Confirmar entrega.';
    }
    return '';
  }

  mensajeContingenciaVisible(): string {
    if (!this.solicitud || this.solicitud.estado_solicitud !== 'APROBADA') {
      return '';
    }
    if (this.puedeFinalizarContingencia()) {
      return '';
    }
    return this.motivoBloqueoContingencia();
  }

  puedeReprogramar(): boolean {
    return !this.reprogramacionesAgotadas() || this.esAdmin();
  }

  reprogramacionesUsadas(): number {
    return this.solicitud?.cantidad_reprogramaciones || 0;
  }

  reprogramacionesAgotadas(): boolean {
    return this.reprogramacionesUsadas() >= this.maxReprogramaciones;
  }

  alertaGestion(): string {
    const solicitud = this.solicitud;
    if (!solicitud || solicitud.estado_solicitud !== 'APROBADA') {
      return '';
    }
    if (!this.yaInicioCita()) {
      return 'La cita aun no inicia. El cierre y la carga del acta firmada se habilitan desde el horario programado.';
    }
    if (!this.estaDentroVentanaCierre()) {
      if (this.esAdmin()) {
        return 'La ventana de cierre vencio. Puedes marcar no asistio, reprogramar, cancelar o finalizar por contingencia si la entrega si ocurrio.';
      }
      return 'La ventana de cierre vencio. Marca no asistio, reprograma o cancela segun lo ocurrido en la atencion.';
    }
    return '';
  }

  alertaReprogramaciones(): string {
    if (!this.solicitud || !['APROBADA', 'NO_ASISTIO'].includes(this.solicitud.estado_solicitud || '')) {
      return '';
    }
    if (this.reprogramacionesAgotadas()) {
      return this.esAdmin()
        ? 'Esta solicitud alcanzo 2 reprogramaciones. Una nueva fecha quedara como excepcion administrativa y requiere observacion.'
        : 'Esta solicitud alcanzo el maximo de 2 reprogramaciones. Debe cancelarse o ser evaluada por un administrador.';
    }
    return `Reprogramaciones usadas: ${this.reprogramacionesUsadas()} de ${this.maxReprogramaciones}.`;
  }

  mensajeBloqueoActaFirmada(): string {
    if (!this.solicitud?.acta_pdf) {
      return 'Genera el acta antes de subir la firmada.';
    }
    if (!this.yaInicioCita()) {
      return 'El acta firmada se sube desde el inicio de la cita presencial.';
    }
    return '';
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

  private yaInicioCita(): boolean {
    const programacion = this.solicitud?.programacionEntrega;
    if (!programacion?.fecha_entrega || !programacion.hora_inicio) {
      return false;
    }
    return new Date() >= this.fechaHoraProgramada(programacion.fecha_entrega, programacion.hora_inicio);
  }

  private estaDentroVentanaCierre(): boolean {
    const programacion = this.solicitud?.programacionEntrega;
    if (!this.yaInicioCita()) {
      return false;
    }
    if (!programacion?.fecha_limite_recojo) {
      return true;
    }
    return new Date() <= new Date(programacion.fecha_limite_recojo);
  }

  private esAdmin(): boolean {
    return this.rol === 'ROLE_ADMIN';
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

  private fechaHoraProgramada(fecha: string, hora: string): Date {
    const partes = hora.split(':');
    const horaNormalizada = `${partes[0] || '00'}:${partes[1] || '00'}:${partes[2] || '00'}`;
    return new Date(`${fecha}T${horaNormalizada}`);
  }

  private mensajeError(error: any, fallback: string): string {
    return error?.error?.mensaje || fallback;
  }

  private finalizarProcesoConError(error: any, fallback: string): void {
    this.procesandoAccion = '';
    this.feedback.error(this.mensajeError(error, fallback));
    this.cdr.detectChanges();
  }
}
