# Casos de Prueba — SIGIV-SM (AP2)

**Documento complementario al** [`plan-de-pruebas.md`](plan-de-pruebas.md).
**Autor:** Ciro Urrustarazu — UES21.
**Versión:** 1.0 — 2026-05-17.

Cada caso detalla precondiciones, datos, pasos numerados, resultado esperado, resultado obtenido y estado, según las técnicas de diseño descritas en el plan de pruebas (sección 4.2).

**Leyenda de estado:** ✅ Aprobado · ⚠️ Aprobado con observaciones · ❌ Fallado · ⏳ No ejecutado.
**Leyenda de prioridad:** A = Alta · M = Media · B = Baja.

---

## CP01 — Login con credenciales válidas

| Atributo | Valor |
|---|---|
| **Módulo** | Seguridad |
| **CU relacionado** | CU01 — Iniciar sesión |
| **RF / RNF cubiertos** | RF09 (Roles), RNF03 (Seguridad contraseñas) |
| **Prioridad** | A |
| **Tipo** | Funcional / Seguridad |
| **Nivel** | Integración (SmokeTest) + Sistema (UI) |

**Precondiciones:**
- Usuario `admin` existe en `usuarios` con `password_hash` = BCrypt(`admin123`) y rol ADMIN activo.

**Datos de prueba:**
- Usuario: `admin`
- Contraseña: `admin123`

**Pasos:**
1. Abrir la aplicación (`VentanaLogin`).
2. Ingresar `admin` en el campo "Usuario".
3. Ingresar `admin123` en el campo "Contraseña".
4. Presionar el botón "Ingresar".

**Resultado esperado:**
- Se cierra `VentanaLogin` y se abre `VentanaPrincipal`.
- `ServicioAuth.login()` retorna un objeto `Usuario` con `rol = "ADMIN"`.
- Se registra el evento de login en `bitacora`.

**Resultado obtenido:** ✅ Sesión iniciada correctamente; menú completo visible para rol ADMIN.

---

## CP02 — Login con contraseña inválida

| Atributo | Valor |
|---|---|
| **Módulo** | Seguridad |
| **CU relacionado** | CU01 — Iniciar sesión (FA1) |
| **RF / RNF cubiertos** | RNF03 (BCrypt valida hash) |
| **Prioridad** | A |
| **Tipo** | Funcional / Seguridad |
| **Nivel** | Integración (SmokeTest) |

**Precondiciones:** usuario `admin` activo.

**Datos de prueba:**
- Usuario: `admin`
- Contraseña: `password-incorrecto`

**Pasos:**
1. Abrir `VentanaLogin`.
2. Ingresar `admin` / `password-incorrecto`.
3. Presionar "Ingresar".

**Resultado esperado:**
- `ServicioAuth.login()` lanza `AutenticacionException`.
- La UI muestra el mensaje "Usuario o contraseña inválidos" sin detalle adicional (no revelar si el usuario existe — RNF03).
- Sesión no iniciada.

**Resultado obtenido:** ✅ Excepción capturada y mostrada con mensaje genérico.

---

## CP03 — Login con usuario inexistente

| Atributo | Valor |
|---|---|
| **Módulo** | Seguridad |
| **CU relacionado** | CU01 — Iniciar sesión (FA2) |
| **RF / RNF cubiertos** | RNF03 |
| **Prioridad** | M |
| **Tipo** | Funcional |
| **Nivel** | Integración |

**Datos de prueba:**
- Usuario: `inexistente_xyz`
- Contraseña: `cualquiera`

**Pasos:**
1. Ingresar credenciales inexistentes en `VentanaLogin`.
2. Presionar "Ingresar".

**Resultado esperado:**
- Mismo mensaje genérico que CP02 (no revelar la causa exacta).
- Sin sesión iniciada.

**Resultado obtenido:** ✅ Comportamiento idéntico a CP02, mensaje genérico.

---

## CP04 — Listar productos activos

| Atributo | Valor |
|---|---|
| **Módulo** | Productos / Inventario |
| **CU relacionado** | CU02 — Consultar productos |
| **RF / RNF cubiertos** | RF01 (ABM productos) |
| **Prioridad** | M |
| **Tipo** | Funcional |
| **Nivel** | Integración |

**Precondiciones:** seed con 14 productos activos cargado (`datos-de-prueba.sql`).

**Pasos:**
1. Iniciar sesión como `admin`.
2. Abrir `PanelProductos`.
3. La lista se carga automáticamente al abrir el panel.

**Resultado esperado:**
- Se devuelve una lista no vacía con los 14 productos del seed.
- Cada fila muestra `codigo`, `descripcion`, `rubro.nombre`, `precio_venta` y `stock_actual` (consulta C1 de `consultas-tp2.sql`).
- Productos con `activo = FALSE` no aparecen.

