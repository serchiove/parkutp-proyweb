$ErrorActionPreference = 'Stop'
$repo = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$config = Join-Path $repo '.env'
if (-not (Test-Path -LiteralPath $config)) {
    function Nueva-Clave {
        $bytes = New-Object byte[] 48
        $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
        try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
        [Convert]::ToBase64String($bytes)
    }
    $lines = @('WEB_PORT=8081', ('DB_PASSWORD=' + (Nueva-Clave)), ('JWT_SECRET=' + (Nueva-Clave)), ('DEMO_PASSWORD=ParkUTP-' + [guid]::NewGuid().ToString('N').Substring(0,12)), 'CORS_ORIGENES=http://localhost:4200')
    [System.IO.File]::WriteAllLines($config, $lines, [System.Text.UTF8Encoding]::new($false))
}
foreach ($line in Get-Content -LiteralPath $config) {
    if ($line -match '^(JWT_SECRET|DEMO_PASSWORD|CORS_ORIGENES)=(.*)$') {
        [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2].Trim().Trim([char]39).Trim([char]34), 'Process')
    }
}
if (-not $env:JWT_SECRET -or -not $env:DEMO_PASSWORD) { throw 'Faltan JWT_SECRET o DEMO_PASSWORD en .env.' }
Write-Host 'Usuarios: admin, operador y consulta. Contraseña: valor DEMO_PASSWORD del archivo .env local.'
Write-Host 'La base de demostración usa 127.0.0.1:55432 y conserva datos en parkutp/.local-data.'
Write-Host 'Para Angular, abre otra terminal en frontend y ejecuta: npm start'
& (Join-Path $repo 'parkutp/scripts/Iniciar.ps1') -DemoLocal
