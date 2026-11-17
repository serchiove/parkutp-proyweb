import { Component, input } from '@angular/core';
import { UpperCasePipe } from '@angular/common';
@Component({ imports: [UpperCasePipe], selector: 'app-module-placeholder', templateUrl: './module-placeholder.html', styleUrl: './module-placeholder.scss' })
export class ModulePlaceholderPage {
  readonly title = input('Módulo');
  readonly description = input('');
  readonly icon = input('P');
}
