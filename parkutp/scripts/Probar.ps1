# TEST_DB_URL debe apuntar a una base dedicada llamada parkutp_test.
# La suite borra sus tablas antes de cada prueba; nunca apuntar a datos reales.
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath (Split-Path $PSScriptRoot -Parent)
if (-not $env:TEST_DB_URL -or -not $env:TEST_DB_USER) { throw 'Configura TEST_DB_URL, TEST_DB_USER y TEST_DB_PASSWORD para la base parkutp_test.' }
& .\mvnw.cmd '-Dmaven.repo.local=.maven-cache' test
if ($LASTEXITCODE -ne 0) { throw 'Falló la suite de pruebas.' }
