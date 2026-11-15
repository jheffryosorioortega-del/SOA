-- Datos de ejemplo del Servicio de Gestión de Inventario
-- usuario_id = 4 -> caja1 (creado en autenticacion)
-- cita_id = 1 -> la cita creada en db-citas/02_seed.sql

INSERT INTO categorias_producto (nombre) VALUES
    ('Medicamento'), ('Vacuna'), ('Alimento'), ('Insumo');
INSERT INTO unidades_medida (nombre, abreviatura) VALUES
    ('Unidad', 'UND'), ('Mililitro', 'ML');
INSERT INTO tipos_movimiento (nombre, signo) VALUES
    ('ENTRADA_COMPRA', 1), ('SALIDA_CONSUMO', -1), ('AJUSTE', 1), ('DEVOLUCION', -1);

INSERT INTO productos (codigo, nombre, categoria_id, unidad_medida_id, precio_unitario, stock_minimo, stock_actual)
VALUES ('MED-001', 'Amoxicilina 250mg',
        (SELECT id FROM categorias_producto WHERE nombre = 'Medicamento'),
        (SELECT id FROM unidades_medida WHERE nombre = 'Unidad'),
        15.50, 10, 12);

-- Salida por consumo durante la cita 1 -> stock resultante 7 (< minimo 10) -> genera alerta
INSERT INTO movimientos_inventario (producto_id, tipo_movimiento_id, cantidad, cita_id, usuario_id, observacion)
SELECT p.id, (SELECT id FROM tipos_movimiento WHERE nombre = 'SALIDA_CONSUMO'), 5, 1, 4, 'Consumo durante atención'
FROM productos p WHERE p.codigo = 'MED-001';
-- Verificación esperada:
-- SELECT stock_actual FROM productos WHERE codigo = 'MED-001'; -> 7.00
-- SELECT * FROM alertas_stock;
