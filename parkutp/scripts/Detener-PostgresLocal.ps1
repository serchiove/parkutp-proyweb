param([string]$PgBin)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
if (-not $PgBin) {
 $pgCandidate = Get-ChildItem 'C:\Program Files\PostgreSQL' -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
 if ($pgCandidate) { $PgBin = Join-Path $pgCandidate.FullName 'bin' }
}
if (-not $PgBin) { throw 'Indica la carpeta bin de PostgreSQL con -PgBin.' }
& (Join-Path $PgBin 'pg_ctl.exe') -D (Join-Path $projectRoot '.local-data') -m fast -w stop
