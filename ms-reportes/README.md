# ms-reportes

Microservicio responsable de la gestion de reportes ciudadanos y almacenamiento de archivos adjuntos.

## Puerto

`8081`

## Responsabilidades

- Crear reportes de tipo `FORESTAL`, `URBANO` o `SIMULACRO`.
- Listar y filtrar reportes por tipo o estado.
- Actualizar estado de reportes.
- Eliminar reportes.
- Guardar y servir imagenes/videos asociados a reportes.

## Stack

- Spring Boot 3.2
- Spring Web
- Spring Data JPA
- H2 Database
- JaCoCo

## Configuracion

Archivo:

```text
src/main/resources/application.properties
```

Valores principales:

```properties
server.port=8081
spring.datasource.url=jdbc:h2:mem:reportesdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
app.upload-dir=./uploads/reportes
```

Los datos H2 son en memoria. Los archivos se guardan en `./uploads/reportes`.

## Ejecutar

```bash
mvn spring-boot:run
```

Health check:

```bash
curl http://localhost:8081/api/reportes/health
```

## Endpoints

| Metodo | URL | Descripcion |
| --- | --- | --- |
| `POST` | `/api/reportes` | Crear reporte |
| `GET` | `/api/reportes` | Listar todos |
| `GET` | `/api/reportes/{id}` | Obtener por ID |
| `GET` | `/api/reportes/tipo/{tipo}` | Filtrar por tipo |
| `GET` | `/api/reportes/estado/{estado}` | Filtrar por estado |
| `PATCH` | `/api/reportes/{id}/estado` | Actualizar estado |
| `DELETE` | `/api/reportes/{id}` | Eliminar reporte |
| `POST` | `/api/reportes/media` | Subir imagen/video |
| `GET` | `/api/reportes/media/{nombre}` | Descargar/visualizar archivo |
| `GET` | `/api/reportes/health` | Health check |

## Crear reporte

```bash
curl -X POST http://localhost:8081/api/reportes \
  -H "Content-Type: application/json" \
  -d '{
    "tipo": "FORESTAL",
    "descripcion": "Incendio en sector norte",
    "latitud": -33.45,
    "longitud": -70.65,
    "mediaUrl": "",
    "intensidad": "ALTA",
    "ciudadanoId": "1",
    "ciudadanoNombre": "Ana Soto"
  }'
```

Tipos validos:

- `FORESTAL`
- `URBANO`
- `SIMULACRO`

Intensidades usadas por la plataforma:

- `BAJA`
- `MEDIA`
- `ALTA`
- `CRITICA`

## Actualizar estado

```bash
curl -X PATCH http://localhost:8081/api/reportes/1/estado \
  -H "Content-Type: application/json" \
  -d '{"estado":"ATENDIDO"}'
```

## Subir archivo

```bash
curl -X POST http://localhost:8081/api/reportes/media \
  -F "archivo=@/ruta/a/foto.jpg"
```

Solo se aceptan `image/*` y `video/*`.

## Consola H2

URL:

```text
http://localhost:8081/h2-console
```

JDBC URL:

```text
jdbc:h2:mem:reportesdb
```

Usuario: `sa`

Password: vacio.

## Pruebas y cobertura

```bash
mvn clean verify
```

Reporte:

```text
target/site/jacoco/index.html
```
