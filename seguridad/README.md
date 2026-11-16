# Escaneo de vulnerabilidades con OWASP ZAP

Reportes del escaneo activo de los tres servicios a traves del API Gateway.

| Archivo | Contenido |
|---|---|
| `zap/antes-<servicio>.html / .json` | Primer escaneo (antes de las correcciones) |
| `zap/despues-<servicio>.html / .json` | Segundo escaneo (despues de las correcciones) |
| `zap/hook.py` | Hook de ZAP que agrega el token JWT (encabezado `Authorization`) a todas las peticiones |

## Como se ejecuto

Con el sistema levantado (`docker compose up -d`) y **un respaldo de las bases de datos** (el escaneo activo inserta datos y puede bloquear cuentas):

```bash
# 1. Descargar la especificacion y fijar la direccion absoluta del gateway (servers[0].url = http://api-gateway:8080)
curl -s http://localhost:8000/api-docs/citas -o spec-citas.json   # y editar "servers"

# 2. Token (usuario de prueba)
TOKEN=$(curl -s -X POST http://localhost:8000/api/auth/login -H "Content-Type: application/json" \
  -d '{"username":"caja1","password":"Vet12345","canal":"INVENTARIO_CAJA"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 3. Escaneo (carpeta con spec-citas.json y hook.py montada en /zap/wrk)
docker run --rm --network vetproyect_default -e ZAP_JWT="$TOKEN" -v "$PWD/zap:/zap/wrk/:rw" \
  ghcr.io/zaproxy/zaproxy:stable zap-api-scan.py -t /zap/wrk/spec-citas.json -f openapi \
  -r reporte-citas.html -J reporte-citas.json -I --hook=/zap/wrk/hook.py \
  -z "-config scanner.maxScanDurationInMins=6"
```

Resultado: ver la seccion 7 del documento del Avance 3.
