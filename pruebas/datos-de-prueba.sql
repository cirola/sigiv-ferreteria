-- =====================================================================
-- SIGIV-SM - Datos de prueba reproducibles para AP2
-- =====================================================================
-- Proposito: poblar la BD con los datos necesarios para ejecutar los
--            casos CP01 a CP12 documentados en pruebas/casos-de-prueba.md.
--
-- Requisitos previos:
--   1. Ejecutar database/schema.sql (crea las 13 tablas e indices).
--   2. Ejecutar database/datos-iniciales.sql (catalogos base: roles,
--      rubros, proveedores, usuarios admin/vendedor1).
--   3. Recien despues correr este script para los datos especificos
--      de los escenarios de prueba.
--
-- IMPORTANTE: este script es idempotente. Antes de insertar limpia las
-- tablas relacionadas con las pruebas, para que pueda re-ejecutarse N
-- veces sin contaminar los datos.
-- =====================================================================

USE sigiv_ferreteria;

-- ---------------------------------------------------------------------
-- 0) LIMPIEZA DE DATOS DE PRUEBAS PREVIOS
-- ---------------------------------------------------------------------
-- Solo afecta filas creadas por este seed; los catalogos base se
-- preservan. Se eliminan en orden inverso al de dependencias (FK).

DELETE FROM movimientos_cta_cte WHERE descripcion LIKE 'SEED-PRUEBAS%';
DELETE FROM detalle_ventas      WHERE venta_id IN (SELECT id FROM ventas WHERE observacion LIKE 'SEED-PRUEBAS%');
DELETE FROM ventas              WHERE observacion LIKE 'SEED-PRUEBAS%';
DELETE FROM bitacora            WHERE detalle LIKE 'SEED-PRUEBAS%';

-- Clientes creados solo para pruebas (identificados por documento ficticio)
DELETE FROM clientes WHERE documento IN ('20-30111222-3', '27-30222333-4', '20-30333444-5');

-- Productos creados solo para pruebas (codigos PIN-LIM, TOR-001, PIN-010)
DELETE FROM productos WHERE codigo IN ('TOR-001', 'PIN-010', 'PIN-LIM');


-- ---------------------------------------------------------------------
-- 1) PRODUCTOS DE PRUEBA
-- ---------------------------------------------------------------------
-- Se asume que existen los rubros y proveedores del seed inicial:
--   rubros.id = 1 (Herramientas), 2 (Pinturas), 3 (Sanitarios),
--               4 (Tornilleria), ...
--   proveedores.id = 1, 2

-- P1: producto con stock alto - usado en CP04, CP06, CP07
INSERT INTO productos (codigo, descripcion, rubro_id, proveedor_id,
                       precio_costo, precio_venta, stock_actual, stock_minimo, activo)
VALUES ('TOR-001', 'Tornillo autoperforante 6x1" (SEED PRUEBAS)', 4, 1,
        12.50, 540.00, 500, 50, TRUE);

-- P2: producto de mayor valor unitario - usado en CP06
INSERT INTO productos (codigo, descripcion, rubro_id, proveedor_id,
                       precio_costo, precio_venta, stock_actual, stock_minimo, activo)
VALUES ('PIN-010', 'Pintura latex blanco 10L (SEED PRUEBAS)', 2, 2,
        900.00, 1500.00, 80, 10, TRUE);

-- P3: producto en stock CRITICO (debajo del minimo) - usado en CP08 y CP12
INSERT INTO productos (codigo, descripcion, rubro_id, proveedor_id,
                       precio_costo, precio_venta, stock_actual, stock_minimo, activo)
VALUES ('PIN-LIM', 'Pintura sintetica 1L (SEED PRUEBAS - stock bajo)', 2, 2,
        450.00, 800.00, 3, 5, TRUE);


-- ---------------------------------------------------------------------
-- 2) CLIENTES DE PRUEBA
-- ---------------------------------------------------------------------

-- C1: cliente con cta. cte. y credito disponible - usado en CP07
INSERT INTO clientes (nombre, documento, telefono, tiene_cta_cte,
                      limite_credito, saldo_cta_cte, activo)
