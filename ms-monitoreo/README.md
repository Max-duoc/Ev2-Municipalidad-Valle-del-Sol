# ms-monitoreo

Microservicio responsable del monitoreo geografico de focos activos.

## Puerto

`8082`

## Responsabilidades

- Registrar focos activos.
- Listar focos activos o historicos.
- Filtrar focos por sector.
- Actualizar estado, intensidad o brigada asignada.
- Eliminar focos.

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
server.port=8082
spring.datasource.url=jdbc:h2:mem:monitoreodb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
```

La base H2 es en memoria.

## Ejecutar

```bash
mvn spring-boot:run
```

Health check:

```bash
curl http://localhost:8082/api/monitoreo/health
```

## Endpoints

| Metodo | URL | Descripcion |
| --- | --- | --- |
| `POST` | `/api/monitoreo/focos` | Registrar foco |
| `GET` | `/api/monitoreo/focos` | Listar todos |
| `GET` | `/api/monitoreo/focos/activos` | Listar solo activos |
| `GET` | `/api/monitoreo/focos/{id}` | Obtener por ID |
| `GET` | `/api/monitoreo/focos/sector/{sector}` | Filtrar por sector |
| `PATCH` | `/api/monitoreo/focos/{id}` | Actualizar foco |
| `DELETE` | `/api/monitoreo/focos/{id}` | Eliminar foco |
| `GET` | `/api/monitoreo/health` | Health check |

## Registrar foco

```bash
curl -X POST http://localhost:8082/api/monitoreo/focos \
  -H "Content-Type: application/json" \
  -d '{
    "latitud": -33.45,
    "longitud": -70.65,
    "intensidad": "ALTA",
    "sector": "Valle Norte",
    "brigadaAsignada": "Brigada 1"
  }'
```

Intensidades validas:

- `BAJA`
- `MEDIA`
- `ALTA`
- `CRITICA`

Estados usados:

- `ACTIVO`
- `CONTROLADO`
- `EXTINGUIDO`

## Actualizar foco

El `PATCH` acepta campos parciales:

```bash
curl -X PATCH http://localhost:8082/api/monitoreo/focos/1 \
  -H "Content-Type: application/json" \
  -d '{
    "estado": "CONTROLADO",
    "intensidad": "BAJA",
    "brigadaAsignada": "Brigada 2"
  }'
```

## Consola H2

URL:

```text
http://localhost:8082/h2-console
```

JDBC URL:

```text
jdbc:h2:mem:monitoreodb
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
