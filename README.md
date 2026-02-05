# API de Franquicias - Spring WebFlux y MongoDB

API REST reactiva para gestionar franquicias, sucursales y productos. Desarrollada como prueba técnica con **Spring Boot WebFlux**, **MongoDB** (persistencia reactiva) y **arquitectura hexagonal**. operadores reactivos, logging, pruebas unitarias, README y API RESTful.

---

## Dominio

- **Franquicia**: nombre + listado de sucursales.
- **Sucursal**: nombre + listado de productos ofertados.
- **Producto**: nombre + cantidad en stock + **enabled** (por defecto `true`; `false` = borrado lógico).

---

## Requisitos

- **Java 21**
- **MongoDB** (por ejemplo en local con Docker)
- **Gradle** (wrapper incluido: `./gradlew` o `gradlew.bat`)

---

## Cómo ejecutar en local

### 1. Levantar MongoDB con Docker

```bash
docker pull mongodb/mongodb-community-server:latest
docker run --name franquicias -d -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=admin -e MONGO_INITDB_ROOT_PASSWORD=password123 mongodb/mongodb-community-server:latest
```

La aplicación está configurada para conectarse a:

- **Host:** `localhost:27017`
- **Base de datos:** `franquicias`
- **Usuario:** `admin`
- **Contraseña:** `password123`

*(URI en `application.properties`: `spring.mongodb.uri=mongodb://admin:password123@localhost:27017/franquicias?authSource=admin`)*

### 2. Ejecutar la aplicación

```bash
./gradlew bootRun
```

En Windows:

```bash
gradlew.bat bootRun
```

La API quedará disponible en **http://localhost:8080**.

### 3. Documentación interactiva (OpenAPI / Swagger)

- **Swagger UI:** http://localhost:8080/swagger-ui.html  
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs  

---

## Contratos de la API (endpoints)

Base path: **`/api/v1/franchises`**

| Método | Path | Descripción | Cuerpo ejemplo | Respuesta típica |
|--------|------|-------------|-----------------|-------------------|
| **POST** | `/api/v1/franchises` | Agregar franquicia | `{"name": "Franquicia Norte"}` | `201` + `{ "id", "name", "branches": [] }` |
| **POST** | `/api/v1/franchises/{franchiseId}/branches` | Agregar sucursal a una franquicia | `{"name": "Sucursal Centro"}` | `201` + `{ "id", "name", "products": [] }` |
| **POST** | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products` | Agregar producto a una sucursal | `{"name": "Producto A", "stockQuantity": 10}` | `201` + `{ "id", "name", "stockQuantity", "enabled" }` |
| **DELETE** | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}` | Eliminar producto (borrado físico) | — | `204` |
| **PATCH** | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/disable` | Deshabilitar producto (borrado lógico) | — | `200` + `{ "id", "name", "stockQuantity", "enabled": false }` |
| **PATCH** | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock` | Modificar stock de un producto | `{"stockQuantity": 20}` | `200` + `{ "id", "name", "stockQuantity", "enabled" }` |
| **GET** | `/api/v1/franchises/{franchiseId}/branches/products/max-stock` | Producto con más stock por sucursal (solo productos habilitados) | — | `200` + `[{ "branchId", "branchName", "product": { "id", "name", "stockQuantity", "enabled" } }]` |
| **GET** | `/api/v1/franchises` | Listar franquicias | — | `200` + array de franquicias |
| **GET** | `/api/v1/franchises/{franchiseId}` | Obtener franquicia por ID | — | `200` + franquicia con sucursales y productos |
| **PATCH** | `/api/v1/franchises/{franchiseId}/name` | Actualizar nombre de franquicia *(punto extra)* | `{"name": "Nuevo Nombre"}` | `200` + franquicia |
| **PATCH** | `/api/v1/franchises/{franchiseId}/branches/{branchId}/name` | Actualizar nombre de sucursal *(punto extra)* | `{"name": "Nueva Sucursal"}` | `200` + sucursal |
| **PATCH** | `/api/v1/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name` | Actualizar nombre de producto *(punto extra)* | `{"name": "Nuevo Producto"}` | `200` + producto |

**Códigos HTTP:** `201` creación, `200` OK, `204` sin contenido, `400` validación/datos inválidos, `404` recurso no encontrado.

### Borrado lógico vs. borrado físico.

- **DELETE** `.../products/{productId}`: elimina el producto del documento (borrado físico). Los datos se pierden.
- **PATCH** `.../products/{productId}/disable`: marca el producto como deshabilitado (`enabled=false`, borrado lógico).

**En entornos productivos se recomienda el borrado lógico** porque:
- Preserva el historial y permite auditoría.
- Permite recuperar o “reactivar” el producto sin perder datos.
- El endpoint “producto con más stock por sucursal” solo considera productos con `enabled=true`.
- Actualizar stock o nombre de un producto deshabilitado devuelve `404` (no se modifican datos “eliminados” lógicamente).

---

## Pruebas y cobertura

- **Ejecutar tests:**  
  `./gradlew test` (en Windows: `gradlew.bat test`)

- **Reporte de cobertura (JaCoCo):**  
  Tras `./gradlew test`, el reporte HTML se genera en:  
  `build/reports/jacoco/test/html/index.html`  

  Objetivo de la prueba: cobertura **> 60%**, deseable **≥ 80%**.  
  Con las pruebas unitarias del servicio (Mockito + StepVerifier) y del controlador (WebTestClient), la cobertura de instrucciones actual es **≥ 74%** (servicio ~94%, controlador ~80%).

---

## Consideraciones de diseño

- **Arquitectura hexagonal:** Dominio sin dependencias de frameworks; puertos en dominio (`FranchiseRepository`); adaptadores en infraestructura (MongoDB reactivo, controladores REST).
- **MongoDB:** Documentos embebidos (franquicia → sucursales → productos) en una sola colección para consultas coherentes y menos joins.
- **Logging:** SLF4J con Logback (`logback-spring.xml` y nivel/configuración en propiedades).
- **Java 21 records:** Los DTOs de request/response son records para inmutabilidad y menor boilerplate.
- **Borrado lógico:** El producto tiene campo `enabled` (por defecto `true`); el endpoint `PATCH .../disable` realiza borrado lógico recomendado en producción.

---

## Flujo de trabajo con Git

- Rama principal: `main`.
- Repositorio público en GitHub para entrega de la prueba.