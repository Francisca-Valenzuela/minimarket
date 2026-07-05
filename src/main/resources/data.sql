-- Insertar Categorías de prueba
INSERT INTO categoria (nombre) VALUES ('Abarrotes');
INSERT INTO categoria (nombre) VALUES ('Lácteos');
INSERT INTO categoria (nombre) VALUES ('Bebidas');

-- Insertar Productos de prueba
-- Nota: 'version' es obligatorio por tu anotación @Version en la entidad Producto (control de concurrencia)
INSERT INTO producto (nombre, precio, stock, version, categoria_id) VALUES ('Galletas de Chocolate', 1500.0, 20, 0, 1);
INSERT INTO producto (nombre, precio, stock, version, categoria_id) VALUES ('Fideos Spaghetti', 990.0, 50, 0, 1);
INSERT INTO producto (nombre, precio, stock, version, categoria_id) VALUES ('Leche Entera 1L', 1200.0, 30, 0, 2);
INSERT INTO producto (nombre, precio, stock, version, categoria_id) VALUES ('Jugo de Naranja', 1800.0, 15, 0, 3);