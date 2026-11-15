-- Datos de ejemplo del Servicio de Autenticación
-- IMPORTANTE: los IDs de usuario generados aquí (1,2,3,4 en orden de inserción)
-- son los que los otros servicios (Citas, Inventario) referencian como
-- "soft reference" (columna sin FK física) en sus propios seeds.

INSERT INTO roles (nombre, descripcion) VALUES
    ('RECEPCIONISTA', 'Personal de recepción de la clínica'),
    ('VETERINARIO', 'Médico veterinario'),
    ('PROPIETARIO', 'Dueño de mascota, acceso vía portal'),
    ('INVENTARIO_CAJA', 'Personal de inventario y caja');

INSERT INTO usuarios (rol_id, username, correo, password_hash, nombres, apellidos) VALUES
    ((SELECT id FROM roles WHERE nombre = 'RECEPCIONISTA'), 'recepcion1', 'recepcion1@vet.pe', 'hash_demo_1', 'Ana', 'Torres'),
    ((SELECT id FROM roles WHERE nombre = 'VETERINARIO'),   'vet.gomez',  'gomez@vet.pe',      'hash_demo_2', 'Carlos', 'Gómez'),
    ((SELECT id FROM roles WHERE nombre = 'PROPIETARIO'),   'jperez',     'jperez@mail.com',   'hash_demo_3', 'Julia', 'Pérez'),
    ((SELECT id FROM roles WHERE nombre = 'INVENTARIO_CAJA'),'caja1',     'caja1@vet.pe',      'hash_demo_4', 'Luis', 'Ramos');

-- Prueba: 3 intentos fallidos consecutivos -> debe bloquear la cuenta de jperez
INSERT INTO log_accesos (usuario_id, username_intento, tipo_evento, canal)
SELECT id, 'jperez', 'LOGIN_FALLIDO', 'PROPIETARIO' FROM usuarios WHERE username = 'jperez';
INSERT INTO log_accesos (usuario_id, username_intento, tipo_evento, canal)
SELECT id, 'jperez', 'LOGIN_FALLIDO', 'PROPIETARIO' FROM usuarios WHERE username = 'jperez';
INSERT INTO log_accesos (usuario_id, username_intento, tipo_evento, canal)
SELECT id, 'jperez', 'LOGIN_FALLIDO', 'PROPIETARIO' FROM usuarios WHERE username = 'jperez';
-- Verificación esperada:
-- SELECT estado, intentos_fallidos FROM usuarios WHERE username = 'jperez';  -> BLOQUEADO, 3
-- SELECT * FROM notificaciones;
