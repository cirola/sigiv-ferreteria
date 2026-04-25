-- =====================================================================
-- SIGIV - Datos iniciales / seed
-- Ejecutar luego de schema.sql
-- =====================================================================

USE sigiv_ferreteria;

-- Roles
INSERT INTO roles (nombre, descripcion) VALUES
  ('ADMIN', 'Administrador con acceso total'),
  ('VENDEDOR', 'Operador de ventas con acceso restringido');

-- Usuarios (password en texto plano solo para seed; la app usa BCrypt al registrar)
-- Password "admin123" hasheado con BCrypt cost 10
INSERT INTO usuarios (nombre_usuario, nombre_completo, password_hash, rol_id) VALUES
  ('admin', 'Ciro Urrustarazu',
   '$2a$10$9UHLvE60K3TiKW1MnyCByuLg6wZRxugbjH5B6wfh8pis7XDOha4l.',
   (SELECT id FROM roles WHERE nombre='ADMIN')),
  ('vendedor1', 'Juan Pérez',
   '$2a$10$9UHLvE60K3TiKW1MnyCByuLg6wZRxugbjH5B6wfh8pis7XDOha4l.',
   (SELECT id FROM roles WHERE nombre='VENDEDOR'));

-- Rubros
INSERT INTO rubros (nombre, margen_default) VALUES
  ('Herramientas manuales', 25.00),
  ('Herramientas eléctricas', 20.00),
  ('Electricidad', 35.00),
  ('Sanitarios', 30.00),
  ('Pinturas', 40.00),
  ('Bulonería', 50.00),
  ('Construcción', 25.00);

-- Proveedores
INSERT INTO proveedores (razon_social, cuit, telefono, email, direccion) VALUES
  ('Distribuidora Central SA', '30-12345678-9', '351-4445566', 'ventas@distcentral.com.ar', 'Av. Colón 1200, Córdoba'),
  ('Herramientas del Sur SRL', '30-98765432-1', '351-6667788', 'pedidos@hsur.com.ar', 'Bv. San Juan 450, Córdoba'),
  ('Pinturerías Córdoba', '30-55443322-8', '351-2223344', 'info@pintcba.com.ar', 'Rivadavia 800, Córdoba');

-- Productos (mezcla de rubros)
INSERT INTO productos (codigo, descripcion, rubro_id, proveedor_id, precio_costo, precio_venta, stock_actual, stock_minimo) VALUES
  ('HM-001', 'Martillo carpintero 500g', 1, 2, 3200.00, 4000.00, 15, 5),
  ('HM-002', 'Destornillador Phillips #2', 1, 2, 800.00, 1200.00, 40, 10),
  ('HM-003', 'Pinza universal 8"', 1, 2, 2500.00, 3500.00, 8, 5),
  ('HE-001', 'Taladro percutor 600W', 2, 2, 45000.00, 55000.00, 3, 2),
  ('HE-002', 'Amoladora angular 4.5"', 2, 2, 38000.00, 48000.00, 4, 2),
  ('EL-001', 'Cable 2.5mm x metro', 3, 1, 250.00, 380.00, 500, 100),
  ('EL-002', 'Tomacorriente doble', 3, 1, 950.00, 1500.00, 25, 10),
  ('EL-003', 'Lámpara LED 9W', 3, 1, 1200.00, 1900.00, 60, 20),
  ('SA-001', 'Canilla de cocina', 4, 1, 8500.00, 11500.00, 6, 3),
  ('PI-001', 'Pintura látex blanca 20L', 5, 3, 18000.00, 26000.00, 10, 4),
  ('PI-002', 'Esmalte sintético 1L blanco', 5, 3, 4500.00, 6500.00, 15, 5),
  ('BU-001', 'Tornillo autoperforante 3"', 6, 1, 30.00, 50.00, 2000, 500),
  ('BU-002', 'Tuerca hexagonal 1/4"', 6, 1, 20.00, 35.00, 3000, 500),
  ('CO-001', 'Bolsa de cemento 50kg', 7, 1, 12000.00, 15500.00, 20, 10);

-- Clientes
INSERT INTO clientes (nombre, documento, telefono, email, direccion, tiene_cta_cte, limite_credito, saldo_cta_cte) VALUES
  ('Consumidor Final', NULL, NULL, NULL, NULL, FALSE, 0, 0),
  ('Carlos Fernández (Albañil)', '20-23456789-3', '351-1112233', 'cfernandez@gmail.com', 'Barrio Alberdi', TRUE, 100000.00, 35000.00),
  ('Electricistas Unidos SRL', '30-77889911-4', '351-4455667', 'contacto@eunidos.com.ar', 'Av. Vélez Sarsfield 2200', TRUE, 250000.00, 87500.00),
  ('María González', '27-34567890-1', '351-5566778', NULL, 'Barrio Cerro', FALSE, 0, 0);

-- Movimientos iniciales de cta cte (coherentes con los saldos)
INSERT INTO movimientos_cta_cte (cliente_id, tipo, monto, descripcion) VALUES
  (2, 'DEBE', 35000.00, 'Saldo inicial migrado de cuaderno'),
  (3, 'DEBE', 87500.00, 'Saldo inicial migrado de cuaderno');
