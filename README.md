# SIGIV — Sistema de Gestión de Inventario y Ventas

Prototipo operacional para **Ferretería San Martín** desarrollado como trabajo práctico AP1 de la materia _Seminario de Práctica Informática_.

**Autor:** Ciro Urrustarazu
**Stack:** Java 17+ · MySQL 8/9 · Maven · Swing · BCrypt

---

## Alcance del prototipo

El prototipo implementa los módulos **Seguridad**, **Productos/Inventario** y **Ventas** (con cuentas corrientes básicas), siguiendo la arquitectura en 3 capas (presentación / lógica / persistencia) y los patrones **MVC + DAO**.

Las operaciones que afectan múltiples tablas (ventas, pagos) se ejecutan dentro de transacciones con rollback automático ante error.

---

## Requisitos previos

- **Java 17** o superior (probado con JDK 21 y JDK 25)
- **Maven 3.9+**
- **MySQL 8.x o 9.x**

En macOS con Homebrew:

```bash
brew install openjdk@21 maven mysql
brew services start mysql
```

---

## Puesta en marcha

### 1) Crear la base de datos

```bash
mysql -u root < database/schema.sql
mysql -u root < database/datos-iniciales.sql
```

Esto crea:

- La base `sigiv_ferreteria` con todas las tablas.
- El usuario de aplicación `sigiv_app` (contraseña `sigiv_app_2026`) con permisos CRUD.
- Datos de prueba: 2 usuarios, 7 rubros, 3 proveedores, 14 productos, 4 clientes.

### 2) Compilar

```bash
mvn clean package
```

### 3) Ejecutar

```bash
mvn exec:java
```

o bien, el jar empaquetado:

```bash
java -jar target/sigiv-ferreteria.jar
```

---

## Credenciales de prueba

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| `admin` | `admin123` | ADMIN |
| `vendedor1` | `admin123` | VENDEDOR |

---

## Estructura del proyecto

```
sigiv-ferreteria/
├── docs/
│   ├── URRUSTARAZU-CIRO-AP1.md      Informe del trabajo práctico
│   └── diagramas/                    Diagramas PlantUML + PNG renderizados
├── database/
│   ├── schema.sql                    Esquema MySQL
│   └── datos-iniciales.sql           Datos de prueba (seed)
├── src/main/java/com/sigiv/
│   ├── App.java                      Entry point
│   ├── util/ConexionBD.java          Acceso a BD (JDBC)
│   ├── modelo/                       Entidades de dominio
│   ├── dao/                          Patrón DAO (persistencia)
│   ├── servicio/                     Lógica de negocio
│   └── vista/                        UI Swing (MVC)
├── src/main/resources/config.properties
├── pom.xml
└── README.md
```

---

## Smoke test

Verificación end-to-end sin interfaz gráfica:

```bash
mvn clean compile
mvn dependency:build-classpath -Dmdep.outputFile=target/cp.txt -q
java -cp "target/classes:src/main/resources:$(cat target/cp.txt)" com.sigiv.util.SmokeTest
```

El test valida: login correcto e incorrecto, listado de productos, venta en efectivo con descuento de stock, venta a cuenta corriente con actualización de saldo, y rechazo ante stock insuficiente.

---

## Diagramas

Todos los diagramas UML/BPMN están como código fuente PlantUML en `docs/diagramas/` y rendereados a PNG. Para regenerarlos:

```bash
brew install plantuml
plantuml -tpng docs/diagramas/*.puml
```

- `casos-uso-sigiv.png` — casos de uso del sistema
- `procesos-venta-actual.png` — proceso AS-IS (venta manual actual)
- `proceso-venta-propuesto.png` — proceso TO-BE (con SIGIV)
- `arquitectura-sigiv.png` — arquitectura en 3 capas

---

## Metodología

Desarrollo guiado por el **Proceso Unificado de Desarrollo (PUD)**:

- **Inception** ✅ — análisis de negocio, requerimientos, casos de uso (esta entrega).
- **Elaboration** — diseño detallado, modelo de clases, modelo de datos.
- **Construction** — desarrollo iterativo de módulos restantes (compras, reportes, cuentas corrientes avanzadas).
- **Transition** — despliegue y capacitación al usuario final.
