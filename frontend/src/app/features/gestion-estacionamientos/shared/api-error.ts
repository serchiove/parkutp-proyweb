import { HttpErrorResponse } from '@angular/common/http';

export function mensajeError(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) return 'Ocurrió un error inesperado. Intenta nuevamente.';
  if (error.status === 0) return 'No se pudo conectar con la API. Comprueba la conexión y vuelve a intentar.';
  if (error.status === 401) return 'Tu sesión venció. Inicia sesión nuevamente.';
  if (error.status === 403) return 'Tu usuario no tiene permiso para realizar esta acción.';
  const body = error.error;
  if (body && typeof body === 'object' && typeof body.mensaje === 'string') {
    const detalles = Array.isArray(body.errores) ? body.errores.filter((v: unknown) => typeof v === 'string') : [];
    return [body.mensaje, ...detalles].join('. ');
  }
  if (error.status === 404) return 'El registro ya no existe. Actualiza la información.';
  return 'No se pudo completar la solicitud. Intenta nuevamente.';
}
