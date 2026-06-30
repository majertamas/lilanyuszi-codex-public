import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs'
import {AuthService} from './auth-service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);

  const request = req.clone({
    withCredentials: true
  });

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      const isUnauthorized = error.status === 401;
      const isRefreshRequest = req.url.includes('/auth/refresh');
      const isLogoutRequest = req.url.includes('/auth/logout');

      if (!isUnauthorized || isRefreshRequest || isLogoutRequest) {
        return throwError(() => error);
      }

      return authService.refresh().pipe(
        switchMap(() => {
          const retryRequest = req.clone({
            withCredentials: true
          });

          return next(retryRequest);
        }),
        catchError(refreshError => {
          return throwError(() => refreshError);
        })
      );
    })
  );
};
