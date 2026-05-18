# Plan de Pruebas — SIGIV-SM (AP2)

**Proyecto:** SIGIV-SM — Sistema de Gestión de Inventario y Ventas, Ferretería San Martín.
**Documento:** Plan de Pruebas del prototipo operacional (Actividad Práctica 2).
**Autor:** Ciro Urrustarazu — *Seminario de Práctica Informática*, Lic. Informática, UES21.
**Versión:** 1.0 — 2026-05-17.

---

## 1. Objetivos

El presente plan define la **estrategia, alcance, criterios y procedimientos** para verificar y validar el prototipo operacional del sistema SIGIV-SM entregado en la AP2.

**Objetivos específicos:**

1. Comprobar que los módulos implementados (Seguridad, Productos/Inventario, Ventas) cumplen con los requerimientos funcionales (RF01–RF10) y no funcionales (RNF01–RNF08) relevantes a esta iteración.
2. Verificar la integridad transaccional de las operaciones multi-tabla (registrar venta, anular venta, pago de cta. cte.).
3. Validar las reglas de negocio críticas: control de stock, límite de crédito de clientes, autenticación y roles.
4. Detectar defectos en etapa temprana antes de la entrega final del PUD (fase de Construcción).
5. Producir evidencia documental que respalde la calidad del prototipo entregado.

---

## 2. Alcance

### 2.1. Incluido en este plan

- **Módulos funcionales:** Seguridad (login, roles), Productos/Inventario (ABM, alertas de stock) y Ventas (registro, cuenta corriente, anulación).
- **Capas técnicas:** servicios (`ServicioAuth`, `ServicioProducto`, `ServicioVenta`), DAO (CRUD JDBC), persistencia (MySQL 8.x).
- **Reglas de negocio críticas:** stock no negativo, límite de crédito, atomicidad transaccional con rollback.

### 2.2. Fuera de alcance

- Pruebas exhaustivas de la UI Swing (se realizan inspecciones funcionales, no automatización con Selenium/equivalentes — fuera del alcance del prototipo).
- Pruebas de carga y stress (postergadas a la fase de Transición del PUD).
- Pruebas de penetración y seguridad ofensiva (cubiertas a nivel de inspección de configuración: BCrypt, usuario BD limitado).
- Módulos de Compras y Reportes Gerenciales completos (entregables de la siguiente iteración).

---

## 3. Referencias

- `docs/URRUSTARAZU-CIRO-AP2.md` — documento principal AP2, secciones 2 (Análisis), 4 (Implementación) y 5 (Pruebas).
- `database/consultas-tp2.sql` — consultas SQL representativas (verificación de consistencia).
- `pruebas/casos-de-prueba.md` — detalle de los 12 casos de prueba (CP01–CP12) y matriz de trazabilidad.
- `pruebas/datos-de-prueba.sql` — script de datos reproducibles para ejecutar los casos.
- Kendall, K. & Kendall, J. (2011). *Análisis y diseño de sistemas* (8a ed.), Cap. 16 — Calidad del software y pruebas.
- Estándar IEEE 829-2008 (Software and System Test Documentation) — referencia conceptual para la estructura del plan.

---

## 4. Estrategia de pruebas

Se aplican **tres niveles** complementarios, alineados con la disciplina de pruebas del Proceso Unificado:

| Nivel | Qué se prueba | Cómo | Responsable |
|---|---|---|---|
| **Unitarias** | Servicios y DAO en aislamiento (validaciones, cálculos, formato). | Métodos `Servicio*` invocados directamente con datos controlados; assertions sobre el retorno. | Desarrollador |
| **Integración** | Camino completo Servicio → DAO → BD MySQL. | Clase `com.sigiv.util.SmokeTest` que ejecuta los escenarios extremo a extremo sin UI. | Desarrollador |
| **Sistema / Aceptación** | UI Swing + lógica + BD, simulando el uso real del vendedor. | Ejecución manual de cada CP siguiendo los pasos del documento `casos-de-prueba.md`. | Usuario final / Tutor |

### 4.1. Tipos de prueba aplicados

- **Funcionales:** verifican que cada CU produce el resultado esperado (CP01–CP10, CP12).
- **No funcionales — confiabilidad:** verificación de rollback ante falla (CP11).
- **No funcionales — seguridad:** validación de contraseñas hasheadas con BCrypt y control de acceso por rol (CP01–CP03, CP10).
- **No funcionales — integridad de datos:** consistencia de stock, saldos y movimientos tras operaciones transaccionales (CP06–CP09).

### 4.2. Técnicas de diseño de casos

- **Partición de equivalencia:** clases válidas/inválidas para credenciales, stock, crédito.
- **Análisis de valores límite:** stock = 0, crédito disponible = monto exacto de la venta.
- **Casos de error:** cada CU se prueba con su camino feliz y al menos un flujo alternativo o excepción.
- **Trazabilidad inversa:** cada CP se enlaza explícitamente a uno o más RF/RNF (ver matriz en `casos-de-prueba.md`).

