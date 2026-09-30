# Sistema de Facturación — JavaFX + PostgreSQL + JDBC

Proyecto de la clase **Programación de Aplicaciones de Escritorio** — Universidad Americana (UAM).

## Integrantes

- Jose Cristo Carvallo Herrer
- Jesy Nicole González Jarquín

## Las asignaciones

### 1. JavaFX + PostgreSQL + JDBC

Desarrollar una aplicación de escritorio con **JavaFX** conectada a una base de datos **PostgreSQL** mediante **JDBC**:

- Crear un proyecto JavaFX utilizando Maven.
- Conectar Java con PostgreSQL mediante JDBC.
- Utilizar `Connection` y `PreparedStatement`.
- Crear clases modelo e implementar el patrón DAO.
- Relacionar las clases `Categoria` y `Producto`.

### 2. Práctica CRUD — Gestión de Productos

Construir un módulo de gestión de productos con Programación Orientada a Objetos:

- Operaciones CRUD (crear, consultar, actualizar y eliminar).
- Visualización de la información en un `TableView`.
- Búsqueda de productos por código, nombre o categoría, sin distinguir mayúsculas de minúsculas.
- Filtrado con `FilteredList` por estado (todos, activos, inactivos) y por categoría.
- Validación de datos y control de códigos duplicados.

## ¿Qué hace la aplicación?

Es un sistema sencillo para administrar el catálogo de una tienda:

- **Menú principal** con acceso a Categorías y Productos.
- **Categorías:** crear, listar, buscar, editar y eliminar categorías. Una categoría que ya tiene productos no se puede eliminar (lo impide la llave foránea); en su lugar se puede desactivar.
- **Productos:**
  - Crear, consultar, actualizar y eliminar productos (con confirmación antes de eliminar).
  - Al seleccionar una fila de la tabla, sus datos se cargan en el formulario.
  - Barra de búsqueda con botones **Buscar** y **Limpiar búsqueda**.
  - Filtros por estado y por categoría que funcionan junto con la búsqueda.
  - Contador de resultados ("Mostrando X de Y productos").

### Validaciones de productos

- El código y el nombre son obligatorios.
- Debe seleccionarse una categoría.
- El precio debe ser numérico y mayor que cero.
- La existencia debe ser un número entero y no puede ser negativa.
- No se permiten códigos duplicados.
- Si un dato es incorrecto se muestra un mensaje y la aplicación sigue funcionando.

### Arquitectura

```
JavaFX (FXML)  →  Controller  →  DAO  →  PostgreSQL
```

- **Vista (FXML + CSS):** define la interfaz y su estilo (tema celeste).
- **Controller:** maneja los eventos de la interfaz, valida los datos y aplica los filtros.
- **DAO:** ejecuta las consultas SQL con `PreparedStatement`.
- **PostgreSQL:** guarda la información.

Para el filtrado se usa la estructura:

```
ObservableList → FilteredList → TableView
```

La `ObservableList` conserva todos los productos y la `FilteredList` solo decide cuáles se muestran.

## Requisitos

- JDK 21
- PostgreSQL
- Maven (se incluye el wrapper `mvnw` / `mvnw.cmd`)

## Configuración de la base de datos

1. Crear la base de datos:

```sql
CREATE DATABASE tienda_javafx;
```

2. Conectado a `tienda_javafx`, crear las tablas:

```sql
CREATE TABLE categoria (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE producto (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    categoria_id INTEGER NOT NULL,
    precio_venta NUMERIC(12,2) NOT NULL,
    existencia INTEGER NOT NULL DEFAULT 0,
    ruta_imagen VARCHAR(500),
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (categoria_id)
        REFERENCES categoria(id)
);
```

3. Ajustar el usuario y la contraseña de PostgreSQL en
   `src/main/java/ni/edu/uam/facturacion/util/DatabaseConnection.java`.

Para comprobar la conexión se puede ejecutar la clase `TestConexion` (en el mismo paquete `util`).

## Cómo ejecutar

```bash
./mvnw clean javafx:run
```

También se puede ejecutar la clase `FacturacionApplication` desde IntelliJ IDEA.

## Estructura del proyecto

```
FacturacionApp/
├── pom.xml
├── README.md
└── src/main/
    ├── java/
    │   ├── module-info.java
    │   └── ni/edu/uam/facturacion/
    │       ├── application/    → clase Application (punto de entrada)
    │       ├── controller/     → controladores de las vistas (Menú, Categoría, Producto)
    │       ├── dao/            → acceso a datos (CategoriaDAO, ProductoDAO)
    │       ├── model/          → entidades (Categoria, Producto, ...)
    │       └── util/           → conexión JDBC, SceneManager y prueba de conexión
    └── resources/ni/edu/uam/facturacion/
        ├── css/                → hoja de estilos (tema celeste)
        ├── fxml/               → vistas (menú, categorías, productos)
        ├── images/             → logo e imágenes de productos
        └── icons/              → íconos de los botones
```

## Tecnologías

- Java 21
- JavaFX 21 (controls, fxml)
- PostgreSQL + driver JDBC 42.7.8
- Lombok
- Maven
