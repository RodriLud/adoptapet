import { HttpInterceptorFn } from '@angular/common/http';
import { ApplicationRef, inject } from '@angular/core';
import { finalize } from 'rxjs/operators';

export const basicAuthInterceptor: HttpInterceptorFn = (req, next) => {
  const appRef = inject(ApplicationRef);

  const refreshView = () => {
    queueMicrotask(() => appRef.tick());
  };

  if (typeof window === 'undefined') {
    return next(req).pipe(finalize(refreshView));
  }

  const session = localStorage.getItem('user_session');
  if (!session || req.url.includes('/auth/login')) {
    return next(req).pipe(finalize(refreshView));
  }

  const user = JSON.parse(session);
  if (!user.username || !user.password) {
    return next(req).pipe(finalize(refreshView));
  }

  const authReq = req.clone({
    setHeaders: {
      Authorization: `Basic ${btoa(`${user.username}:${user.password}`)}`,
    },
  });

  return next(authReq).pipe(finalize(refreshView));
};