**Resultado obtenido:** ✅ 14 filas listadas, productos inactivos filtrados correctamente.

---

## CP05 — Alta de producto con código duplicado

| Atributo | Valor |
|---|---|
| **Módulo** | Productos / Inventario |
| **CU relacionado** | CU03 — Alta de producto (excepción) |
| **RF / RNF cubiertos** | RF01, integridad de unicidad |
| **Prioridad** | M |
| **Tipo** | Funcional / Integridad |
| **Nivel** | Integración |

**Precondiciones:** existe producto con `codigo = 'TOR-001'`.

**Datos de prueba:**
- Código: `TOR-001` (duplicado)
- Descripción: `Tornillo otro proveedor`
- Rubro: 4, Proveedor: 2, costo: 10.00, venta: 18.00, stock: 100, stock_min: 10.

**Pasos:**
1. Abrir `PanelProductos` → "Nuevo".
2. Completar el formulario con los datos de prueba.
3. Guardar.

**Resultado esperado:**
- MySQL rechaza la inserción por violación de `UNIQUE (codigo)`.
- `ProductoDAO` lanza `SQLIntegrityConstraintViolationException`.
- La UI muestra "Ya existe un producto con ese código".
- No se inserta ninguna fila nueva.

**Resultado obtenido:** ✅ Producto no insertado; mensaje claro en UI.

---

## CP06 — Venta simple en efectivo

| Atributo | Valor |
|---|---|
| **Módulo** | Ventas |
| **CU relacionado** | CU04 — Registrar venta (flujo principal, sin cta. cte.) |
| **RF / RNF cubiertos** | RF02 (Registrar venta) |
| **Prioridad** | A |
| **Tipo** | Funcional / Transaccional |
| **Nivel** | Integración + Sistema |

**Precondiciones:**
- Sesión iniciada como `vendedor1`.
- Productos disponibles: `TOR-001` (stock = 500), `PIN-010` (stock = 80).

**Datos de prueba:**
- Línea 1: producto `TOR-001`, cantidad 10, precio unitario 22.00 → subtotal 220.00.
- Línea 2: producto `PIN-010`, cantidad 2, precio unitario 1500.00 → subtotal 3000.00.
- Forma de pago: `EFECTIVO`.
- Total esperado: 3220.00.

**Pasos:**
1. Abrir `PanelVenta` → "Nueva venta".
2. Agregar línea 1 (cantidad 10 de TOR-001).
3. Agregar línea 2 (cantidad 2 de PIN-010).
4. Verificar que el total acumulado es 3220.00.
5. Seleccionar forma de pago "Efectivo".
6. Confirmar la venta.

**Resultado esperado:**
- Se inserta una fila en `ventas` con `total = 3220.00`, `estado = 'CONFIRMADA'`.
- Se insertan 2 filas en `detalle_ventas` con los subtotales correspondientes.
- `productos.stock_actual` queda en 490 (TOR-001) y 78 (PIN-010).
- No se inserta movimiento en `movimientos_cta_cte`.
- Se inserta entrada en `bitacora`.
- COMMIT exitoso.

**Resultado obtenido:** ✅ Venta confirmada, stocks descontados, bitácora con registro.

---

## CP07 — Venta a cuenta corriente válida

| Atributo | Valor |
|---|---|
| **Módulo** | Ventas / Cuentas corrientes |
| **CU relacionado** | CU04 — Registrar venta (flujo principal, con cta. cte.) |
| **RF / RNF cubiertos** | RF02, RF03 (Venta a crédito) |
| **Prioridad** | A |
| **Tipo** | Funcional / Transaccional |
| **Nivel** | Integración (SmokeTest) + Sistema |

**Precondiciones:**
- Cliente `Juan Albañil` (id=1): `tiene_cta_cte = TRUE`, `limite_credito = 50000.00`, `saldo_cta_cte = 0.00`.
- Producto `TOR-001` con stock ≥ 10.

**Datos de prueba:**
- Cliente: id=1.
- Línea 1: `TOR-001` × 10 unidades × 540.00 → subtotal 5400.00.
- Forma de pago: `CTA_CTE`.

**Pasos:**
1. Iniciar nueva venta.
2. Asociar cliente Juan Albañil.
3. Agregar línea TOR-001 × 10.
4. Seleccionar `CTA_CTE`.
5. Confirmar.

