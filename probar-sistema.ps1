# Prueba automatica de todo el sistema. Uso (desde la carpeta del proyecto):
#   powershell -ExecutionPolicy Bypass -File .\probar-sistema.ps1
#   powershell -ExecutionPolicy Bypass -File .\probar-sistema.ps1 -Aislamiento
# Con -Aislamiento tambien apaga Inventario y comprueba que Citas sigue funcionando.
param([switch]$Aislamiento)

$B = "http://localhost:8000"
$ok = 0; $fail = 0

function Check($nombre, $condicion, $detalle = "") {
    if ($condicion) { Write-Host "  [OK]    $nombre" -ForegroundColor Green; $script:ok++ }
    else { Write-Host "  [FALLO] $nombre $detalle" -ForegroundColor Red; $script:fail++ }
}

function Llamar($metodo, $url, $body = $null, $token = $null) {
    $h = @{}
    if ($token) { $h["Authorization"] = "Bearer $token" }
    try {
        if ($body) { $r = Invoke-WebRequest -Uri $url -Method $metodo -Headers $h -Body $body -ContentType "application/json" -UseBasicParsing -TimeoutSec 30 }
        else { $r = Invoke-WebRequest -Uri $url -Method $metodo -Headers $h -UseBasicParsing -TimeoutSec 30 }
        return @{ Status = [int]$r.StatusCode; Body = $r.Content }
    } catch {
        $resp = $_.Exception.Response
        if ($resp) {
            $sr = New-Object IO.StreamReader($resp.GetResponseStream())
            return @{ Status = [int]$resp.StatusCode; Body = $sr.ReadToEnd() }
        }
        return @{ Status = 0; Body = $_.Exception.Message }
    }
}

function Login($user, $pass, $canal) {
    $cuerpo = (@{ username = $user; password = $pass; canal = $canal } | ConvertTo-Json)
    return Llamar "POST" "$B/api/auth/login" $cuerpo
}

Write-Host "`n=== 1. Contenedores ===" -ForegroundColor Cyan
$esperados = "vet-db-autenticacion","vet-db-citas","vet-db-inventario","vet-auth-service","vet-citas-service","vet-inventario-service","vet-api-gateway","vet-prometheus","vet-grafana","Jaeger-OpenTe","vet-adminer","vet-pg-exporter-autenticacion","vet-pg-exporter-citas","vet-pg-exporter-inventario"
$corriendo = docker ps --format "{{.Names}}"
foreach ($c in $esperados) { Check "Contenedor $c" ($corriendo -contains $c) }

Write-Host "`n=== 2. Autenticacion ===" -ForegroundColor Cyan
$r = Login "recepcion1" "Vet12345" "RECEPCION"
Check "Login correcto responde 200" ($r.Status -eq 200) "(status $($r.Status): $($r.Body))"
$token = $null
if ($r.Status -eq 200) { $token = ($r.Body | ConvertFrom-Json).token }
Check "El login devuelve un token JWT" ($token -and $token.Split(".").Count -eq 3)
$r = Login "recepcion1" "clave_mala" "RECEPCION"
Check "Contrasena incorrecta responde 401" ($r.Status -eq 401) "(status $($r.Status))"
$r = Llamar "GET" "$B/api/citas"
Check "Peticion sin token responde 401" ($r.Status -eq 401) "(status $($r.Status))"

Write-Host "`n=== 3. Citas ===" -ForegroundColor Cyan
$r = Llamar "GET" "$B/api/citas" $null $token
Check "Listar citas responde 200" ($r.Status -eq 200) "(status $($r.Status))"
$fecha = (Get-Date).AddDays((Get-Random -Minimum 60 -Maximum 700)).ToString("yyyy-MM-dd")
$cita = (@{ mascotaId = 1; propietarioId = 1; veterinarioId = 1; fechaCita = $fecha; horaInicio = "10:00:00"; horaFin = "10:30:00"; motivo = "Prueba automatica" } | ConvertTo-Json)
$r = Llamar "POST" "$B/api/citas" $cita $token
Check "Crear cita responde 201" ($r.Status -eq 201) "(status $($r.Status))"
$r = Llamar "POST" "$B/api/citas" $cita $token
Check "Misma cita otra vez responde 409 (solapamiento)" ($r.Status -eq 409) "(status $($r.Status))"

