# Clínica Veterinaria - Arquitectura SOA (Base de datos + Backend)

Implementación completa (base de datos + backend) de los procesos de
negocio seleccionados en el Avance 1 (sección 4): **Gestión de Citas**,
**Autenticación** y **Gestión de Inventario**. Cada proceso es un
**microservicio Spring Boot independiente, conectado SOLO a su propia base
de datos PostgreSQL**, detrás de un **API Gateway** único — igual que el
"Mapa de Arquitectura Inicial" de la sección 6 del documento.

## Estructura

```
docker-compose.yml
db-autenticacion/          -- scripts SQL: roles, usuarios, sesiones, log_accesos, notificaciones
db-citas/                  -- scripts SQL: pacientes/propietarios/veterinarios + citas
db-inventario/              -- scripts SQL: productos, movimientos (kárdex), alertas de stock

auth-service/               -- microservicio Spring Boot: login, bloqueo de cuenta, JWT
citas-service/               -- microservicio Spring Boot: citas, mascotas, propietarios, veterinarios
inventario-service/          -- microservicio Spring Boot: productos, movimientos, alertas
api-gateway/                 -- Spring Cloud Gateway: puerta de entrada única (puerto 8000)
```

## Arquitectura

```
Cliente (Postman / app / navegador)
        │
        ▼
  API Gateway (puerto 8000)
        │
   ┌────┼─────────────┬───────────────────┐
   ▼                  ▼                   ▼
auth-service      citas-service      inventario-service
(puerto 8081)     (puerto 8082)      (puerto 8083)
   │                  │                   │
   ▼                  ▼                   ▼
db-autenticacion   db-citas           db-inventario
(puerto 5433)      (puerto 5434)      (puerto 5435)
```

Cada microservicio tiene su **propia base de datos** y nunca se conecta
directamente a la de otro servicio. La comunicación entre servicios (por
ejemplo, para validar que un `usuario_id` existe) se haría vía HTTP entre
ellos, no vía SQL directo — así, si Inventario se cae, Citas y Autenticación
siguen funcionando (esto se prueba más abajo).

**Autenticación por JWT sin acoplamiento:** `auth-service` emite un token
firmado (JWT) al hacer login. `citas-service` e `inventario-service`
**verifican la firma del token localmente** (con una clave secreta
compartida, variable `JWT_SECRET`), sin llamar a la base de datos de
Autenticación en cada petición. Por eso, aunque `auth-service` esté caído,
las peticiones ya autenticadas hacia Citas o Inventario siguen funcionando.

## Cómo correrlo

Requiere Docker Desktop abierto (el build de los 4 servicios Java tarda unos
minutos la primera vez, porque Maven descarga las dependencias).

```bash
docker compose up -d --build
```

| Servicio | Contenedor | Puerto host |
|---|---|---|
| API Gateway (**usar este para todo**) | `vet-api-gateway` | **8000** |
| Auth Service (directo, para depurar) | `vet-auth-service` | 8081 |
| Citas Service (directo, para depurar) | `vet-citas-service` | 8082 |
| Inventario Service (directo, para depurar) | `vet-inventario-service` | 8083 |
| Adminer (interfaz gráfica de las BD) | `vet-adminer` | 8080 |
| PostgreSQL Autenticación | `vet-db-autenticacion` | 5433 |
| PostgreSQL Citas | `vet-db-citas` | 5434 |
| PostgreSQL Inventario | `vet-db-inventario` | 5435 |

> **Importante:** si reconstruyes/reinicias un servicio backend individual
> (`docker compose up -d --build citas-service`, por ejemplo), reinicia
> también el gateway después (`docker restart vet-api-gateway`). El gateway
> cachea las conexiones hacia los servicios, y si estos se recrean con una
> IP nueva dentro de la red de Docker, el gateway sigue apuntando a la IP
> vieja hasta que se reinicia.

## Usuarios de prueba (contraseña para todos: `Vet12345`)

| username | rol | canal |
|---|---|---|
| `recepcion1` | RECEPCIONISTA | RECEPCION |
| `vet.gomez` | VETERINARIO | VETERINARIO |
| `jperez` | PROPIETARIO | PROPIETARIO |
| `caja1` | INVENTARIO_CAJA | INVENTARIO_CAJA |

