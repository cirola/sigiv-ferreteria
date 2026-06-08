#!/bin/bash
# Genera URRUSTARAZU-CIRO-AP3.pdf a partir de portada.md + URRUSTARAZU-CIRO-AP3.md
# Requiere: pandoc + Google Chrome.
# Ejecutar desde docs/

set -e
cd "$(dirname "$0")"

echo "==> Generando HTML con pandoc..."
pandoc portada.md URRUSTARAZU-CIRO-AP3.md \
    -f markdown+fenced_divs \
    -t html5 \
    --standalone \
    --css=estilo-pdf.css \
    --metadata title="URRUSTARAZU-CIRO-AP3" \
    -o URRUSTARAZU-CIRO-AP3.html

echo "==> Imprimiendo PDF con Chrome headless..."
"/Applications/Google Chrome.app/Contents/MacOS/Google Chrome" \
    --headless \
    --disable-gpu \
    --no-pdf-header-footer \
    --print-to-pdf="URRUSTARAZU-CIRO-AP3.pdf" \
    "file://$(pwd)/URRUSTARAZU-CIRO-AP3.html" 2>&1 | grep -i "bytes written" || true

echo "==> Listo: $(pwd)/URRUSTARAZU-CIRO-AP3.pdf"
ls -lh URRUSTARAZU-CIRO-AP3.pdf
