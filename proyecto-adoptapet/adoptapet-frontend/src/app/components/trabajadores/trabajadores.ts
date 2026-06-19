import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { TrabajadorService } from '../../services/trabajador.service';
import { Sidebar } from '../shared/sidebar/sidebar';
import { FeedbackService } from '../../services/feedback.service';

@Component({
  selector: 'app-trabajadores',
  standalone: true,
  imports: [CommonModule, FormsModule, Sidebar],
  templateUrl: './trabajadores.html',
  styleUrls: ['./trabajadores.css'],
})
export class Trabajadores implements OnInit {
  trabajadores: any[] = [];
  trabajadorEditado: any = null;
  esNuevo = false;
  rol = '';
  currentUserId: number | null = null;
  guardando = false;
  trabajadorProcesandoId: number | null = null;
  busqueda = '';
  filtroEstado = '';
  paginaActual = 1;
  elementosPorPagina = 10;

  constructor(
    private authService: AuthService,
    private trabajadorService: TrabajadorService,
    private cdr: ChangeDetectorRef,
    private feedback: FeedbackService,
  ) {}

  ngOnInit(): void {
    const user = this.authService.getCurrentUser();
    this.rol = user?.rol || '';
    this.currentUserId = user?.id_usuario ?? null;
    this.cargar();
  }

  cargar() {
    this.trabajadorService.listar().subscribe((data) => {
      this.trabajadores = data;
      this.ajustarPagina();
      this.cdr.detectChanges();
    });
  }

  nuevo() {
    this.esNuevo = true;
    this.trabajadorEditado = {
      username: '',
      password: '',
      nom_trabajador: '',
      ape_trabajador: '',
      dni: '',
      fec_nacimiento: '',
      email: '',
      telefono: '',
    };
  }

  editar(trabajador: any) {
    this.esNuevo = false;
    this.trabajadorEditado = { ...trabajador, password: '' };
  }

  cerrar() {
    this.trabajadorEditado = null;
  }

  guardar() {
    if (!this.formularioTrabajadorValido()) {
      this.feedback.warning('Datos incompletos', 'Completa los datos obligatorios del trabajador.');
      return;
    }
    const request = this.esNuevo
      ? this.trabajadorService.registrar(this.trabajadorEditado)
      : this.trabajadorService.actualizar(this.trabajadorEditado.id_usuario, this.trabajadorEditado);

    this.guardando = true;
    request.subscribe({
      next: () => {
        this.cargar();
        this.cerrar();
        this.feedback.success(this.esNuevo ? 'Trabajador registrado' : 'Trabajador actualizado');
      },
      error: () => {
        this.guardando = false;
        this.feedback.error('No se pudo guardar el trabajador');
        this.cdr.detectChanges();
      },
      complete: () => {
        this.guardando = false;
        this.cdr.detectChanges();
      },
    });
  }

  async eliminar(trabajador: any) {
    if (!this.puedeDesactivar(trabajador)) {
      this.feedback.warning('Accion no permitida', 'No puedes desactivar el administrador principal ni tu propia sesion.');
      return;
    }
    const confirmado = await this.feedback.confirm({
      title: 'Desactivar trabajador',
      message: `${trabajador.username} perdera acceso operativo al portal.`,
      confirmText: 'Desactivar',
      tone: 'danger',
    });
    if (!confirmado) {
      return;
    }
    this.trabajadorProcesandoId = trabajador.id_usuario;
    this.trabajadorService.eliminar(trabajador.id_usuario).subscribe({
      next: () => {
        this.cargar();
        this.feedback.success('Trabajador desactivado');
      },
      error: () => {
        this.trabajadorProcesandoId = null;
        this.feedback.error('No se pudo desactivar el trabajador');
        this.cdr.detectChanges();
      },
      complete: () => {
        this.trabajadorProcesandoId = null;
        this.cdr.detectChanges();
      },
    });
  }

