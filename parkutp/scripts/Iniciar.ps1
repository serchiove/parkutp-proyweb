param([switch]$DemoLocal, [string]$PgBin)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
Set-Location -LiteralPath $projectRoot
if (-not $env:JAVA_HOME) {
 $jdkCandidate = Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Directory -ErrorAction SilentlyContinue | Where-Object Name -Like 'jdk-21*' | Select-Object -First 1
 if ($jdkCandidate) { $env:JAVA_HOME = $jdkCandidate.FullName }
}
if (-not $env:JAVA_HOME -or -not (Test-Path -LiteralPath "$env:JAVA_HOME\bin\javac.exe")) { throw 'Configura JAVA_HOME con un JDK 21 completo.' }
if ($DemoLocal) {
 if (-not $PgBin) {
  $pgCandidate = Get-ChildItem 'C:\Program Files\PostgreSQL' -Directory -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
  if ($pgCandidate) { $PgBin = Join-Path $pgCandidate.FullName 'bin' }
 }
 if (-not $PgBin -or -not (Test-Path -LiteralPath (Join-Path $PgBin 'initdb.exe'))) { throw 'Instala PostgreSQL o indica -PgBin con la carpeta bin.' }
 $localData = Join-Path $projectRoot '.local-data'
 $logPath = Join-Path $projectRoot '.local-postgres.log'
 if (-not (Test-Path -LiteralPath (Join-Path $localData 'PG_VERSION'))) {
  & (Join-Path $PgBin 'initdb.exe') -D $localData -U parkutp_local --auth=trust --encoding=UTF8 --locale=C
  if ($LASTEXITCODE -ne 0) { throw 'No se pudo inicializar PostgreSQL.' }
 }
 & (Join-Path $PgBin 'pg_ctl.exe') -D $localData status *> $null
 if ($LASTEXITCODE -ne 0) {
  & (Join-Path $PgBin 'pg_ctl.exe') -D $localData -l $logPath -o '-p 55432 -h 127.0.0.1' -w start
  if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar PostgreSQL. Verifica que el puerto 55432 esté libre.' }
 }
 $dbExists = & (Join-Path $PgBin 'psql.exe') -h 127.0.0.1 -p 55432 -U parkutp_local -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='parkutp'"
 if ($dbExists -ne '1') {
  & (Join-Path $PgBin 'createdb.exe') -h 127.0.0.1 -p 55432 -U parkutp_local parkutp
  if ($LASTEXITCODE -ne 0) { throw 'No se pudo crear la base de datos.' }
 }
 $env:DB_URL = 'jdbc:postgresql://127.0.0.1:55432/parkutp'
 $env:DB_USER = 'parkutp_local'
 $env:DB_PASSWORD = ''
 Write-Host 'Demo local: PostgreSQL accesible solo desde esta computadora. Los datos quedan en .local-data.'
} elseif (-not $env:DB_URL -or -not $env:DB_USER) { throw 'Configura DB_URL, DB_USER y DB_PASSWORD, o utiliza -DemoLocal.' }
& "$env:JAVA_HOME\bin\java.exe" -version
& .\mvnw.cmd '-Dmaven.repo.local=.maven-cache' package
if ($LASTEXITCODE -ne 0) { throw 'La compilación falló.' }
Write-Host 'Abre http://localhost:8081 (o el puerto configurado en PORT). Ctrl+C detiene la aplicación.'
& "$env:JAVA_HOME\bin\java.exe" -jar target\parkutp-apf2.war