---

## 5. Criterios de entrada y salida

### 5.1. Criterios de entrada (para iniciar la ejecución)

1. Base de datos `sigiv_ferreteria` creada y poblada con el seed inicial (`database/schema.sql` + `database/datos-iniciales.sql` + `pruebas/datos-de-prueba.sql`).
2. Aplicación compilada sin errores (`mvn clean package`).
3. Usuario `sigiv_app@localhost` creado con permisos CRUD.
4. Credenciales de prueba (`admin`/`admin123`, `vendedor1`/`admin123`) activas.

### 5.2. Criterios de salida (para considerar la fase aprobada)

1. **100% de los CP de prioridad Alta** ejecutados y aprobados (CP01, CP06, CP07, CP08, CP11).
2. **≥ 90% del total** de CP aprobados (mínimo 11 de 12).
3. Ningún defecto **crítico o bloqueante** abierto.
4. Los defectos **menores** detectados están documentados con su severidad y planificada su corrección o aceptación formal.
5. Evidencia documental archivada (resultados del SmokeTest + observaciones de la ejecución manual).

---

## 6. Entorno de pruebas

| Componente | Configuración |
|---|---|
| **Sistema operativo cliente** | Windows 11 / macOS 14+ / Ubuntu 22.04 |
| **JDK** | OpenJDK 21 (compatible con Java 17+ del proyecto) |
| **Build** | Apache Maven 3.9+ |
| **Base de datos** | MySQL 8.0 (local, `localhost:3306`) |
| **Driver** | MySQL Connector/J 8.3 |
| **Hashing** | jBCrypt 0.4 |
| **Base de pruebas** | `sigiv_ferreteria` (esquema completo + seed inicial + seed de pruebas) |

**Configuración recomendada:** ejecutar las pruebas sobre una instancia de MySQL aislada del entorno de desarrollo del usuario, para no contaminar datos reales.

---

## 7. Recursos y roles

| Rol | Responsabilidades |
|---|---|
| **Desarrollador (autor)** | Implementación del SmokeTest, ejecución de pruebas unitarias e integración, documentación de resultados. |
| **Tutor académico** | Validación del plan, revisión de cobertura, ejecución de pruebas de aceptación con criterio externo. |
| **Usuario final simulado** | En la fase de Transición — fuera del alcance de AP2, mencionado a futuro. |

---

## 8. Cronograma de pruebas

| Etapa | Duración estimada | Resultado |
|---|---|---|
| Diseño de casos | 4 h | 12 CP detallados en `casos-de-prueba.md`. |
| Preparación del entorno | 1 h | Esquema + seeds cargados. |
| Ejecución unitaria + integración (SmokeTest) | 30 min | Log de ejecución con resultado por CP automatizado. |
| Ejecución manual de aceptación | 2 h | Observaciones registradas. |
| Análisis y cierre | 1 h | Actualización del reporte y de la tabla del AP2.md sección 5.2. |

---

## 9. Riesgos y mitigaciones

| Riesgo | Impacto | Probabilidad | Mitigación |
|---|---|---|---|
| Versión de MySQL incompatible con DDL (índices `IF NOT EXISTS`, etc.). | Medio | Baja | Documentar versión mínima (8.0); fallback con DDL alternativo. |
| Datos de prueba contaminados de ejecuciones previas. | Medio | Media | Script de reset (`TRUNCATE` + reload del seed) antes de cada ejecución completa. |
| Test de "caída de BD" (CP11) requiere detener manualmente el servicio mysqld. | Bajo | Alta | Documentado como prueba manual en el caso de prueba; alternativa: cerrar el socket JDBC explícitamente para simular. |
| Concurrencia real (dos vendedores al mismo producto) no replicable en entorno mono-instancia. | Bajo | Alta | Simular con dos conexiones JDBC en paralelo desde el SmokeTest (mejora futura). |

---

## 10. Entregables

Al finalizar la ejecución del plan se producen los siguientes artefactos:

- **`pruebas/plan-de-pruebas.md`** — este documento.
- **`pruebas/casos-de-prueba.md`** — 12 CP detallados + matriz de trazabilidad.
- **`pruebas/datos-de-prueba.sql`** — script SQL para reproducir los escenarios.
- **Sección 5 del AP2.md** — resumen ejecutivo del plan con tabla de resultados y cobertura.
- **Clase `SmokeTest.java`** (en `src/main/java/com/sigiv/util/`) — ejecución automatizada parcial.

---

## 11. Aprobación

El presente plan se considera **aprobado** cuando es revisado por el tutor académico de la materia y se cumplen los criterios de salida detallados en la sección 5.2.
