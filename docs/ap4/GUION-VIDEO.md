# Guión del video de presentación — AP4 (SIGIV-SM)

**Duración objetivo:** ~1:50 min (Fernando indicó en AP3 que el video debe durar de 1 a 2 minutos).
**Formato sugerido:** captura de pantalla con tu voz en off. Mostrar IDE + consola corriendo.

> Consejo: grabá la corrida real del programa antes y narrá encima. Hablá tranquilo; con 2 minutos alcanza si no te detenés en detalles.

---

## Escaleta (con tiempos y qué mostrar)

### 0:00 – 0:15 · Presentación
**En pantalla:** portada del informe / nombre del sistema.
**Decís:**
> "Hola, soy Ciro Urrustarazu. Les presento la versión final de SIGIV-SM, el Sistema de Gestión de Inventario y Ventas que desarrollé para la Ferretería San Martín, un comercio que hoy trabaja con talonario y planillas. El sistema está hecho en Java con persistencia en MySQL."

### 0:15 – 0:35 · Qué resuelve y cómo está organizado
**En pantalla:** estructura de paquetes en el IDE (modelo, dao, servicio, vista/consola).
**Decís:**
> "El sistema está organizado en tres capas: presentación, lógica de negocio y persistencia. El patrón de diseño protagonista es DAO, que aísla el acceso a la base de datos del resto del código. Lo formalicé con una interfaz genérica, DAO de T, que implementan los objetos de acceso a datos."

### 0:35 – 0:55 · Persistencia en MySQL
**En pantalla:** abrir `ProductoDAO` o `VentaDAO`; mostrar un `PreparedStatement` y la transacción.
**Decís:**
> "Toda la persistencia usa JDBC con consultas parametrizadas, para evitar inyección SQL. Operaciones como registrar una venta son transaccionales: si algo falla, se hace rollback y no queda nada a medias. Los errores de base de datos se manejan con SQLException, separados de los errores de negocio."

### 0:55 – 1:25 · Demo en vivo (lo más importante)
**En pantalla:** ejecutar `java -jar target/sigiv-ferreteria.jar --consola`, login, y usar el menú.
**Decís mientras mostrás:**
> "Inicio sesión… listo el menú principal. Voy a mostrar dos cosas. Primero, una venta: cargo un producto, elijo forma de pago, y el sistema descuenta el stock automáticamente. Y segundo, el reporte de ventas por forma de pago."
>
> *(elegís la opción 8)*
>
> "Acá uso de forma complementaria las dos estructuras: la lista de ventas viene de la base como un ArrayList dinámico, y los totales se acumulan en un arreglo de tamaño fijo, uno por cada forma de pago. El reporte se muestra en pantalla y, además, se exporta a un archivo de texto."

### 1:25 – 1:45 · Cierre técnico
**En pantalla:** mostrar el archivo `.txt` generado en la carpeta `reportes/` y, opcional, `BUILD SUCCESS` de Maven.
**Decís:**
> "Ahí está el reporte guardado en el archivo. El sistema usa clases abstractas e interfaces, manejo de excepciones, arreglos y ArrayList, todo sobre MySQL. El proyecto compila y corre correctamente."

### 1:45 – 1:50 · Despedida
**Decís:**
> "El código completo está en el repositorio de GitHub que dejo en el informe. ¡Gracias!"

---

## Checklist antes de grabar
- [ ] MySQL levantado y con datos de prueba cargados (`datos-iniciales.sql`).
- [ ] `mvn clean package` ya ejecutado (jar listo en `target/`).
- [ ] Tener cargada al menos 1 venta de cada forma de pago para que el reporte muestre números.
- [ ] Cerrar notificaciones y limpiar el escritorio antes de grabar.
- [ ] Audio probado; hablar pausado.
- [ ] Subir el video a YouTube (no listado) o Drive y dejar el enlace en el informe / entrega.
