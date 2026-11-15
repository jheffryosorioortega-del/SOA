-- =====================================================================
-- Base de datos INDEPENDIENTE del Servicio de Gestión de Citas.
-- Incluye también Pacientes/Propietarios/Veterinarios: no son uno de los
-- 3 procesos seleccionados en la sección 4 del documento, pero Citas
-- depende directamente de ellos, así que viven en su misma base.
--
-- Las columnas que en el diseño monolítico apuntaban a otro servicio
-- (usuario_id -> Servicio de Autenticación) YA NO tienen foreign key:
-- en una arquitectura "database per service" no puede existir una FK física
-- entre bases de datos distintas. La validez de esos IDs se garantiza a
-- nivel de aplicación (el backend de Citas llama al API del Servicio de
-- Autenticación para validar el usuario antes de escribir).
-- =====================================================================

-- --- Datos maestros compartidos (Pacientes) ---

CREATE TABLE propietarios (
    id                  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id          INTEGER UNIQUE, -- referencia lógica a autenticacion.usuarios(id) (Portal del Propietario)
    nombres             VARCHAR(100) NOT NULL,
    apellidos           VARCHAR(100) NOT NULL,
    tipo_documento      VARCHAR(10) NOT NULL DEFAULT 'DNI',
    numero_documento    VARCHAR(20) NOT NULL UNIQUE,
    telefono            VARCHAR(20) NOT NULL,
    correo              VARCHAR(150),
    direccion           VARCHAR(200),
    fecha_registro      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE especies (
    id      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre  VARCHAR(50) NOT NULL UNIQUE -- Canino, Felino, etc.
);

CREATE TABLE razas (
    id          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    especie_id  INTEGER NOT NULL REFERENCES especies(id),
    nombre      VARCHAR(60) NOT NULL,
    UNIQUE (especie_id, nombre)
);

CREATE TABLE mascotas (
    id                  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    propietario_id      INTEGER NOT NULL REFERENCES propietarios(id),
    especie_id          INTEGER NOT NULL REFERENCES especies(id),
    raza_id             INTEGER REFERENCES razas(id),
    nombre              VARCHAR(80) NOT NULL,
    sexo                CHAR(1) NOT NULL CHECK (sexo IN ('M', 'H')),
    fecha_nacimiento    DATE,
    fecha_registro      TIMESTAMPTZ NOT NULL DEFAULT now(),
    activo              BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE veterinarios (
    id              INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usuario_id      INTEGER UNIQUE NOT NULL, -- referencia lógica a autenticacion.usuarios(id)
    nombres         VARCHAR(100) NOT NULL,
    apellidos       VARCHAR(100) NOT NULL,
    colegiatura     VARCHAR(30) NOT NULL UNIQUE,
    especialidad    VARCHAR(80),
    activo          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE horarios_veterinario (
    id              INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    veterinario_id  INTEGER NOT NULL REFERENCES veterinarios(id),
    dia_semana      SMALLINT NOT NULL CHECK (dia_semana BETWEEN 1 AND 7), -- 1=Lunes ... 7=Domingo
    hora_inicio     TIME NOT NULL,
    hora_fin        TIME NOT NULL,
    CHECK (hora_fin > hora_inicio)
);

CREATE INDEX idx_mascotas_propietario ON mascotas(propietario_id);
CREATE INDEX idx_razas_especie ON razas(especie_id);
CREATE INDEX idx_horarios_vet ON horarios_veterinario(veterinario_id, dia_semana);

-- --- Servicio de Gestión de Citas ---

CREATE TABLE estados_cita (
    id      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre  VARCHAR(30) NOT NULL UNIQUE
    -- PROGRAMADA, CONFIRMADA, REPROGRAMADA, CANCELADA, ATENDIDA, NO_ASISTIO
);

CREATE TABLE citas (
    id                  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    mascota_id          INTEGER NOT NULL REFERENCES mascotas(id),
    propietario_id      INTEGER NOT NULL REFERENCES propietarios(id),
    veterinario_id      INTEGER NOT NULL REFERENCES veterinarios(id),
    estado_id           INTEGER NOT NULL REFERENCES estados_cita(id),
    fecha_cita          DATE NOT NULL,
    hora_inicio         TIME NOT NULL,
    hora_fin            TIME NOT NULL,
    motivo              VARCHAR(200) NOT NULL,
    creado_por          INTEGER NOT NULL, -- referencia lógica a autenticacion.usuarios(id)
    actualizado_por     INTEGER,          -- referencia lógica a autenticacion.usuarios(id)
    fecha_creacion      TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_modificacion  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (hora_fin > hora_inicio)
);

CREATE TABLE historial_estados_cita (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cita_id         INTEGER NOT NULL REFERENCES citas(id),
    estado_id       INTEGER NOT NULL REFERENCES estados_cita(id),
    usuario_id      INTEGER NOT NULL, -- referencia lógica a autenticacion.usuarios(id)
    observacion     VARCHAR(300),
    fecha_cambio    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_citas_fecha ON citas(fecha_cita);
CREATE INDEX idx_citas_veterinario ON citas(veterinario_id, fecha_cita);
CREATE INDEX idx_citas_mascota ON citas(mascota_id);
CREATE INDEX idx_citas_estado ON citas(estado_id);
CREATE INDEX idx_historial_cita ON historial_estados_cita(cita_id);

-- Evita que un veterinario tenga dos citas activas que se solapen en fecha/hora
CREATE OR REPLACE FUNCTION fn_validar_solapamiento_cita()
RETURNS TRIGGER AS $$
DECLARE
    v_conflicto INTEGER;
BEGIN
    SELECT c.id INTO v_conflicto
      FROM citas c
      JOIN estados_cita e ON e.id = c.estado_id
     WHERE c.veterinario_id = NEW.veterinario_id
       AND c.fecha_cita = NEW.fecha_cita
       AND c.id <> COALESCE(NEW.id, -1)
       AND e.nombre NOT IN ('CANCELADA', 'NO_ASISTIO')
       AND (NEW.hora_inicio, NEW.hora_fin) OVERLAPS (c.hora_inicio, c.hora_fin)
     LIMIT 1;

    IF v_conflicto IS NOT NULL THEN
        RAISE EXCEPTION 'El veterinario % ya tiene una cita activa (id=%) que se solapa con el horario solicitado',
            NEW.veterinario_id, v_conflicto;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_citas_validar_solapamiento
BEFORE INSERT OR UPDATE ON citas
FOR EACH ROW
EXECUTE FUNCTION fn_validar_solapamiento_cita();

CREATE OR REPLACE FUNCTION fn_touch_fecha_modificacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_modificacion := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_citas_touch
BEFORE UPDATE ON citas
FOR EACH ROW
EXECUTE FUNCTION fn_touch_fecha_modificacion();

CREATE OR REPLACE FUNCTION fn_registrar_historial_estado()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' OR NEW.estado_id IS DISTINCT FROM OLD.estado_id THEN
        INSERT INTO historial_estados_cita (cita_id, estado_id, usuario_id, observacion)
        VALUES (NEW.id, NEW.estado_id, COALESCE(NEW.actualizado_por, NEW.creado_por),
                'Registro automático de cambio de estado');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_citas_historial_insert
AFTER INSERT ON citas
FOR EACH ROW
EXECUTE FUNCTION fn_registrar_historial_estado();

CREATE TRIGGER trg_citas_historial_update
AFTER UPDATE ON citas
FOR EACH ROW
EXECUTE FUNCTION fn_registrar_historial_estado();
