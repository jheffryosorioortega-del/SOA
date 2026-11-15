-- =====================================================================
-- Base de datos INDEPENDIENTE del Servicio de Gestión de Inventario.
--
-- Las columnas cita_id y usuario_id (antes con FK hacia otras bases) ahora
-- son solo columnas planas: la integridad referencial entre servicios se
-- garantiza a nivel de aplicación (API), no a nivel de base de datos, que
-- es exactamente el punto de una arquitectura "database per service".
-- =====================================================================

CREATE TABLE categorias_producto (
    id          INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre      VARCHAR(60) NOT NULL UNIQUE -- Medicamento, Vacuna, Alimento, Insumo
);

CREATE TABLE unidades_medida (
    id              INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre          VARCHAR(30) NOT NULL UNIQUE,
    abreviatura     VARCHAR(10) NOT NULL
);

CREATE TABLE proveedores (
    id              INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    razon_social    VARCHAR(150) NOT NULL,
    ruc             VARCHAR(20) NOT NULL UNIQUE,
    telefono        VARCHAR(20),
    correo          VARCHAR(150),
    direccion       VARCHAR(200),
    activo          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE productos (
    id                  INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    codigo              VARCHAR(30) NOT NULL UNIQUE,
    nombre              VARCHAR(150) NOT NULL,
    categoria_id        INTEGER NOT NULL REFERENCES categorias_producto(id),
    unidad_medida_id    INTEGER NOT NULL REFERENCES unidades_medida(id),
    precio_unitario     NUMERIC(10,2) NOT NULL CHECK (precio_unitario >= 0),
    stock_minimo        NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    stock_actual        NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    estado              VARCHAR(20) NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO', 'DESCONTINUADO')),
    fecha_registro      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE tipos_movimiento (
    id      INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre  VARCHAR(30) NOT NULL UNIQUE, -- ENTRADA_COMPRA, SALIDA_CONSUMO, AJUSTE, DEVOLUCION
    signo   SMALLINT NOT NULL CHECK (signo IN (1, -1)) -- +1 suma stock, -1 resta stock
);

-- Kárdex: un registro por cada movimiento de stock
CREATE TABLE movimientos_inventario (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    producto_id         INTEGER NOT NULL REFERENCES productos(id),
    tipo_movimiento_id  INTEGER NOT NULL REFERENCES tipos_movimiento(id),
    cantidad            NUMERIC(10,2) NOT NULL CHECK (cantidad > 0),
    proveedor_id        INTEGER REFERENCES proveedores(id), -- aplica en entradas por compra
    cita_id             INTEGER, -- referencia lógica a citas.citas(id); aplica en salidas por consumo
    usuario_id          INTEGER NOT NULL, -- referencia lógica a autenticacion.usuarios(id)
    saldo_resultante    NUMERIC(10,2) NOT NULL,
    observacion         VARCHAR(300),
    fecha_movimiento    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE alertas_stock (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    producto_id         INTEGER NOT NULL REFERENCES productos(id),
    stock_al_momento    NUMERIC(10,2) NOT NULL,
    estado              VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'ATENDIDA')),
    fecha_alerta        TIMESTAMPTZ NOT NULL DEFAULT now(),
    fecha_atencion      TIMESTAMPTZ
);

CREATE INDEX idx_productos_categoria ON productos(categoria_id);
CREATE INDEX idx_movimientos_producto ON movimientos_inventario(producto_id, fecha_movimiento);
CREATE INDEX idx_movimientos_cita ON movimientos_inventario(cita_id);
CREATE INDEX idx_alertas_producto ON alertas_stock(producto_id, estado);

-- Actualiza el stock del producto en cada movimiento y calcula el saldo resultante
CREATE OR REPLACE FUNCTION fn_aplicar_movimiento()
RETURNS TRIGGER AS $$
DECLARE
    v_signo         SMALLINT;
    v_nuevo_stock   NUMERIC(10,2);
    v_stock_minimo  NUMERIC(10,2);
BEGIN
    SELECT signo INTO v_signo FROM tipos_movimiento WHERE id = NEW.tipo_movimiento_id;

    UPDATE productos
       SET stock_actual = stock_actual + (v_signo * NEW.cantidad)
     WHERE id = NEW.producto_id
     RETURNING stock_actual, stock_minimo INTO v_nuevo_stock, v_stock_minimo;

    IF v_nuevo_stock < 0 THEN
        RAISE EXCEPTION 'Stock insuficiente para el producto %: stock resultante %', NEW.producto_id, v_nuevo_stock;
    END IF;

    NEW.saldo_resultante := v_nuevo_stock;

    IF v_nuevo_stock < v_stock_minimo THEN
        INSERT INTO alertas_stock (producto_id, stock_al_momento)
        VALUES (NEW.producto_id, v_nuevo_stock);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_movimientos_aplicar
BEFORE INSERT ON movimientos_inventario
FOR EACH ROW
EXECUTE FUNCTION fn_aplicar_movimiento();
