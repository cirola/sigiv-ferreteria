#!/usr/bin/env bash
#
# reset-demo.sh — Deja la base SIGIV en su estado inicial limpio.
#
# Borra todas las ventas, movimientos de cuenta corriente y demás datos de
# prueba acumulados, y vuelve a cargar el seed (productos con su stock original,
# clientes con saldo en cero, usuarios, etc.). Útil para grabar el video con un
# reporte de ventas "desde cero".
#
# Uso:
#   ./database/reset-demo.sh          (usuario root sin contraseña)
#   DB_USER=root DB_PASS=tu_pass ./database/reset-demo.sh
#
set -euo pipefail

# Carpeta donde vive este script (para poder correrlo desde cualquier lado)
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

DB_USER="${DB_USER:-root}"

# Arma los argumentos de mysql; agrega -p sólo si hay contraseña definida
MYSQL_ARGS=(-u "$DB_USER")
if [[ -n "${DB_PASS:-}" ]]; then
  MYSQL_ARGS+=("-p${DB_PASS}")
fi

echo "==> Recreando esquema (borra y crea la base sigiv_ferreteria)..."
mysql "${MYSQL_ARGS[@]}" < "$DIR/schema.sql"

echo "==> Cargando datos iniciales (seed)..."
mysql "${MYSQL_ARGS[@]}" < "$DIR/datos-iniciales.sql"

echo "==> Base reseteada. 0 ventas cargadas, stock y saldos en su estado inicial."
echo "    Ya podés grabar: cargá tus ventas en vivo y el reporte (opción 8) mostrará sólo esas."
