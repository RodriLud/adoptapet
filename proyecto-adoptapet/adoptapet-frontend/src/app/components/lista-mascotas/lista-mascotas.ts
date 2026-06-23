import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MascotaService } from '../../services/mascota.service';
import { Mascota } from '../../models/mascota';
import { AuthService } from '../../services/auth.service';
import { Sidebar } from '../shared/sidebar/sidebar';
import { FeedbackService } from '../../services/feedback.service';

@Component({
  selector: 'app-lista-mascotas',
  standalone: true,
  imports: [CommonModule, FormsModule, Sidebar],
  styleUrls: ['./lista-mascotas.css'],
  templateUrl: './lista-mascotas.html',
})
export class ListaMascotas implements OnInit {
  mascotas: Mascota[] = [];
  mascotaEditada: Mascota | null = null;
  archivoSeleccionado: File | null = null;
  loading = false;
  guardando = false;
  mascotaProcesandoId: number | null = null;
  rol = '';
  esNuevo = false;
  busqueda = '';
  filtroAdopcion = 'OPERATIVAS';
  filtroSalud = '';
  paginaActual = 1;
  elementosPorPagina = 10;

  estadosSalud = ['SIN NOVEDADES', 'EN TRATAMIENTO', 'CRITICO'];
  estadosAdopcion = ['DISPONIBLE', 'ADOPTADO', 'RESERVADO', 'INACTIVO'];
  sexos = ['MACHO', 'HEMBRA'];
  estadosEsterilizacion = ['ESTERILIZADO', 'NO ESTERILIZADO', 'PENDIENTE', 'NO APLICA'];
  filtrosAdopcion = [
    { label: 'Operativas', value: 'OPERATIVAS' },
    { label: 'Disponibles', value: 'DISPONIBLE' },
    { label: 'Reservadas', value: 'RESERVADO' },
    { label: 'Adoptadas', value: 'ADOPTADO' },
    { label: 'Inactivas', value: 'INACTIVO' },
    { label: 'Todas', value: '' },
  ];

  constructor(
    private mascotaService: MascotaService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef,
    private feedback: FeedbackService,
  ) {}

  ngOnInit() {
    this.rol = this.authService.getUserRole();
    this.cargarMascotas();
  }

