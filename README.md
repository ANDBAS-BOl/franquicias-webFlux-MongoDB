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

## Uso del Dockerfile (Etapa 5 – Docker)

La aplicación se puede construir y ejecutar con Docker mediante un **Dockerfile multi-stage**: una etapa compila con Gradle y Java 21, y la etapa final usa solo JRE 21 para ejecutar el JAR.

### Requisitos

- **Docker** instalado y en ejecución.
- **MongoDB** accesible (en tu máquina, en otro contenedor o en un servicio en la nube). La aplicación necesita la URI de MongoDB en tiempo de ejecución.

### Construir la imagen

Desde la raíz del proyecto (`API-franchises-webFlux-MongoDB/`):

```bash
docker build -t api-franquicias:latest .
```

- `-t api-franquicias:latest` asigna nombre y etiqueta a la imagen.
- El punto (`.`) indica el contexto de build (directorio actual).

### Ejecutar el contenedor

**Si MongoDB está en tu máquina (localhost):**

En Linux/Mac, usar `host.docker.internal` para que el contenedor acceda al host:

```bash
docker run -d -p 8080:8080 \
  -e SPRING_MONGODB_URI=mongodb://admin:password123@host.docker.internal:27017/franquicias?authSource=admin \
  --name api-franquicias \
  api-franquicias:latest
```

En Windows con Docker Desktop también se puede usar `host.docker.internal`.

**Si MongoDB está en otro contenedor o en una red Docker:**

Pasa la URI correcta (hostname del servicio o IP):

```bash
docker run -d -p 8080:8080 \
  -e SPRING_MONGODB_URI=mongodb://admin:password123@<host-mongo>:27017/franquicias?authSource=admin \
  --name api-franquicias \
  api-franquicias:latest
```

Sustituye `<host-mongo>` por el nombre del contenedor/servicio de MongoDB o por su IP.

### Comandos útiles

| Acción | Comando |
|--------|--------|
| Ver logs | `docker logs -f api-franquicias` |
| Parar | `docker stop api-franquicias` |
| Eliminar contenedor | `docker rm api-franquicias` |
| Eliminar imagen | `docker rmi api-franquicias:latest` |

La API quedará disponible en **http://localhost:8080** (Swagger: http://localhost:8080/swagger-ui.html).

### Levantar todo con Docker Compose

Para levantar la **API y MongoDB** juntos (ideal para desarrollo o verificación local):

Desde la raíz del proyecto (`API-franchises-webFlux-MongoDB/`):

```bash
docker compose up -d --build
```

- `--build` construye la imagen de la API si no existe o si hubo cambios.
- `-d` ejecuta en segundo plano.

**Verificar que estén en ejecución:**

```bash
docker compose ps
```

**Ver logs de la API:**

```bash
docker compose logs -f api
```

**Probar la API:**

- Base: http://localhost:8080  
- Swagger UI: http://localhost:8080/swagger-ui.html  

Por ejemplo, crear una franquicia:

```bash
curl -X POST http://localhost:8080/api/v1/franchises -H "Content-Type: application/json" -d "{\"name\": \"Franquicia Test\"}"
```

**Detener y eliminar contenedores:**

```bash
docker compose down
```

Para eliminar también volúmenes (datos de MongoDB):

```bash
docker compose down -v
```

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

- **Rama principal:** `main`.
- **Desarrollo:** Se crea una rama desde `main` (por ejemplo `develop` o `feature/nombre`) para el desarrollo.
- **Trabajo en la rama:** Se realizan los cambios, commits y `push` a esa rama (no a `main` directamente).
- **Integración a main:** Cuando el desarrollo está listo y funciona, desde GitHub se abre un **Pull Request (PR)** desde la rama de desarrollo hacia `main`. Tras la revisión (y opcionalmente CI), se hace merge a `main`.
- Repositorio público en GitHub para entrega de la prueba.