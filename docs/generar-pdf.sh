#!/bin/bash
# Genera URRUSTARAZU-CIRO-AP2.pdf a partir de portada.md + URRUSTARAZU-CIRO-AP2.md
# Requiere: pandoc + Google Chrome.
# Ejecutar desde docs/

set -e
cd "$(dirname "$0")"

echo "==> Generando HTML con pandoc..."
pandoc portada.md URRUSTARAZU-CIRO-AP2.md \
    -f markdown+fenced_divs \
    -t html5 \
    --standalone \
    --css=estilo-pdf.css \
    --metadata title="URRUSTARAZU-CIRO-AP2" \
    -o URRUSTARAZU-CIRO-AP2.html

echo "==> Imprimiendo PDF con Chrome headless..."
"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" \
    --headless \
    --disable-gpu \
    --no-pdf-header-footer \
    --print-to-pdf="URRUSTARAZU-CIRO-AP2.pdf" \
    "file://$(pwd)/URRUSTARAZU-CIRO-AP2.html" 2>&1 | grep -i "bytes written" || true

echo "==> Listo: $(pwd)/URRUSTARAZU-CIRO-AP2.pdf"
ls -lh URRUSTARAZU-CIRO-AP2.pdf
