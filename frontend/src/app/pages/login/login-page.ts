import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';

@Component({ imports: [ReactiveFormsModule], selector: 'app-login-page', styleUrl: './login-page.scss', templateUrl: './login-page.html' })
export class LoginPage {
  private readonly formBuilder = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  readonly loading = signal(false);
  readonly errorMessage = signal('');
  readonly passwordVisible = signal(false);
  readonly expired = this.route.snapshot.queryParamMap.has('expired');
  readonly form = this.formBuilder.nonNullable.group({
    username: ['', [Validators.required, Validators.maxLength(60)]],
    password: ['', [Validators.required, Validators.maxLength(72)]],
  });
  submit(): void {
    this.errorMessage.set('');
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.auth.login(this.form.getRawValue()).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: () => {
        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        const destination = returnUrl?.startsWith('/') && !returnUrl.startsWith('//') ? returnUrl : this.auth.homePath();
        void this.router.navigateByUrl(destination);
      },
      error: (error: unknown) => this.errorMessage.set(this.getErrorMessage(error)),
    });
  }
  private getErrorMessage(error: unknown): string {
    if (!(error instanceof HttpErrorResponse)) return 'No se pudo iniciar sesión. Intenta nuevamente.';
    if (error.status === 401) return 'Usuario o contraseña incorrectos.';
    if (error.status === 0) return 'No hay conexión con el servidor. Verifica que la API esté activa.';
    if (error.status === 400) return 'Revisa los datos ingresados e inténtalo de nuevo.';
    return 'Ocurrió un problema al iniciar sesión. Intenta nuevamente más tarde.';
  }
}