Write-Host "`n=== 4. Inventario ===" -ForegroundColor Cyan
$r = Llamar "GET" "$B/api/productos" $null $token
Check "Listar productos responde 200" ($r.Status -eq 200) "(status $($r.Status))"
$mov = (@{ productoId = 1; tipoMovimiento = "ENTRADA_COMPRA"; cantidad = 1; observacion = "Prueba automatica" } | ConvertTo-Json)
$r = Llamar "POST" "$B/api/movimientos" $mov $token
Check "Registrar entrada de stock responde 201" ($r.Status -eq 201) "(status $($r.Status))"
$mov = (@{ productoId = 1; tipoMovimiento = "SALIDA_CONSUMO"; cantidad = 999999 } | ConvertTo-Json)
$r = Llamar "POST" "$B/api/movimientos" $mov $token
Check "Salida mayor al stock responde 409" ($r.Status -eq 409) "(status $($r.Status))"
$r = Llamar "GET" "$B/api/alertas" $null $token
Check "Listar alertas responde 200" ($r.Status -eq 200) "(status $($r.Status))"

Write-Host "`n=== 5. Bloqueo de cuenta (3 intentos fallidos) ===" -ForegroundColor Cyan
1..3 | ForEach-Object { $null = Login "vet.gomez" "mala" "VETERINARIO" }
$r = Login "vet.gomez" "Vet12345" "VETERINARIO"
Check "4to intento (clave correcta) responde 423 cuenta bloqueada" ($r.Status -eq 423) "(status $($r.Status))"
docker exec vet-db-autenticacion psql -U postgres -d autenticacion -qc "UPDATE usuarios SET estado='ACTIVO', intentos_fallidos=0 WHERE username='vet.gomez';" | Out-Null
$r = Login "vet.gomez" "Vet12345" "VETERINARIO"
Check "Cuenta desbloqueada de nuevo (limpieza) responde 200" ($r.Status -eq 200) "(status $($r.Status))"

Write-Host "`n=== 6. Documentacion Swagger ===" -ForegroundColor Cyan
foreach ($s in "auth","citas","inventario") {
    $r = Llamar "GET" "$B/api-docs/$s"
    Check "Especificacion OpenAPI de $s" ($r.Status -eq 200) "(status $($r.Status))"
}

Write-Host "`n=== 7. Monitoreo ===" -ForegroundColor Cyan
$r = Llamar "GET" "http://localhost:9090/api/v1/targets"
if ($r.Status -eq 200) {
    $arriba = ([regex]::Matches($r.Body, '"health":"up"')).Count
    Check "Prometheus ve los 7 objetivos en UP" ($arriba -ge 7) "(UP: $arriba)"
} else { Check "Prometheus responde" $false "(status $($r.Status))" }
$r = Llamar "GET" "http://localhost:3000/api/health"
Check "Grafana responde 200" ($r.Status -eq 200) "(status $($r.Status))"
$r = Llamar "GET" "http://localhost:16686/api/services"
Check "Jaeger-OpenTe recibe trazas de api-gateway" ($r.Status -eq 200 -and $r.Body -match "api-gateway") "(status $($r.Status))"

if ($Aislamiento) {
    Write-Host "`n=== 8. Aislamiento de fallos ===" -ForegroundColor Cyan
    docker stop vet-inventario-service vet-db-inventario | Out-Null
    Start-Sleep -Seconds 3
    $r = Llamar "GET" "$B/api/citas" $null $token
    Check "Con Inventario apagado, Citas sigue respondiendo 200" ($r.Status -eq 200) "(status $($r.Status))"
    $r = Login "recepcion1" "Vet12345" "RECEPCION"
    Check "Con Inventario apagado, Autenticacion sigue respondiendo 200" ($r.Status -eq 200) "(status $($r.Status))"
    $r = Llamar "GET" "$B/api/productos" $null $token
    Check "Inventario apagado: sus peticiones fallan (esperado)" ($r.Status -ge 500 -or $r.Status -eq 0) "(status $($r.Status))"
    docker start vet-db-inventario vet-inventario-service | Out-Null
    Write-Host "  Inventario encendido otra vez; reiniciando el gateway..." -ForegroundColor Yellow
    Start-Sleep -Seconds 25
    docker restart vet-api-gateway | Out-Null
    Start-Sleep -Seconds 15
    $r = Llamar "GET" "$B/api/productos" $null $token
    Check "Inventario restaurado responde 200" ($r.Status -eq 200) "(status $($r.Status))"
}

Write-Host "`n=== RESULTADO: $ok correctas, $fail con fallo ===" -ForegroundColor $(if ($fail -eq 0) { "Green" } else { "Red" })
