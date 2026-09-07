# Sistema de Facturación (JavaFX)

Proyecto de la clase de Programación Estructurada / JavaFX — UAM.

## Requisitos

- JDK 21
- Maven (se incluye el wrapper `mvnw` / `mvnw.cmd`)

## Cómo ejecutar

```bash
./mvnw clean javafx:run
```

## Estructura del proyecto

```
sistema-facturacion-javafx/
├── pom.xml
├── README.md
├── .gitignore
└── src/main/
    ├── java/
    │   ├── module-info.java
    │   └── ni/edu/uam/facturacion/
    │       ├── application/    → clase Application (punto de entrada)
    │       ├── controller/     → controladores de las vistas FXML
    │       ├── model/          → entidades del dominio
    │       └── util/           → utilidades (SceneManager, etc.)
    └── resources/ni/edu/uam/facturacion/
        ├── fxml/               → vistas
        ├── images/             → imágenes (logo, productos)
        └── icons/              → íconos de la interfaz
```
