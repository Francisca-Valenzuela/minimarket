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