  cargarMascotas(): void {
    this.loading = true;
    this.mascotaService.listarMascotas().subscribe({
      next: (data) => {
        this.mascotas = data;
        this.ajustarPagina();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  abrirRegistrador() {
    this.esNuevo = true;
    this.archivoSeleccionado = null;
    this.mascotaEditada = {
      nombre: '',
      especie: 'Perro',
      raza: '',
      sexo: 'MACHO',
      edad_anios: 0,
      edad_meses: 0,
      est_salud: 'SIN NOVEDADES',
      est_adopcion: 'DISPONIBLE',
      estado_esterilizacion: 'PENDIENTE',
      observaciones: '',
    };
  }

  abrirEditor(mascota: Mascota) {
    this.esNuevo = false;
    this.archivoSeleccionado = null;
    this.mascotaEditada = {
      ...mascota,
      sexo: mascota.sexo || 'MACHO',
      estado_esterilizacion: mascota.estado_esterilizacion || 'PENDIENTE',
      observaciones: mascota.observaciones || '',
    };
  }

  cerrarEditor() {
    this.mascotaEditada = null;
    this.archivoSeleccionado = null;
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.archivoSeleccionado = input.files?.[0] || null;
  }

  guardar() {
    if (!this.mascotaEditada) {
      return;
    }
    if (!this.formularioMascotaValido()) {
      this.feedback.warning('Datos incompletos', 'Completa nombre, especie, raza y edad valida.');
      return;
    }

    this.guardando = true;
    this.mascotaService.guardarMascota(this.mascotaEditada, this.archivoSeleccionado).subscribe({
      next: () => {
        this.cargarMascotas();
        this.cerrarEditor();
        this.feedback.success(this.esNuevo ? 'Mascota registrada' : 'Mascota actualizada');
      },
      error: (error) => {
        this.guardando = false;
        this.feedback.error(this.mensajeError(error, 'No se pudo guardar la mascota'));
        this.cdr.detectChanges();
      },
      complete: () => {
        this.guardando = false;
        this.cdr.detectChanges();
      },
    });
  }

  async eliminar(mascota: Mascota) {
    if (!mascota.id_mascota) {
      return;
    }
    const confirmado = await this.feedback.confirm({
      title: 'Dar de baja mascota',
      message: `La mascota ${mascota.nombre} pasara a estado inactivo y dejara de estar disponible para adopcion.`,
      confirmText: 'Dar de baja',
      tone: 'danger',
    });
    if (!confirmado) {
      return;
    }

    this.mascotaProcesandoId = mascota.id_mascota;
    this.mascotaService.eliminarMascota(mascota.id_mascota, mascota.version).subscribe({
      next: () => {
        this.cargarMascotas();
        this.feedback.success('Mascota dada de baja');
      },
      error: (error) => {
        this.mascotaProcesandoId = null;
        this.feedback.error(this.mensajeError(error, 'No se pudo dar de baja la mascota'));
        this.cdr.detectChanges();
      },
      complete: () => {
        this.mascotaProcesandoId = null;
        this.cdr.detectChanges();
      },
    });
  }

  imagenUrl(mascota: Mascota): string {
    return mascota.ruta_imagen
      ? `http://localhost:8080/fotos_mascotas/${mascota.ruta_imagen}`
      : 'assets/img/fondologin.jpg';
  }

  cambiarFiltroAdopcion(estado: string) {
    this.filtroAdopcion = estado;
    this.paginaActual = 1;
  }

  cambiarPagina(pagina: number) {
    if (pagina < 1 || pagina > this.totalPaginas()) {
      return;
    }
    this.paginaActual = pagina;
  }

  alCambiarBusqueda() {
    this.paginaActual = 1;
  }

  mascotasFiltradas(): Mascota[] {
    const texto = this.normalizar(this.busqueda);
    return this.mascotas.filter((mascota) => {
      const coincideTexto = !texto || [
        mascota.nombre,
        mascota.raza,
        mascota.especie,
        mascota.sexo,
        mascota.estado_esterilizacion,
      ].some((valor) => this.normalizar(valor).includes(texto));
      const coincideAdopcion = this.coincideFiltroAdopcion(mascota);
      const coincideSalud = !this.filtroSalud || mascota.est_salud === this.filtroSalud;
      return coincideTexto && coincideAdopcion && coincideSalud;
    });
  }

  mascotasPaginadas(): Mascota[] {
    const inicio = (this.paginaActual - 1) * this.elementosPorPagina;
    return this.mascotasFiltradas().slice(inicio, inicio + this.elementosPorPagina);
  }

  totalPaginas(): number {
    return Math.max(1, Math.ceil(this.mascotasFiltradas().length / this.elementosPorPagina));
  }

  paginas(): number[] {
    return Array.from({ length: this.totalPaginas() }, (_, index) => index + 1);
  }

  rangoActual(): string {
    const total = this.mascotasFiltradas().length;
    if (total === 0) {
      return '0 de 0';
    }
    const inicio = (this.paginaActual - 1) * this.elementosPorPagina + 1;
    const fin = Math.min(this.paginaActual * this.elementosPorPagina, total);
    return `${inicio}-${fin} de ${total}`;
  }

  puedeDarBaja(mascota: Mascota): boolean {
    return mascota.est_adopcion === 'DISPONIBLE';
  }

  puedeEditarEstadoAdopcion(): boolean {
    return this.esNuevo || this.mascotaEditada?.est_adopcion === 'DISPONIBLE' || this.mascotaEditada?.est_adopcion === 'INACTIVO';
  }

  textoAccionMascota(mascota: Mascota): string {
    if (mascota.est_adopcion === 'ADOPTADO') {
      return 'Historial';
    }
    if (mascota.est_adopcion === 'RESERVADO') {
      return 'Detalle';
    }
    return 'Editar';
  }

  disponibles(): number {
    return this.mascotas.filter((mascota) => mascota.est_adopcion === 'DISPONIBLE').length;
  }

  statusClass(valor?: string): string {
    const normalizado = (valor || '')
      .toLowerCase()
      .replaceAll(' ', '-')
      .replaceAll('_', '-');
    return normalizado ? `status-${normalizado}` : 'status-muted';
  }

  formularioMascotaValido(): boolean {
    const mascota = this.mascotaEditada;
    if (!mascota) {
      return false;
    }
    return !!mascota.nombre?.trim()
      && !!mascota.especie?.trim()
      && !!mascota.raza?.trim()
      && Number(mascota.edad_anios) >= 0
      && Number(mascota.edad_meses) >= 0
      && Number(mascota.edad_meses) <= 11;
  }

  private ajustarPagina() {
    this.paginaActual = Math.min(this.paginaActual, this.totalPaginas());
  }

  private coincideFiltroAdopcion(mascota: Mascota): boolean {
    if (this.filtroAdopcion === 'OPERATIVAS') {
      return ['DISPONIBLE', 'RESERVADO'].includes(mascota.est_adopcion);
    }
    return !this.filtroAdopcion || mascota.est_adopcion === this.filtroAdopcion;
  }

  private normalizar(valor?: string): string {
    return (valor || '').toLowerCase().trim();
  }

  private mensajeError(error: any, fallback: string): string {
    return error?.error?.mensaje || fallback;
  }
}
