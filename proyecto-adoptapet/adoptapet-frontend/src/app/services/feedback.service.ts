import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ToastType = 'success' | 'error' | 'info' | 'warning';

export interface ToastMessage {
  id: number;
  type: ToastType;
  title: string;
  message?: string;
}

export interface ConfirmRequest {
  title: string;
  message: string;
  confirmText: string;
  cancelText: string;
  tone: 'primary' | 'danger' | 'warning';
  resolve: (accepted: boolean) => void;
}

@Injectable({ providedIn: 'root' })
export class FeedbackService {
  private nextToastId = 1;
  private readonly toastsSubject = new BehaviorSubject<ToastMessage[]>([]);
  private readonly confirmSubject = new BehaviorSubject<ConfirmRequest | null>(null);

  readonly toasts$ = this.toastsSubject.asObservable();
  readonly confirm$ = this.confirmSubject.asObservable();

  success(title: string, message?: string): void {
    this.toast('success', title, message);
  }

  error(title: string, message?: string): void {
    this.toast('error', title, message);
  }

  info(title: string, message?: string): void {
    this.toast('info', title, message);
  }

  warning(title: string, message?: string): void {
    this.toast('warning', title, message);
  }

  confirm(options: Partial<Omit<ConfirmRequest, 'resolve'>> & { title: string; message: string }): Promise<boolean> {
    return new Promise((resolve) => {
      this.confirmSubject.next({
        title: options.title,
        message: options.message,
        confirmText: options.confirmText || 'Confirmar',
        cancelText: options.cancelText || 'Cancelar',
        tone: options.tone || 'primary',
        resolve,
      });
    });
  }

  closeToast(id: number): void {
    this.toastsSubject.next(this.toastsSubject.value.filter((toast) => toast.id !== id));
  }

  answerConfirm(accepted: boolean): void {
    const current = this.confirmSubject.value;
    if (!current) {
      return;
    }
    this.confirmSubject.next(null);
    current.resolve(accepted);
  }

  private toast(type: ToastType, title: string, message?: string): void {
    const toast: ToastMessage = {
      id: this.nextToastId++,
      type,
      title,
      message,
    };
    this.toastsSubject.next([...this.toastsSubject.value, toast]);
    window.setTimeout(() => this.closeToast(toast.id), type === 'error' ? 6200 : 4200);
  }
}