Al arrancar, `auth-service` reemplaza automáticamente los hashes de marcador
de posición del seed por un hash BCrypt real de `Vet12345` (ver
`SeedPasswordInitializer.java`), así que el login funciona desde el primer
arranque sin pasos manuales.

## Probar la API (ejemplos con curl, vía el Gateway en :8000)

**1. Login:**
```bash
curl -X POST http://localhost:8000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"recepcion1","password":"Vet12345","canal":"RECEPCION"}'
```
Devuelve un `token`. Úsalo en el resto de peticiones como
`-H "Authorization: Bearer <token>"`.

**2. Listar citas:**
```bash
curl http://localhost:8000/api/citas -H "Authorization: Bearer <token>"
```

**3. Crear una cita:**
```bash
curl -X POST http://localhost:8000/api/citas \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{"mascotaId":1,"propietarioId":1,"veterinarioId":1,"fechaCita":"2026-10-05","horaInicio":"09:00:00","horaFin":"09:30:00","motivo":"Control"}'
```
Si el veterinario ya tiene una cita en ese horario, responde **409
Conflict** (lo aplica el trigger `fn_validar_solapamiento_cita` de la base
de datos).

**4. Cambiar el estado de una cita:**
```bash
curl -X PATCH http://localhost:8000/api/citas/1/estado \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{"estado":"CONFIRMADA","observacion":"Confirmada por telefono"}'
```

**5. Ver productos e inventario:**
```bash
curl http://localhost:8000/api/productos -H "Authorization: Bearer <token>"
```

**6. Registrar un movimiento de inventario (entrada o salida):**
```bash
curl -X POST http://localhost:8000/api/movimientos \
  -H "Authorization: Bearer <token>" -H "Content-Type: application/json" \
  -d '{"productoId":1,"tipoMovimiento":"SALIDA_CONSUMO","cantidad":2,"observacion":"Consumo en atencion"}'
```
Si dejaría el stock en negativo, responde **409 Conflict** (trigger
`fn_aplicar_movimiento`). Si el stock resultante cae bajo el mínimo, se
genera automáticamente una alerta visible en:
```bash
curl http://localhost:8000/api/alertas -H "Authorization: Bearer <token>"
```

## Probar con Postman

En la carpeta [`postman/`](postman) hay una colección lista para importar
con **todos los endpoints** (29 requests) y cuerpos de ejemplo ya armados.

