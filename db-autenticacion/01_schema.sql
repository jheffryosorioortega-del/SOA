-- =====================================================================
-- Base de datos INDEPENDIENTE del Servicio de Autenticación.
-- Patrón "Database per Service": esta base de datos vive en su propio
-- contenedor Postgres y no comparte motor con los demás servicios. Si este
-- contenedor cae, solo el Servicio de Autenticación deja de responder; los
-- servicios de Citas e Inventario siguen operando.
-- =====================================================================

CREATE TABLE roles (
    id              INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre          VARCHAR(40) NOT NULL UNIQUE,   -- RECEPCIONISTA, VETERINARIO, PROPIETARIO, INVENTARIO_CAJA, ADMIN
    descripcion     VARCHAR(200)
);

CREATE TABLE usuarios (
    id                  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rol_id              INTEGER NOT NULL REFERENCES roles(id),
    username            VARCHAR(50) NOT NULL UNIQUE,
    correo              VARCHAR(150) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    nombres             VARCHAR(100) NOT NULL,
    apellidos           VARCHAR(100) NOT NULL,
    estado              VARCHAR(20) NOT NULL DEFAULT 'ACTIVO'
                            CHECK (estado IN ('ACTIVO', 'BLOQUEADO', 'INACTIVO')),
    intentos_fallidos   SMALLINT NOT NULL DEFAULT 0 CHECK (intentos_fallidos >= 0),
    fecha_creacion      TIMESTAMPTZ NOT NULL DEFAULT now(),
    ultimo_acceso       TIMESTAMPTZ
);

CREATE TABLE sesiones (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id          INTEGER NOT NULL REFERENCES usuarios(id),
    token               VARCHAR(500) NOT NULL UNIQUE,
    fecha_emision       TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_expiracion    TIMESTAMPTZ NOT NULL,
    activo              BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE log_accesos (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id       INTEGER REFERENCES usuarios(id),
    username_intento VARCHAR(50) NOT NULL, -- se conserva aunque el usuario no exista, para auditoría
    tipo_evento      VARCHAR(20) NOT NULL
                         CHECK (tipo_evento IN ('LOGIN_EXITOSO', 'LOGIN_FALLIDO', 'BLOQUEO', 'LOGOUT')),
    canal            VARCHAR(30) NOT NULL
                         CHECK (canal IN ('RECEPCION', 'VETERINARIO', 'PROPIETARIO', 'INVENTARIO_CAJA')),
    ip_origen        INET,
    fecha_hora       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE notificaciones (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id      INTEGER REFERENCES usuarios(id),
    tipo            VARCHAR(30) NOT NULL, -- CUENTA_BLOQUEADA, etc.
    mensaje         VARCHAR(300) NOT NULL,
    fecha_envio     TIMESTAMPTZ NOT NULL DEFAULT now(),
    leida           BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_usuarios_rol ON usuarios(rol_id);
CREATE INDEX idx_sesiones_usuario ON sesiones(usuario_id);
CREATE INDEX idx_log_accesos_usuario ON log_accesos(usuario_id);
CREATE INDEX idx_log_accesos_fecha ON log_accesos(fecha_hora);

-- Bloqueo automático de cuenta al superar 3 intentos fallidos (Tabla 1 del documento)
CREATE OR REPLACE FUNCTION fn_controlar_intentos_fallidos()
RETURNS TRIGGER AS $$
DECLARE
    v_intentos SMALLINT;
BEGIN
    IF NEW.tipo_evento = 'LOGIN_FALLIDO' AND NEW.usuario_id IS NOT NULL THEN
        UPDATE usuarios
           SET intentos_fallidos = intentos_fallidos + 1
         WHERE id = NEW.usuario_id
         RETURNING intentos_fallidos INTO v_intentos;

        IF v_intentos >= 3 THEN
            UPDATE usuarios SET estado = 'BLOQUEADO' WHERE id = NEW.usuario_id;

            INSERT INTO log_accesos (usuario_id, username_intento, tipo_evento, canal, fecha_hora)
            VALUES (NEW.usuario_id, NEW.username_intento, 'BLOQUEO', NEW.canal, now());

            INSERT INTO notificaciones (usuario_id, tipo, mensaje)
            VALUES (NEW.usuario_id, 'CUENTA_BLOQUEADA',
                    'Su cuenta ha sido bloqueada por superar el número de intentos fallidos permitidos.');
        END IF;
    ELSIF NEW.tipo_evento = 'LOGIN_EXITOSO' AND NEW.usuario_id IS NOT NULL THEN
        UPDATE usuarios
           SET intentos_fallidos = 0,
               ultimo_acceso = now()
         WHERE id = NEW.usuario_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_controlar_intentos_fallidos
AFTER INSERT ON log_accesos
FOR EACH ROW
EXECUTE FUNCTION fn_controlar_intentos_fallidos();
