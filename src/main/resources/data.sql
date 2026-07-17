-- Insertar Categorías de prueba (idempotente: no falla si ya existen)
INSERT INTO categoria (nombre)
SELECT 'Abarrotes' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Abarrotes');
INSERT INTO categoria (nombre)
SELECT 'Lácteos' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Lácteos');
INSERT INTO categoria (nombre)
SELECT 'Bebidas' WHERE NOT EXISTS (SELECT 1 FROM categoria WHERE nombre = 'Bebidas');

-- Insertar Productos de prueba (idempotente por nombre)
-- Nota: 'version' es obligatorio por tu anotación @Version en la entidad Producto (control de concurrencia)
INSERT INTO producto (nombre, precio, stock, version, categoria_id)
SELECT 'Galletas de Chocolate', 1500.0, 20, 0, 1
WHERE NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Galletas de Chocolate');

INSERT INTO producto (nombre, precio, stock, version, categoria_id)
SELECT 'Fideos Spaghetti', 990.0, 50, 0, 1
WHERE NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Fideos Spaghetti');

INSERT INTO producto (nombre, precio, stock, version, categoria_id)
SELECT 'Leche Entera 1L', 1200.0, 30, 0, 2
WHERE NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Leche Entera 1L');

INSERT INTO producto (nombre, precio, stock, version, categoria_id)
SELECT 'Jugo de Naranja', 1800.0, 15, 0, 3
WHERE NOT EXISTS (SELECT 1 FROM producto WHERE nombre = 'Jugo de Naranja');

-- ============================================================
-- SUCURSALES (idempotente: no falla si ya existen)
-- ============================================================
INSERT INTO sucursal (nombre, direccion, activa)
SELECT 'Sucursal Centro', 'Av. Libertador Bernardo O''Higgins 123, Santiago', true
WHERE NOT EXISTS (SELECT 1 FROM sucursal WHERE nombre = 'Sucursal Centro');

INSERT INTO sucursal (nombre, direccion, activa)
SELECT 'Sucursal Providencia', 'Av. Providencia 456, Providencia', true
WHERE NOT EXISTS (SELECT 1 FROM sucursal WHERE nombre = 'Sucursal Providencia');

INSERT INTO sucursal (nombre, direccion, activa)
SELECT 'Sucursal Las Condes', 'Av. Apoquindo 789, Las Condes', true
WHERE NOT EXISTS (SELECT 1 FROM sucursal WHERE nombre = 'Sucursal Las Condes');


-- ============================================================
-- PROVEEDORES (idempotente por RUT único)
-- ============================================================
INSERT INTO proveedores (nombre, rut, email, telefono, activo)
SELECT 'Distribuidora Andina SpA', '76.123.456-7', 'ventas@andina.cl', '+56223334444', true
WHERE NOT EXISTS (SELECT 1 FROM proveedores WHERE rut = '76.123.456-7');

INSERT INTO proveedores (nombre, rut, email, telefono, activo)
SELECT 'Comercial Sureña Ltda.', '77.987.654-3', 'contacto@surena.cl', '+56229998888', true
WHERE NOT EXISTS (SELECT 1 FROM proveedores WHERE rut = '77.987.654-3');

-- ============================================================
-- STOCK POR SUCURSAL (cruce Producto x Sucursal)
-- Nota: se usa subconsulta por nombre en vez de IDs fijos,
-- para no depender del orden real de inserción/autogeneración.
-- 'version' es obligatorio por @Version en StockSucursal (concurrencia optimista)
-- ============================================================

-- Sucursal Centro
INSERT INTO stock_sucursal (sucursal_id, producto_id, cantidad, stock_minimo, version)
SELECT s.id, p.id, 40, 10, 0
FROM sucursal s, producto p
WHERE s.nombre = 'Sucursal Centro' AND p.nombre = 'Galletas de Chocolate'
AND NOT EXISTS (SELECT 1 FROM stock_sucursal ss WHERE ss.sucursal_id = s.id AND ss.producto_id = p.id);

INSERT INTO stock_sucursal (sucursal_id, producto_id, cantidad, stock_minimo, version)
SELECT s.id, p.id, 60, 15, 0
FROM sucursal s, producto p
WHERE s.nombre = 'Sucursal Centro' AND p.nombre = 'Fideos Spaghetti'
AND NOT EXISTS (SELECT 1 FROM stock_sucursal ss WHERE ss.sucursal_id = s.id AND ss.producto_id = p.id);

INSERT INTO stock_sucursal (sucursal_id, producto_id, cantidad, stock_minimo, version)
SELECT s.id, p.id, 25, 10, 0
FROM sucursal s, producto p
WHERE s.nombre = 'Sucursal Centro' AND p.nombre = 'Leche Entera 1L'
AND NOT EXISTS (SELECT 1 FROM stock_sucursal ss WHERE ss.sucursal_id = s.id AND ss.producto_id = p.id);