VALUES ('Juan Albanil', '20-30111222-3', '3514567890', TRUE,
        50000.00, 0.00, TRUE);

-- C2: cliente con cta. cte. cerca del limite - usado en CP09
INSERT INTO clientes (nombre, documento, telefono, tiene_cta_cte,
                      limite_credito, saldo_cta_cte, activo)
VALUES ('Maria Pinta', '27-30222333-4', '3514568901', TRUE,
        10000.00, 9500.00, TRUE);

-- C3: cliente sin cta. cte. - usado para validar rechazo en flujo CTA_CTE
INSERT INTO clientes (nombre, documento, telefono, tiene_cta_cte,
                      limite_credito, saldo_cta_cte, activo)
VALUES ('Pedro Cliente Ocasional', '20-30333444-5', '3514569012', FALSE,
        0.00, 0.00, TRUE);


-- ---------------------------------------------------------------------
-- 3) VENTA DE PRUEBA YA EXISTENTE (para CP10 - Anulacion)
-- ---------------------------------------------------------------------
-- Se inserta una venta confirmada que el caso CP10 anula.
-- Se ejecuta como bloque atomico (igual que la aplicacion).

START TRANSACTION;

INSERT INTO ventas (usuario_id, cliente_id, forma_pago, total, estado, observacion)
SELECT u.id, c.id, 'CTA_CTE', 2700.00, 'CONFIRMADA', 'SEED-PRUEBAS venta para anular en CP10'
  FROM usuarios u, clientes c
 WHERE u.nombre_usuario = 'vendedor1'
   AND c.documento = '20-30111222-3'
 LIMIT 1;

SET @venta_seed = LAST_INSERT_ID();

INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario, subtotal)
SELECT @venta_seed, p.id, 5, 540.00, 2700.00
  FROM productos p
 WHERE p.codigo = 'TOR-001';

-- Actualiza stock conforme la venta
UPDATE productos SET stock_actual = stock_actual - 5 WHERE codigo = 'TOR-001';

-- Actualiza saldo del cliente Juan Albanil
UPDATE clientes SET saldo_cta_cte = saldo_cta_cte + 2700.00
 WHERE documento = '20-30111222-3';

INSERT INTO movimientos_cta_cte (cliente_id, tipo, monto, descripcion, venta_id)
SELECT c.id, 'DEBE', 2700.00, 'SEED-PRUEBAS Venta inicial para escenario CP10', @venta_seed
  FROM clientes c WHERE c.documento = '20-30111222-3';

COMMIT;


-- ---------------------------------------------------------------------
-- 4) VERIFICACION RAPIDA DEL SEED (no modifica datos)
-- ---------------------------------------------------------------------
-- Consultas para validar que el seed quedo cargado correctamente.
-- Ejecutar luego del script para chequear cantidades esperadas.

SELECT 'Productos de prueba' AS chequeo,
       COUNT(*) AS filas,
       'esperado: 3' AS esperado
  FROM productos
 WHERE codigo IN ('TOR-001', 'PIN-010', 'PIN-LIM');

SELECT 'Clientes de prueba' AS chequeo,
       COUNT(*) AS filas,
       'esperado: 3' AS esperado
  FROM clientes
 WHERE documento IN ('20-30111222-3', '27-30222333-4', '20-30333444-5');

SELECT 'Ventas seed (para CP10)' AS chequeo,
       COUNT(*) AS filas,
       'esperado: 1' AS esperado
  FROM ventas
 WHERE observacion LIKE 'SEED-PRUEBAS%';

SELECT 'Producto en stock bajo (CP08, CP12)' AS chequeo,
       codigo, stock_actual, stock_minimo,
       (stock_minimo - stock_actual) AS faltante
  FROM productos
 WHERE codigo = 'PIN-LIM';

SELECT 'Cliente cerca de limite (CP09)' AS chequeo,
       nombre, limite_credito, saldo_cta_cte,
       (limite_credito - saldo_cta_cte) AS credito_disponible
  FROM clientes
 WHERE documento = '27-30222333-4';

-- =====================================================================
-- FIN DEL SCRIPT DE DATOS DE PRUEBA
-- =====================================================================
