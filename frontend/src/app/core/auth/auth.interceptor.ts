import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { API_BASE_URL } from '../api/api-config';
import { AuthService } from './auth.service';
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const isLoginRequest = request.url === `${API_BASE_URL}/auth/login`;
  const token = auth.session()?.token;
  const securedRequest = token && request.url.startsWith(API_BASE_URL) && !isLoginRequest
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : request;
  return next(securedRequest).pipe(catchError((error: unknown) => {
    if (error instanceof HttpErrorResponse && error.status === 401 && !isLoginRequest) {
      auth.clearSession();
      void router.navigate(['/login'], { queryParams: { expired: 'true' } });
    }
    return throwError(() => error);
  }));
};
