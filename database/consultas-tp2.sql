-- =====================================================================
-- SIGIV - Consultas SQL representativas (TP2)
-- Base: sigiv_ferreteria (ver TP1/sigiv-ferreteria/database/schema.sql)
-- =====================================================================

USE sigiv_ferreteria;

-- =====================================================================
-- A) DDL - Definicion (extracto, ya presentado en TP1)
-- =====================================================================
-- Las sentencias CREATE TABLE completas estan en database/schema.sql del repo.
-- Aqui mostramos ejemplos puntuales de DDL "incremental":

-- A1) Vista materializada en logica: top productos del mes
CREATE OR REPLACE VIEW v_top_productos_mes AS
SELECT p.id, p.codigo, p.descripcion,
       SUM(dv.cantidad) AS unidades_vendidas,
       SUM(dv.subtotal) AS facturado
  FROM detalle_ventas dv
  JOIN ventas v ON v.id = dv.venta_id
  JOIN productos p ON p.id = dv.producto_id
 WHERE v.estado = 'CONFIRMADA'
   AND v.fecha >= DATE_FORMAT(CURDATE(), '%Y-%m-01')
 GROUP BY p.id, p.codigo, p.descripcion
 ORDER BY unidades_vendidas DESC;

-- A2) Indice adicional para acelerar busqueda de ventas por fecha
CREATE INDEX IF NOT EXISTS idx_ventas_fecha ON ventas(fecha);


-- =====================================================================
-- B) INSERT - Ejemplos
-- =====================================================================

-- B1) Alta de un producto nuevo
INSERT INTO productos (codigo, descripcion, rubro_id, proveedor_id,
                       precio_costo, precio_venta, stock_actual, stock_minimo)
VALUES ('TOR-001', 'Tornillo autoperforante 6x1"', 4, 1,
        12.50, 22.00, 500, 50);

-- B2) Alta de un cliente con cuenta corriente
INSERT INTO clientes (nombre, documento, telefono, tiene_cta_cte, limite_credito)
VALUES ('Juan Albanil', '20-30111222-3', '3514567890', TRUE, 50000.00);

-- B3) Registro de una venta (cabecera + detalle + movimiento de cta. cte.)
--     En la aplicacion esta secuencia se ejecuta dentro de una transaccion.
START TRANSACTION;

INSERT INTO ventas (usuario_id, cliente_id, forma_pago, total)
VALUES (2, 1, 'CTA_CTE', 5400.00);
SET @venta = LAST_INSERT_ID();

INSERT INTO detalle_ventas (venta_id, producto_id, cantidad, precio_unitario, subtotal)
VALUES (@venta, 1, 10, 540.00, 5400.00);

UPDATE productos SET stock_actual = stock_actual - 10 WHERE id = 1;

UPDATE clientes SET saldo_cta_cte = saldo_cta_cte + 5400.00 WHERE id = 1;

INSERT INTO movimientos_cta_cte (cliente_id, tipo, monto, descripcion, venta_id)
VALUES (1, 'DEBE', 5400.00, 'Venta a credito', @venta);

COMMIT;


-- =====================================================================
-- C) SELECT - Consultas representativas (DQL)
-- =====================================================================

-- C1) Listado de productos activos con su rubro y proveedor (JOIN multi-tabla)
SELECT p.codigo, p.descripcion, r.nombre AS rubro,
       COALESCE(pr.razon_social, '-') AS proveedor,
       p.precio_venta, p.stock_actual
  FROM productos p
  JOIN rubros r           ON r.id = p.rubro_id
  LEFT JOIN proveedores pr ON pr.id = p.proveedor_id
 WHERE p.activo = TRUE
 ORDER BY r.nombre, p.descripcion;

-- C2) Productos por debajo del stock minimo (alerta de reposicion - RF07)
SELECT p.codigo, p.descripcion, p.stock_actual, p.stock_minimo,
       (p.stock_minimo - p.stock_actual) AS faltante
  FROM productos p
 WHERE p.activo = TRUE
   AND p.stock_actual <= p.stock_minimo
 ORDER BY faltante DESC;

-- C3) Ventas por dia en un rango de fechas (GROUP BY + agregados)
SELECT DATE(v.fecha) AS dia,
       COUNT(*)      AS cantidad_ventas,
       SUM(v.total)  AS facturacion_dia,
       AVG(v.total)  AS ticket_promedio
  FROM ventas v
 WHERE v.estado = 'CONFIRMADA'
   AND v.fecha BETWEEN '2026-05-01' AND '2026-05-31'
 GROUP BY DATE(v.fecha)
 ORDER BY dia;