INSERT INTO stock_sucursal (sucursal_id, producto_id, cantidad, stock_minimo, version)
SELECT s.id, p.id, 8, 5, 0
FROM sucursal s, producto p
WHERE s.nombre = 'Sucursal Centro' AND p.nombre = 'Jugo de Naranja'
AND NOT EXISTS (SELECT 1 FROM stock_sucursal ss WHERE ss.sucursal_id = s.id AND ss.producto_id = p.id);

-- Sucursal Providencia
INSERT INTO stock_sucursal (sucursal_id, producto_id, cantidad, stock_minimo, version)
SELECT s.id, p.id, 18, 10, 0
FROM sucursal s, producto p
WHERE s.nombre = 'Sucursal Providencia' AND p.nombre = 'Galletas de Chocolate'
AND NOT EXISTS (SELECT 1 FROM stock_sucursal ss WHERE ss.sucursal_id = s.id AND ss.producto_id = p.id);

INSERT INTO stock_sucursal (sucursal_id, producto_id, cantidad, stock_minimo, version)
SELECT s.id, p.id, 35, 15, 0
FROM sucursal s, producto p
WHERE s.nombre = 'Sucursal Providencia' AND p.nombre = 'Fideos Spaghetti'
AND NOT EXISTS (SELECT 1 FROM stock_sucursal ss WHERE ss.sucursal_id = s.id AND ss.producto_id = p.id);

INSERT INTO stock_sucursal (sucursal_id, producto_id, cantidad, stock_minimo, version)
SELECT s.id, p.id, 40, 10, 0
FROM sucursal s, producto p
WHERE s.nombre = 'Sucursal Providencia' AND p.nombre = 'Leche Entera 1L'
AND NOT EXISTS (SELECT 1 FROM stock_sucursal ss WHERE ss.sucursal_id = s.id AND ss.producto_id = p.id);

INSERT INTO stock_sucursal (sucursal_id, producto_id, cantidad, stock_minimo, version)
SELECT s.id, p.id, 20, 5, 0
FROM sucursal s, producto p
WHERE s.nombre = 'Sucursal Providencia' AND p.nombre = 'Jugo de Naranja'
AND NOT EXISTS (SELECT 1 FROM stock_sucursal ss WHERE ss.sucursal_id = s.id AND ss.producto_id = p.id);

-- Sucursal Las Condes
INSERT INTO stock_sucursal (sucursal_id, producto_id, cantidad, stock_minimo, version)
SELECT s.id, p.id, 30, 10, 0
FROM sucursal s, producto p
WHERE s.nombre = 'Sucursal Las Condes' AND p.nombre = 'Galletas de Chocolate'
AND NOT EXISTS (SELECT 1 FROM stock_sucursal ss WHERE ss.sucursal_id = s.id AND ss.producto_id = p.id);

INSERT INTO stock_sucursal (sucursal_id, producto_id, cantidad, stock_minimo, version)
SELECT s.id, p.id, 50, 15, 0
FROM sucursal s, producto p
WHERE s.nombre = 'Sucursal Las Condes' AND p.nombre = 'Fideos Spaghetti'
AND NOT EXISTS (SELECT 1 FROM stock_sucursal ss WHERE ss.sucursal_id = s.id AND ss.producto_id = p.id);

-- Deliberadamente BAJO el mínimo, para poder probar la reposición automática al vender
INSERT INTO stock_sucursal (sucursal_id, producto_id, cantidad, stock_minimo, version)
SELECT s.id, p.id, 3, 10, 0
FROM sucursal s, producto p
WHERE s.nombre = 'Sucursal Las Condes' AND p.nombre = 'Leche Entera 1L'
AND NOT EXISTS (SELECT 1 FROM stock_sucursal ss WHERE ss.sucursal_id = s.id AND ss.producto_id = p.id);

INSERT INTO stock_sucursal (sucursal_id, producto_id, cantidad, stock_minimo, version)
SELECT s.id, p.id, 12, 5, 0
FROM sucursal s, producto p
WHERE s.nombre = 'Sucursal Las Condes' AND p.nombre = 'Jugo de Naranja'
AND NOT EXISTS (SELECT 1 FROM stock_sucursal ss WHERE ss.sucursal_id = s.id AND ss.producto_id = p.id);

-- Promoción GLOBAL: 15% de descuento en Leche Entera, en TODAS las sucursales
INSERT INTO promociones (producto_id, sucursal_id, tipo_descuento, valor, fecha_inicio, fecha_fin, activa)
VALUES (3, NULL, 'PORCENTAJE', 15.0, '2026-07-01', '2026-07-31', true);

-- Promoción LOCAL: $200 de descuento fijo en Leche Entera, SOLO en Las Condes (sucursal_id = 3)
INSERT INTO promociones (producto_id, sucursal_id, tipo_descuento, valor, fecha_inicio, fecha_fin, activa)
VALUES (3, 3, 'MONTO_FIJO', 200.0, '2026-07-01', '2026-07-31', true);