1. Instala [Postman](https://www.postman.com/downloads/) (o usa la versión web).
2. **Import** → arrastra el archivo
   `postman/ClinicaVeterinariaSOA.postman_collection.json`.
3. Asegúrate de que el stack esté corriendo (`docker compose up -d`).
4. Abre la carpeta **Autenticacion → Login** y dale **Send**. El script de
   esa petición guarda el token automáticamente en una variable de la
   colección; **todas las demás peticiones lo usan solas** (la colección
   tiene configurada la autenticación Bearer con `{{token}}`), no tienes
   que copiar/pegar nada.
5. Prueba cualquier otra petición de las carpetas **Citas** e **Inventario**.

Detalles a tener en cuenta:
- La URL base es `http://localhost:8000` (el API Gateway), definida en la
  variable de colección `baseUrl`. Si algún día cambia, se edita en un solo
  lugar: clic en la colección → pestaña **Variables**.
- El token dura 1 hora; si una petición responde `401`, vuelve a ejecutar
  **Login**.
- Los ejemplos de **crear** (`cita`, `propietario`, `producto`) usan datos
  fijos: al enviarlos por segunda vez responden error (`409` en la cita
  porque el horario ya está ocupado, y `400` en propietario/producto porque
  el documento/código ya existe). Es el comportamiento esperado; cambia
  `numeroDocumento`, `codigo` u `horaInicio` para crear otro.
- `jperez` ya viene **bloqueada** desde el seed (por eso la petición "Login
  con cuenta ya bloqueada" responde `423`). La carpeta **Demo bloqueo de
  cuenta** permite bloquear a `vet.gomez` enviando 3 veces una contraseña
  incorrecta; no la ejecutes dentro de Collection Runner, ya que dejaría la
  cuenta bloqueada. Para desbloquearla:
  ```bash
  docker exec vet-db-autenticacion psql -U postgres -d autenticacion -c "UPDATE usuarios SET estado='ACTIVO', intentos_fallidos=0 WHERE username='vet.gomez';"
  ```

También sirve la extensión **Thunder Client** de VS Code (importa el mismo
archivo).

## Endpoints disponibles

**Autenticación** (`/api/auth/**`): `POST /login`, `POST /logout`.

**Citas** (`/api/citas/**`, `/api/mascotas/**`, `/api/propietarios/**`,
`/api/veterinarios/**`, `/api/especies/**`, `/api/razas/**`,
`/api/estados-cita/**`): CRUD básico + `GET /api/citas/{id}/historial`,
`PATCH /api/citas/{id}/estado`, `GET /api/veterinarios/{id}/horarios`.

**Inventario** (`/api/productos/**`, `/api/movimientos/**`,
`/api/alertas/**`, `/api/categorias-producto/**`, `/api/unidades-medida/**`,
`/api/tipos-movimiento/**`, `/api/proveedores/**`): CRUD básico +
`POST /api/movimientos`, `PATCH /api/alertas/{id}/atender`.

## Monitoreo con Prometheus y Grafana

| Herramienta | URL | Acceso |
|---|---|---|
| Grafana (tablero "Clinica Veterinaria SOA") | http://localhost:3000 | `admin` / `admin` (puedes elegir "Skip" al cambiar la clave) |
| Prometheus (ver Status → Targets) | http://localhost:9090 | sin login |

**Cómo funciona:**
1. Cada servicio Java (los 3 y el gateway) incluye Spring Boot Actuator y Micrometer, y publica sus métricas en `/actuator/prometheus` (tráfico, errores por código HTTP, tiempos de respuesta, memoria de la JVM, conexiones a la base).
2. Hay un `postgres_exporter` por cada base de datos. Además de métricas de PostgreSQL, ejecuta consultas de negocio definidas en `monitoring/postgres-exporter/` (cuentas bloqueadas, citas por estado, alertas de stock, stock por producto). Así se obtienen métricas de negocio sin modificar el código de los servicios.
3. Prometheus (`monitoring/prometheus/prometheus.yml`) consulta los 7 objetivos cada 5 segundos.
4. Grafana carga automáticamente el datasource y el tablero desde `monitoring/grafana/` (provisioning), por lo que no hay que configurar nada a mano.

El tablero tiene 3 secciones: estado UP/DOWN de cada componente, tráfico y rendimiento, y métricas de negocio.

## Prueba automática de todo el sistema

Con el sistema levantado, desde la carpeta del proyecto:

```bash
powershell -ExecutionPolicy Bypass -File .\probar-sistema.ps1
```

Comprueba los 14 contenedores, el login (200, 401, 423 por bloqueo), citas (201 y 409 por solapamiento), inventario (201 y 409 por stock insuficiente), las especificaciones de Swagger, Prometheus (7 objetivos), Grafana y Jaeger. Con `-Aislamiento` apaga Inventario, comprueba que Citas y Autenticación siguen funcionando y lo vuelve a encender. Cada línea sale como `[OK]` o `[FALLO]`; al final muestra el total. Crea una cita y un movimiento de prueba y deja a `vet.gomez` desbloqueado.

## Documentación interactiva con Swagger (OpenAPI)

| Pantalla | URL |
|---|---|
| Swagger UI unificado (los 3 servicios) | http://localhost:8000/swagger-ui.html |

**Cómo se usa:**
1. Abre la URL. Arriba a la derecha, el menú **Select a definition** cambia entre Autenticación, Citas e Inventario.
2. En **Autenticación → POST /api/auth/login** pulsa **Try it out** y **Execute** (ya trae un usuario de prueba). Copia el valor de `token` de la respuesta.
3. Pulsa **Authorize** (candado, arriba), pega el token (sin la palabra Bearer) y confirma. Desde ese momento todas las pruebas de Citas e Inventario lo usan.
4. Abre cualquier operación, **Try it out**, **Execute**. Verás la petición, el código de respuesta y el JSON devuelto.

**Cómo funciona:**
- Cada servicio genera su especificación OpenAPI desde su propio código con `springdoc-openapi` (ruta interna `/v3/api-docs`). Las descripciones y los ejemplos salen de las anotaciones en controladores y DTOs.
- El API Gateway publica las tres especificaciones en `/api-docs/auth`, `/api-docs/citas` y `/api-docs/inventario`, y sirve una única pantalla de Swagger que las lista.
- Las pruebas salen por el gateway (servidor relativo `/`), igual que las de Postman, por lo que el flujo es el mismo que en producción.
- `/v3/api-docs` y `/swagger-ui` no exigen token; las operaciones sí.

## Trazas distribuidas con OpenTelemetry y Jaeger (Jaeger-OpenTe)

| Herramienta | URL |
|---|---|
| Jaeger-OpenTe (contenedor `Jaeger-OpenTe`) | http://localhost:16686 |

Prometheus y Grafana responden *cuántas* peticiones hay y cuántas fallan; las trazas responden **por dónde pasó cada petición y cuánto tardó en cada tramo** (gateway → servicio → consultas SQL).

**Cómo funciona:**
1. Los 4 `Dockerfile` Java descargan el **agente de OpenTelemetry (v2.33.1)** y arrancan la aplicación con `-javaagent`. No se modificó el código de negocio.
2. El agente lee `monitoring/otel/otel-config.yaml` (variable `OTEL_CONFIG_FILE`): nombre del servicio, exportación de trazas por OTLP/HTTP hacia `Jaeger-OpenTe` (puerto 4318) y una regla que **descarta las trazas de `/actuator`** (Prometheus consulta cada 5 s y taparía las peticiones reales).
3. El gateway propaga el identificador de la traza a cada servicio, por lo que una petición aparece como **una sola traza** con varios tramos.
4. Solo se exportan trazas; las métricas siguen en Prometheus.

**Cómo verlo:** haz una petición en Postman (por ejemplo `GET /api/citas`), abre Jaeger, elige el servicio `api-gateway` y pulsa **Find Traces**. Entra a una traza para ver la cascada: `api-gateway` → `citas-service` → `SELECT ...`.

Notas:
- Jaeger guarda las trazas en memoria: se pierden al reiniciar su contenedor.
- El agente muestra dos avisos al arrancar (versión del formato del archivo y regla marcada como obsoleta para la versión 3.0). No afectan el funcionamiento.
- Como los servicios no se llaman entre sí, las trazas son cortas: cliente → gateway → un servicio → su base.

## Comprobar el aislamiento de fallos (con el backend real)

```bash
docker stop vet-inventario-service vet-db-inventario
```

`GET http://localhost:8000/api/citas` con un token válido sigue
respondiendo con normalidad — Inventario está completamente caído (base de
datos y servicio) y Citas/Autenticación no se enteran. Esto se probó
explícitamente antes de la entrega. Para restaurar:
```bash
docker start vet-db-inventario vet-inventario-service
docker restart vet-api-gateway
```

## Reglas de negocio implementadas (trigger en la BD + manejo de errores en el backend)

### Autenticación (sección 4.2 / Tabla 1 del documento)
- El trigger `fn_controlar_intentos_fallidos` bloquea la cuenta
  (`usuarios.estado = 'BLOQUEADO'`) al registrar 3 `LOGIN_FALLIDO`
  consecutivos y genera una notificación. `AuthController` traduce esto a
  HTTP `423 Locked`.

### Gestión de Citas (sección 4.1)
- El trigger `fn_validar_solapamiento_cita` impide asignar dos citas activas
  al mismo veterinario en horarios que se solapan. `CitaController` lo
  traduce a HTTP `409 Conflict`.
- `historial_estados_cita` conserva el rastro de cada
  confirmación/reprogramación/cancelación (expuesto en
  `GET /api/citas/{id}/historial`).

### Gestión de Inventario (sección 4.3)
- El trigger `fn_aplicar_movimiento` actualiza `stock_actual`
  automáticamente, rechaza movimientos que dejarían stock negativo (HTTP
  `409 Conflict`), y genera una fila en `alertas_stock` cuando el stock cae
  bajo el mínimo.

## Validado end-to-end

Antes de la entrega se probó: compilación de los 4 servicios Java, arranque
completo del stack (7 contenedores), login con contraseña real vía Gateway,
creación y rechazo de citas solapadas vía API, registro de movimientos de
inventario con generación de alertas vía API, y aislamiento de fallos real
(apagando el servicio + la base de datos de Inventario y confirmando que
Citas y Autenticación siguen respondiendo).
