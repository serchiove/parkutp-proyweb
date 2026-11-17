import { spawnSync } from 'node:child_process';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { crearServidor } from './serve-ricardo.mjs';

const cwd = dirname(fileURLToPath(import.meta.url));
const build = spawnSync(process.execPath, [
  'node_modules/@angular/cli/bin/ng.js', 'build', '--configuration', 'development',
  '--browser', 'src/main.ricardo.ts', '--output-path', 'dist/ricardo',
], { cwd, stdio: 'inherit', env: { ...process.env, NG_CLI_ANALYTICS: 'false' } });
if (build.status !== 0) process.exit(build.status ?? 1);
const root = resolve(cwd, 'dist/ricardo/browser');
crearServidor(root).listen(4200, 'localhost', () => console.log('Revisión Ricardo: http://localhost:4200/operacion (Ctrl+C para detener). API real: localhost:8081.'));