**Resultado esperado (todas las operaciones dentro de una transacción atómica):**
- `ventas`: nueva fila con `forma_pago = 'CTA_CTE'`, `total = 5400.00`.
- `detalle_ventas`: 1 fila.
- `productos.stock_actual` decrece en 10.
- `clientes.saldo_cta_cte` pasa de 0.00 a 5400.00.
- `movimientos_cta_cte`: nueva fila tipo `DEBE`, monto 5400.00, referenciando la venta.
- `bitacora`: entrada de auditoría.
- COMMIT exitoso.

**Resultado obtenido:** ✅ Las 5 escrituras quedaron consistentes; saldo del cliente actualizado correctamente.

---

## CP08 — Venta con stock insuficiente (rollback)

| Atributo | Valor |
|---|---|
| **Módulo** | Ventas |
| **CU relacionado** | CU04 — Flujo alternativo FA1 |
| **RF / RNF cubiertos** | RF02, integridad de stock |
| **Prioridad** | A |
| **Tipo** | Funcional / Integridad |
| **Nivel** | Integración (SmokeTest) |

**Precondiciones:**
- Producto `PIN-LIM` (creado por el seed de pruebas) con `stock_actual = 3`, `stock_minimo = 5`.

**Datos de prueba:**
- Línea: `PIN-LIM` × 5 unidades (cantidad > stock).
- Forma de pago: `EFECTIVO`.

**Pasos:**
1. Iniciar nueva venta.
2. Agregar `PIN-LIM` con cantidad 5.
3. Confirmar.

**Resultado esperado:**
- El `CHECK (stock_actual >= 0)` o la validación previa en `ServicioVenta` rechaza la operación.
- `ServicioVenta.registrarVenta()` ejecuta `conn.rollback()`.
- **Ninguna escritura queda persistida** (ni en `ventas`, ni en `detalle_ventas`, ni en `productos`).
- La UI muestra "Stock insuficiente para PIN-LIM (disponible: 3, solicitado: 5)".

**Resultado obtenido:** ✅ Rollback aplicado; verificado con `SELECT COUNT(*) FROM ventas WHERE total = ...` antes/después.

---

## CP09 — Venta a cta. cte. excediendo crédito disponible

| Atributo | Valor |
|---|---|
| **Módulo** | Ventas / Cuentas corrientes |
| **CU relacionado** | CU04 — Flujo alternativo FA2 |
| **RF / RNF cubiertos** | RF03, regla de negocio de crédito |
| **Prioridad** | A |
| **Tipo** | Funcional |
| **Nivel** | Integración |

**Precondiciones:**
- Cliente `Maria Pinta` (id=2): `tiene_cta_cte = TRUE`, `limite_credito = 10000.00`, `saldo_cta_cte = 9500.00` (crédito disponible 500.00).
- Producto con precio total de venta > 500.00.

**Datos de prueba:**
- Cliente: id=2.
- Línea: cualquier producto que totalice 800.00 (excede el crédito en 300.00).
- Forma de pago: `CTA_CTE`.

**Pasos:**
1. Iniciar nueva venta y asociar cliente Maria Pinta.
2. Agregar línea por 800.00.
3. Seleccionar `CTA_CTE`.
4. Confirmar.

**Resultado esperado:**
- Validación **previa** a la apertura de la transacción rechaza la venta.
- Mensaje: "El cliente excedería su límite de crédito (disponible: $500.00, requerido: $800.00). Seleccione otra forma de pago."
- Ninguna escritura en BD.
- El stock del producto **no se modifica** (no se entra a la transacción).

**Resultado obtenido:** ✅ Validación temprana funcionó; el sistema sugiere cambiar forma de pago.

---

## CP10 — Anulación de venta (rol Administrador)

| Atributo | Valor |
|---|---|
| **Módulo** | Ventas |
| **CU relacionado** | CU05 — Anular venta |
| **RF / RNF cubiertos** | RF09 (Roles — solo ADMIN puede anular) |
| **Prioridad** | M |
| **Tipo** | Funcional / Autorización |
| **Nivel** | Integración + Sistema |

**Precondiciones:**
- Sesión iniciada como `admin`.
- Existe una venta `id=100` con `estado='CONFIRMADA'` (creada por el seed o por CP06).

**Pasos:**
1. Abrir el listado de ventas en `PanelVenta`.
2. Seleccionar la venta id=100.
3. Presionar "Anular".
4. Confirmar el diálogo de seguridad.

**Resultado esperado:**
- `ventas.estado` pasa de `'CONFIRMADA'` a `'ANULADA'`.
- Se registra entrada en `bitacora` con `accion = 'ANULAR_VENTA'`, usuario admin y timestamp.
- **No se borra** la venta (preservación histórica).
- *Nota:* la reversión de stock y saldo de cta. cte. es responsabilidad de un flujo posterior fuera del alcance de este CP (queda como mejora documentada).

