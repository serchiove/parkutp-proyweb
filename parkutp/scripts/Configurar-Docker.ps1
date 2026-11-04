$ErrorActionPreference = 'Stop'
$repositoryRoot = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
$configurationPath = Join-Path $repositoryRoot '.env'
if (Test-Path -LiteralPath $configurationPath) {
    throw 'Ya existe .env. Se conserva su configuración; edítalo si necesitas cambiarla.'
}
function New-RandomKey {
    $keyBytes = New-Object byte[] 48
    $generator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $generator.GetBytes($keyBytes) } finally { $generator.Dispose() }
    return [Convert]::ToBase64String($keyBytes)
}
$securePassword = Read-Host 'Elige la contraseña inicial de admin, operador y consulta' -AsSecureString
$webPassword = [System.Net.NetworkCredential]::new('', $securePassword).Password
if ([string]::IsNullOrWhiteSpace($webPassword) -or $webPassword.Contains("`n") -or $webPassword.Contains("`r")) {
    throw 'La contraseña no puede estar vacía ni contener saltos de línea.'
}
$escapedPassword = $webPassword.Replace("'", "\'")
$configuration = @(
    'WEB_PORT=8081'
    "DB_PASSWORD='$(New-RandomKey)'"
    "JWT_SECRET='$(New-RandomKey)'"
    "DEMO_PASSWORD='$escapedPassword'"
    'CORS_ORIGENES=http://localhost:4200'
) -join "`n"
[System.IO.File]::WriteAllText($configurationPath, $configuration + "`n", [System.Text.UTF8Encoding]::new($false))
$webPassword = $null
$configuration = $null
Write-Host 'Configuración creada en .env. Inicia desde la raíz con: docker compose up -d --build'