  async activar(trabajador: any) {
    const confirmado = await this.feedback.confirm({
      title: 'Activar trabajador',
      message: `${trabajador.username} recuperara acceso al portal.`,
      confirmText: 'Activar',
      tone: 'primary',
    });
    if (!confirmado) {
      return;
    }
    this.trabajadorProcesandoId = trabajador.id_usuario;
    this.trabajadorService.activar(trabajador.id_usuario).subscribe({
      next: () => {
        this.cargar();
        this.feedback.success('Trabajador activado');
      },
      error: () => {
        this.trabajadorProcesandoId = null;
        this.feedback.error('No se pudo activar el trabajador');
        this.cdr.detectChanges();
      },
      complete: () => {
        this.trabajadorProcesandoId = null;
        this.cdr.detectChanges();
      },
    });
  }

  puedeDesactivar(trabajador: any): boolean {
    const esUsuarioActual = this.currentUserId !== null && trabajador.id_usuario === this.currentUserId;
    const esPrimerAdmin = trabajador.rol === 'ROLE_ADMIN' && this.esPrimerAdministrador(trabajador);
    return trabajador.activo !== false && !esUsuarioActual && !esPrimerAdmin;
  }

  alCambiarFiltro() {
    this.paginaActual = 1;
  }

  trabajadoresFiltrados(): any[] {
    const texto = this.normalizar(this.busqueda);
    return this.trabajadores.filter((trabajador) => {
      const coincideTexto = !texto || [
        trabajador.nom_trabajador,
        trabajador.ape_trabajador,
        trabajador.username,
        trabajador.dni,
        trabajador.email,
        trabajador.telefono,
      ].some((valor) => this.normalizar(valor).includes(texto));
      const coincideEstado = !this.filtroEstado
        || (this.filtroEstado === 'ACTIVO' && trabajador.activo !== false)
        || (this.filtroEstado === 'INACTIVO' && trabajador.activo === false);
      return coincideTexto && coincideEstado;
    });
  }

  trabajadoresPaginados(): any[] {
    const inicio = (this.paginaActual - 1) * this.elementosPorPagina;
    return this.trabajadoresFiltrados().slice(inicio, inicio + this.elementosPorPagina);
  }

  totalPaginas(): number {
    return Math.max(1, Math.ceil(this.trabajadoresFiltrados().length / this.elementosPorPagina));
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
    const total = this.trabajadoresFiltrados().length;
    if (total === 0) {
      return '0 de 0';
    }
    const inicio = (this.paginaActual - 1) * this.elementosPorPagina + 1;
    const fin = Math.min(this.paginaActual * this.elementosPorPagina, total);
    return `${inicio}-${fin} de ${total}`;
  }

  activos(): number {
    return this.trabajadores.filter((trabajador) => trabajador.activo !== false).length;
  }

  formularioTrabajadorValido(): boolean {
    const trabajador = this.trabajadorEditado;
    if (!trabajador) {
      return false;
    }
    return (!this.esNuevo || (!!trabajador.username?.trim() && !!trabajador.password?.trim()))
      && !!trabajador.nom_trabajador?.trim()
      && !!trabajador.ape_trabajador?.trim()
      && !!trabajador.dni?.trim()
      && !!trabajador.email?.trim()
      && !!trabajador.telefono?.trim();
  }

  private esPrimerAdministrador(trabajador: any): boolean {
    const admins = this.trabajadores
      .filter((item) => item.rol === 'ROLE_ADMIN')
      .sort((a, b) => (a.id_usuario ?? 0) - (b.id_usuario ?? 0));
    return admins.length > 0 && admins[0].id_usuario === trabajador.id_usuario;
  }

  private ajustarPagina() {
    this.paginaActual = Math.min(this.paginaActual, this.totalPaginas());
  }

  private normalizar(valor?: string): string {
    return (valor || '').toLowerCase().trim();
  }
}
