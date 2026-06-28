# BFF - Backend For Frontend

Servicio de entrada para el frontend. Orquesta llamadas hacia `ms-reportes`, `ms-monitoreo` y `ms-usuarios`, y expone una API unica bajo `/bff`.

## Puerto

`8080`

## Responsabilidades

- Reenviar autenticacion y administracion hacia `ms-usuarios`.
- Crear, listar, actualizar y eliminar reportes en `ms-reportes`.
- Registrar focos y consultar monitoreo en `ms-monitoreo`.
- Crear un foco automatico cuando se registra un reporte.
- Publicar notificaciones cuando ocurren eventos relevantes.
- Aplicar Circuit Breaker sobre operaciones de monitoreo.

## Requisitos

Servicios esperados:

| Servicio | URL local |
| --- | --- |
| `ms-reportes` | `http://localhost:8081` |
| `ms-monitoreo` | `http://localhost:8082` |
| `ms-usuarios` | `http://localhost:8083` |

## Configuracion

Archivo principal:

```text
src/main/resources/application.properties
```

Propiedades:

```properties
services.reportes.url=http://localhost:8081
services.monitoreo.url=http://localhost:8082
services.usuarios.url=http://localhost:8083
```

En Docker Compose se sobreescriben con variables de entorno equivalentes:

```yaml
SERVICES_REPORTES_URL: http://ms-reportes:8081
SERVICES_MONITOREO_URL: http://ms-monitoreo:8082
SERVICES_USUARIOS_URL: http://ms-usuarios:8083
```

## Ejecutar

```bash
mvn spring-boot:run
```

Health check:

```bash
curl http://localhost:8080/bff/health
```

## Endpoints principales

### Autenticacion y usuarios

| Metodo | URL | Descripcion |
| --- | --- | --- |
| `POST` | `/bff/auth/register` | Registro ciudadano |
| `POST` | `/bff/auth/login` | Login |
| `GET` | `/bff/auth/me` | Sesion actual |
| `POST` | `/bff/auth/logout` | Cerrar sesion |
| `GET` | `/bff/auth/usuarios` | Listar usuarios, requiere admin |
| `POST` | `/bff/auth/usuarios` | Crear admin/brigada, requiere admin |
| `GET` | `/bff/auth/notificaciones` | Notificaciones pendientes |
| `PATCH` | `/bff/auth/notificaciones/{id}/leida` | Marcar notificacion leida |

### Reportes

| Metodo | URL | Descripcion |
| --- | --- | --- |
| `POST` | `/bff/reportes` | Crear reporte y foco automatico |
| `GET` | `/bff/reportes` | Listar reportes |
| `GET` | `/bff/reportes/{id}` | Obtener reporte |
| `PATCH` | `/bff/reportes/{id}/estado` | Actualizar estado |
| `DELETE` | `/bff/reportes/{id}` | Eliminar reporte, requiere admin |
| `POST` | `/bff/reportes/media` | Subir imagen/video |
| `GET` | `/bff/reportes/media/{nombre}` | Obtener archivo |

### Monitoreo

| Metodo | URL | Descripcion |
| --- | --- | --- |
| `GET` | `/bff/monitoreo/focos` | Focos activos, con Circuit Breaker |
| `POST` | `/bff/monitoreo/focos` | Registrar foco, con Circuit Breaker |
| `PATCH` | `/bff/monitoreo/focos/{id}` | Actualizar foco/asignar brigada, requiere admin |

## Autorizacion

Los endpoints administrativos requieren:

```http
Authorization: Bearer <token>
```

El token se obtiene desde `/bff/auth/login`.

## Ejemplo

```bash
curl -X POST http://localhost:8080/bff/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@valledelsol.cl","password":"admin123"}'
```

```bash
curl http://localhost:8080/bff/reportes
```

## Circuit Breaker

Configurado para `ms-monitoreo`:

- Ventana deslizante: 5 llamadas
- Umbral de fallo: 50%
- Espera en abierto: 10 segundos
- Transicion automatica a half-open: activa

Si `ms-monitoreo` falla, el BFF responde con `status: "CIRCUIT_OPEN"` y mantiene operativas las funciones de reportes.

## Pruebas y cobertura

```bash
mvn clean verify
```

Reporte HTML:

```text
target/site/jacoco/index.html
```
