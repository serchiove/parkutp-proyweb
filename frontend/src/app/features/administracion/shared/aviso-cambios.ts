import { Component, effect, ElementRef, inject, viewChild } from '@angular/core';
import { AvisoCambiosService } from './aviso-cambios.service';

@Component({
  selector: 'app-aviso-cambios',
  template: `
    <dialog #ventana aria-labelledby="aviso-titulo" aria-describedby="aviso-descripcion"
      (cancel)="$event.preventDefault(); aviso.responder(false)">
      <div class="contenido">
        <div class="icono" aria-hidden="true">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><path d="M12 8v5m0 3h.01M10.3 3.9 2.5 17.5A2 2 0 0 0 4.2 20h15.6a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0Z"/></svg>
        </div>
        <span class="etiqueta">ANTES DE SALIR</span>
        <h2 id="aviso-titulo">Tienes cambios sin guardar</h2>
        <p id="aviso-descripcion">Lo que escribiste se perderá si cambias de pantalla. Puedes quedarte para terminar y guardar tus cambios.</p>
        <div class="acciones">
          <button class="continuar" autofocus (click)="aviso.responder(false)">Seguir editando</button>
          <button class="salir" (click)="aviso.responder(true)">Salir sin guardar</button>
        </div>
      </div>
    </dialog>
  `,
  styles: `
    dialog { border: 1px solid #e8e8ec; border-radius: 22px; padding: 0; width: min(480px, calc(100vw - 32px)); color: #19191d; background: white; box-shadow: 0 24px 80px #0003; }
    dialog::backdrop { background: #14121c88; backdrop-filter: blur(4px); }
    .contenido { padding: 32px; font-family: var(--font-base); }
    .icono { width: 56px; height: 56px; display: grid; place-items: center; background: #fff0f3; color: #ec0035; border-radius: 17px; margin-bottom: 22px; }
    svg { width: 28px; height: 28px; }
    .etiqueta { color: #bd0030; font-size: 11px; font-weight: 700; letter-spacing: 1.4px; }
    h2 { font-size: 25px; line-height: 1.2; letter-spacing: -.6px; margin: 10px 0 12px; }
    p { color: #677181; font-size: 14px; line-height: 1.65; margin: 0 0 26px; }
    .acciones { display: flex; gap: 10px; }
    button { flex: 1; border-radius: 11px; padding: 14px 10px; font: 600 13px var(--font-base); cursor: pointer; }
    .continuar { background: #ec0035; color: white; border: 1px solid #ec0035; box-shadow: 0 5px 14px #ec003522; }
    .salir { background: white; color: #9f1735; border: 1px solid #e5cbd2; }
    button:hover { filter: brightness(.95); }
    button:focus-visible { outline: 3px solid #ff93ad; outline-offset: 3px; }
    @media(max-width: 400px) { .contenido { padding: 24px; } .acciones { flex-direction: column; } }
  `,
})
export class AvisoCambios {
  readonly aviso = inject(AvisoCambiosService);
  readonly ventana = viewChild<ElementRef<HTMLDialogElement>>('ventana');
  constructor() {
    effect(() => {
      const dialogo = this.ventana()?.nativeElement;
      if (!dialogo) return;
      if (this.aviso.abierto() && !dialogo.open) dialogo.showModal();
      if (!this.aviso.abierto() && dialogo.open) dialogo.close();
    });
  }
}
