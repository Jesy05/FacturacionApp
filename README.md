# Sistema de Facturación — JavaFX + PostgreSQL + JDBC

Proyecto de la clase **Programación de Aplicaciones de Escritorio** — Universidad Americana (UAM).

## Integrantes

- Jose Cristo Carvallo Herrer
- Jesy Nicole González Jarquín

## La asignación

La práctica consiste en desarrollar una aplicación de escritorio con **JavaFX** conectada a una base de datos **PostgreSQL** mediante **JDBC**. Los objetivos son:

- Crear un proyecto JavaFX utilizando Maven.
- Conectar Java con PostgreSQL mediante JDBC.
- Utilizar `Connection` y `PreparedStatement`.
- Crear clases modelo e implementar el patrón DAO.
- Relacionar las clases `Categoria` y `Producto`.
- Mostrar la información utilizando JavaFX.

## ¿Qué hace la aplicación?

Es un sistema sencillo para administrar el catálogo de una tienda:

- **Menú principal** con acceso a Categorías y Productos.
- **Categorías:** crear, listar, editar y eliminar categorías. Una categoría que ya tiene productos no se puede eliminar (lo impide la llave foránea); en su lugar se puede desactivar.
- **Productos:** crear, listar, editar y eliminar productos con código, nombre, categoría, precio de venta, existencia, ruta de imagen y estado activo. La categoría se elige en un `ComboBox` que muestra solo las categorías activas.
- Los datos se muestran en un `TableView`; al seleccionar una fila, el formulario se llena para editarla.

### Arquitectura

```
JavaFX (FXML)  →  Controller  →  DAO  →  PostgreSQL
```

- **Vista (FXML):** define la interfaz.
- **Controller:** maneja los eventos de la interfaz y valida los datos.
- **DAO:** ejecuta las consultas SQL con `PreparedStatement`.
- **PostgreSQL:** guarda la información.

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

Para comprobar la conexión se puede ejecutar la clase `TestConexion`, que está en el mismo paquete `util`.

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