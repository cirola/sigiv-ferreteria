∆-- =====================================================================
-- SIGIV - Ferretería San Martín
-- Esquema de base de datos MySQL 8.x
-- Autor: Ciro Urrustarazu
-- =====================================================================

DROP DATABASE IF EXISTS sigiv_ferreteria;
CREATE DATABASE sigiv_ferreteria
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sigiv_ferreteria;

-- ---------------------------------------------------------------------
-- Roles y usuarios (Módulo Seguridad)
-- ---------------------------------------------------------------------
CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(120)
);

CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(30) NOT NULL UNIQUE,
    nombre_completo VARCHAR(100) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    rol_id INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_alta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES roles(id)
);

-- ---------------------------------------------------------------------
-- Rubros y productos (Módulo Inventario)
-- ---------------------------------------------------------------------
CREATE TABLE rubros (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(60) NOT NULL UNIQUE,
    margen_default DECIMAL(5,2) NOT NULL DEFAULT 30.00
);

CREATE TABLE proveedores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    razon_social VARCHAR(120) NOT NULL,
    cuit VARCHAR(13) UNIQUE,
    telefono VARCHAR(30),
    email VARCHAR(80),
    direccion VARCHAR(150),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(150) NOT NULL,
    rubro_id INT NOT NULL,
    proveedor_id INT,
    precio_costo DECIMAL(12,2) NOT NULL CHECK (precio_costo >= 0),
    precio_venta DECIMAL(12,2) NOT NULL CHECK (precio_venta >= 0),
    stock_actual INT NOT NULL DEFAULT 0 CHECK (stock_actual >= 0),
    stock_minimo INT NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_alta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prod_rubro FOREIGN KEY (rubro_id) REFERENCES rubros(id),
    CONSTRAINT fk_prod_prov FOREIGN KEY (proveedor_id) REFERENCES proveedores(id)
);

CREATE INDEX idx_prod_descripcion ON productos(descripcion);

-- ---------------------------------------------------------------------
-- Clientes y cuentas corrientes
-- ---------------------------------------------------------------------
CREATE TABLE clientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(120) NOT NULL,
    documento VARCHAR(15) UNIQUE,
    telefono VARCHAR(30),
    email VARCHAR(80),
    direccion VARCHAR(150),
    tiene_cta_cte BOOLEAN NOT NULL DEFAULT FALSE,
    limite_credito DECIMAL(12,2) NOT NULL DEFAULT 0,
    saldo_cta_cte DECIMAL(12,2) NOT NULL DEFAULT 0,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_alta DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE movimientos_cta_cte (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo ENUM('DEBE','HABER') NOT NULL,
    monto DECIMAL(12,2) NOT NULL CHECK (monto >= 0),
    descripcion VARCHAR(200),
    venta_id INT,
    CONSTRAINT fk_mov_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

-- ---------------------------------------------------------------------
-- Ventas
-- ---------------------------------------------------------------------
CREATE TABLE ventas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id INT NOT NULL,
    cliente_id INT,
    forma_pago ENUM('EFECTIVO','TRANSFERENCIA','CTA_CTE') NOT NULL,
    total DECIMAL(12,2) NOT NULL CHECK (total >= 0),
    estado ENUM('CONFIRMADA','ANULADA') NOT NULL DEFAULT 'CONFIRMADA',
    CONSTRAINT fk_venta_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id),
    CONSTRAINT fk_venta_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
);

CREATE TABLE detalle_ventas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    venta_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(12,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_det_venta FOREIGN KEY (venta_id) REFERENCES ventas(id) ON DELETE CASCADE,
    CONSTRAINT fk_det_producto FOREIGN KEY (producto_id) REFERENCES productos(id)
);

-- ---------------------------------------------------------------------
-- Compras a proveedores
-- ---------------------------------------------------------------------
CREATE TABLE compras (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    proveedor_id INT NOT NULL,
    usuario_id INT NOT NULL,
    remito VARCHAR(30),
    total DECIMAL(12,2) NOT NULL CHECK (total >= 0),
    CONSTRAINT fk_compra_prov FOREIGN KEY (proveedor_id) REFERENCES proveedores(id),
    CONSTRAINT fk_compra_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE detalle_compras (
    id INT AUTO_INCREMENT PRIMARY KEY,
    compra_id INT NOT NULL,
    producto_id INT NOT NULL,
    cantidad INT NOT NULL CHECK (cantidad > 0),
    precio_costo DECIMAL(12,2) NOT NULL CHECK (precio_costo >= 0),
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_detc_compra FOREIGN KEY (compra_id) REFERENCES compras(id) ON DELETE CASCADE,
    CONSTRAINT fk_detc_producto FOREIGN KEY (producto_id) REFERENCES productos(id)
);

-- ---------------------------------------------------------------------
-- Auditoría / bitácora (RF10)
-- ---------------------------------------------------------------------
CREATE TABLE bitacora (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    usuario_id INT,
    accion VARCHAR(60) NOT NULL,
    detalle VARCHAR(300),
    CONSTRAINT fk_bit_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- ---------------------------------------------------------------------
-- Usuario de aplicación (NO usar root desde Java)
-- ---------------------------------------------------------------------
CREATE USER IF NOT EXISTS 'sigiv_app'@'localhost' IDENTIFIED BY 'sigiv_app_2026';
GRANT SELECT, INSERT, UPDATE, DELETE ON sigiv_ferreteria.* TO 'sigiv_app'@'localhost';
FLUSH PRIVILEGES;