-- C4) Top 10 productos mas vendidos por unidades (mes actual)
SELECT p.codigo, p.descripcion,
       SUM(dv.cantidad) AS unidades,
       SUM(dv.subtotal) AS facturado
  FROM detalle_ventas dv
  JOIN ventas v    ON v.id = dv.venta_id
  JOIN productos p ON p.id = dv.producto_id
 WHERE v.estado = 'CONFIRMADA'
   AND v.fecha >= DATE_FORMAT(CURDATE(), '%Y-%m-01')
 GROUP BY p.id, p.codigo, p.descripcion
 ORDER BY unidades DESC
 LIMIT 10;

-- C5) Clientes con cuenta corriente y saldo deudor
SELECT c.id, c.nombre, c.documento,
       c.limite_credito,
       c.saldo_cta_cte,
       (c.limite_credito - c.saldo_cta_cte) AS credito_disponible
  FROM clientes c
 WHERE c.tiene_cta_cte = TRUE
   AND c.saldo_cta_cte > 0
 ORDER BY c.saldo_cta_cte DESC;

-- C6) Stock valorizado por rubro (suma de costo x stock)
SELECT r.nombre AS rubro,
       COUNT(p.id)                            AS productos,
       SUM(p.stock_actual)                    AS unidades_total,
       SUM(p.stock_actual * p.precio_costo)   AS valor_costo,
       SUM(p.stock_actual * p.precio_venta)   AS valor_venta
  FROM productos p
  JOIN rubros r ON r.id = p.rubro_id
 WHERE p.activo = TRUE
 GROUP BY r.nombre
 ORDER BY valor_costo DESC;

-- C7) Compras por proveedor (ultimos 6 meses)
SELECT pr.razon_social,
       COUNT(c.id)   AS cantidad_compras,
       SUM(c.total)  AS total_comprado
  FROM compras c
  JOIN proveedores pr ON pr.id = c.proveedor_id
 WHERE c.fecha >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
 GROUP BY pr.id, pr.razon_social
 ORDER BY total_comprado DESC;

-- C8) Movimientos de cuenta corriente de un cliente (extracto)
SELECT m.fecha, m.tipo, m.monto, m.descripcion,
       SUM(CASE WHEN m.tipo='DEBE'  THEN  m.monto
                WHEN m.tipo='HABER' THEN -m.monto END)
       OVER (ORDER BY m.fecha, m.id) AS saldo_acumulado
  FROM movimientos_cta_cte m
 WHERE m.cliente_id = 1
 ORDER BY m.fecha, m.id;


-- =====================================================================
-- D) UPDATE - Ejemplos
-- =====================================================================

-- D1) Ajuste manual de precio de venta de un rubro (recargo del 10%)
UPDATE productos
   SET precio_venta = ROUND(precio_venta * 1.10, 2)
 WHERE rubro_id = (SELECT id FROM rubros WHERE nombre = 'Pinturas');

-- D2) Registro de pago parcial de cta. cte. (descontar saldo + asentar HABER)
START TRANSACTION;
UPDATE clientes SET saldo_cta_cte = saldo_cta_cte - 2000.00 WHERE id = 1;
INSERT INTO movimientos_cta_cte (cliente_id, tipo, monto, descripcion)
VALUES (1, 'HABER', 2000.00, 'Pago parcial en efectivo');
COMMIT;


-- =====================================================================
-- E) DELETE / baja logica
-- =====================================================================

-- E1) Baja LOGICA de un producto (preserva historico de ventas/compras)
UPDATE productos SET activo = FALSE WHERE codigo = 'TOR-001';

-- E2) Anular una venta (no la borra, cambia el estado)
UPDATE ventas SET estado = 'ANULADA' WHERE id = 25;

-- E3) Borrado fisico de detalle (solo durante depuracion / pruebas)
DELETE FROM detalle_ventas WHERE venta_id = 25;
-- Nota: ON DELETE CASCADE en la FK borra automaticamente al borrar la venta.

-- E4) Limpieza de bitacora antigua (politica de retencion 1 año)
DELETE FROM bitacora WHERE fecha < DATE_SUB(CURDATE(), INTERVAL 1 YEAR);
