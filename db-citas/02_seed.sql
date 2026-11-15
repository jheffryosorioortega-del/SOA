-- Datos de ejemplo del Servicio de Gestión de Citas
-- Los usuario_id usados abajo (2 = vet.gomez, 3 = jperez, 1 = recepcion1)
-- corresponden a los IDs generados en db-autenticacion/02_seed.sql.

INSERT INTO estados_cita (nombre) VALUES
    ('PROGRAMADA'), ('CONFIRMADA'), ('REPROGRAMADA'),
    ('CANCELADA'), ('ATENDIDA'), ('NO_ASISTIO');

INSERT INTO especies (nombre) VALUES ('Canino'), ('Felino');
INSERT INTO razas (especie_id, nombre) SELECT id, 'Mestizo' FROM especies;

-- usuario_id = 2 -> vet.gomez (creado en autenticacion)
INSERT INTO veterinarios (usuario_id, nombres, apellidos, colegiatura, especialidad)
VALUES (2, 'Carlos', 'Gómez', 'CMVP-1234', 'Medicina General');

-- usuario_id = 3 -> jperez (creado en autenticacion)
INSERT INTO propietarios (usuario_id, nombres, apellidos, numero_documento, telefono, correo)
VALUES (3, 'Julia', 'Pérez', '45678912', '987654321', 'jperez@mail.com');

INSERT INTO mascotas (propietario_id, especie_id, raza_id, nombre, sexo, fecha_nacimiento)
SELECT p.id, e.id, r.id, 'Firulais', 'M', DATE '2021-05-10'
FROM propietarios p, especies e, razas r
WHERE p.numero_documento = '45678912' AND e.nombre = 'Canino' AND r.especie_id = e.id;

-- usuario_id = 1 -> recepcion1 (creado en autenticacion)
INSERT INTO citas (mascota_id, propietario_id, veterinario_id, estado_id,
                    fecha_cita, hora_inicio, hora_fin, motivo, creado_por)
SELECT m.id, m.propietario_id, v.id,
       (SELECT id FROM estados_cita WHERE nombre = 'PROGRAMADA'),
       CURRENT_DATE + 1, TIME '09:00', TIME '09:30', 'Control de vacunas', 1
FROM mascotas m, veterinarios v
WHERE m.nombre = 'Firulais';

-- Esta debería FALLAR (horario solapado con la cita anterior):
-- INSERT INTO citas (mascota_id, propietario_id, veterinario_id, estado_id,
--                     fecha_cita, hora_inicio, hora_fin, motivo, creado_por)
-- SELECT m.id, m.propietario_id, v.id,
--        (SELECT id FROM estados_cita WHERE nombre = 'PROGRAMADA'),
--        CURRENT_DATE + 1, TIME '09:15', TIME '09:45', 'Consulta general', 1
-- FROM mascotas m, veterinarios v
-- WHERE m.nombre = 'Firulais';