**Resultado obtenido:** ✅ Estado actualizado, bitácora con registro. Verificado que un vendedor (no admin) ve el botón "Anular" deshabilitado.

---

## CP11 — Caída de la BD durante una venta (rollback automático)

| Atributo | Valor |
|---|---|
| **Módulo** | Ventas / Conexión BD |
| **CU relacionado** | CU04 — Excepción E1 |
| **RF / RNF cubiertos** | RNF01 (Confiabilidad), integridad transaccional |
| **Prioridad** | A |
| **Tipo** | No funcional / Confiabilidad |
| **Nivel** | Sistema (manual) |

**Precondiciones:**
- Sesión iniciada.
- Producto con stock suficiente.

**Pasos:**
1. Iniciar una venta con varias líneas.
2. Antes de presionar "Confirmar", **detener manualmente** el servicio `mysqld` (`sudo systemctl stop mysql` en Linux, o equivalente).
3. Presionar "Confirmar".

**Resultado esperado:**
- `ConexionBD.getConnection()` falla con `SQLException` (host unreachable / connection refused).
- El bloque `catch` de `ServicioVenta.registrarVenta()` ejecuta `rollback()` sobre la conexión (si llegó a abrirla) o simplemente propaga la excepción si no hubo conexión.
- Ninguna fila parcial queda en BD.
- La UI muestra "Sin conexión con la base. Reintente en unos minutos."
- Al reiniciar `mysqld` y reintentar, la venta se registra normalmente.

**Resultado obtenido:** ✅ Sin datos huérfanos; verificado consultando `ventas` y `detalle_ventas` tras restaurar la BD.

---

## CP12 — Alerta de productos bajo stock mínimo

| Atributo | Valor |
|---|---|
| **Módulo** | Productos / Inventario |
| **CU relacionado** | CU06 — Consultar alertas de stock |
| **RF / RNF cubiertos** | RF07 (Alerta stock mínimo) |
| **Prioridad** | M |
| **Tipo** | Funcional / Reporte |
| **Nivel** | Integración |

**Precondiciones:**
- Existe al menos un producto con `stock_actual <= stock_minimo` (el seed crea `PIN-LIM` con stock 3, mínimo 5).

**Pasos:**
1. Iniciar sesión.
2. En el menú principal seleccionar "Alertas de stock".
3. Se ejecuta la consulta C2 de `consultas-tp2.sql`.

**Resultado esperado:**
- Lista contiene `PIN-LIM` con `faltante = 2`.
- Productos con `stock_actual > stock_minimo` no aparecen.
- Productos inactivos (`activo = FALSE`) no aparecen aunque tengan stock bajo.
- Orden descendente por `faltante`.

**Resultado obtenido:** ✅ Al menos un producto en la alerta; consulta correctamente filtrada y ordenada.

---

# Matriz de trazabilidad: Casos de prueba × Requisitos

| Requisito | Descripción breve | Casos que lo verifican | Cobertura |
|---|---|---|---|
| **RF01** | ABM de productos | CP04, CP05 | ✅ |
| **RF02** | Registrar venta | CP06, CP07, CP08, CP11 | ✅ |
| **RF03** | Venta a crédito (cta. cte.) | CP07, CP09 | ✅ |
| **RF07** | Alerta de stock mínimo | CP12 | ✅ |
| **RF09** | Control de acceso por roles | CP01, CP10 | ✅ |
| **RNF01** | Confiabilidad / transaccionalidad | CP08, CP11 | ✅ |
| **RNF03** | Seguridad de contraseñas (BCrypt) | CP01, CP02, CP03 | ✅ |

**Requerimientos no cubiertos en esta iteración (deferidos a la siguiente fase del PUD):**

- RF04 (Registrar compra), RF05 (Pago a proveedor), RF06 (Reporte de cuenta corriente), RF08 (Reportes gerenciales detallados), RF10 (Backup automático manual desde la UI).
- RNF02 (Performance bajo carga), RNF04 (Internacionalización), RNF05 (Backup automático — solo a nivel de definición de plan), RNF06 (Usabilidad medida), RNF07 (Portabilidad probada), RNF08 (Mantenibilidad).

---

# Resumen ejecutivo

| Métrica | Valor |
|---|---|
| Total de casos diseñados | 12 |
| Casos de prioridad Alta | 5 (CP01, CP06, CP07, CP08, CP11) |
| Casos de prioridad Media | 6 (CP02, CP03, CP04, CP05, CP10, CP12) |
| Casos ejecutados | 12 |
| Casos aprobados | 12 ✅ |
| Casos con observaciones | 0 |
| Casos fallidos | 0 |
| **Estado de la fase de pruebas** | **Aprobada (cumple criterios de salida 5.2 del plan).** |
