# ms-usuarios

Microservicio responsable de autenticacion, sesiones, administracion de usuarios y notificaciones.

## Puerto

`8083`

## Responsabilidades

- Registrar ciudadanos.
- Login y cierre de sesion.
- Consultar la sesion actual con token Bearer.
- Crear usuarios administrativos (`ADMIN` y `BRIGADA`).
- Listar usuarios.
- Crear y consultar notificaciones.
- Marcar notificaciones como leidas.

## Stack

- Spring Boot 3.2
- Spring Web
- Spring Data JPA
- H2 Database en archivo
- BCrypt para passwords
- JaCoCo

## Configuracion

Archivo:

```text
src/main/resources/application.properties
```

Valores principales:

```properties
server.port=8083
spring.datasource.url=jdbc:h2:file:./data/usuariosdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.jpa.hibernate.ddl-auto=update
```

La base se guarda en:

```text
./data/usuariosdb
```

## Usuario administrador inicial

Al arrancar, si no existe ningun usuario `ADMIN`, se crea:

- Email: `admin@valledelsol.cl`
- Password: `admin123`

Usa esas credenciales para entrar al panel de administracion desde el frontend.

## Ejecutar

```bash
mvn spring-boot:run
```

Health check:

```bash
curl http://localhost:8083/api/auth/health
```

## Endpoints de autenticacion

| Metodo | URL | Descripcion |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Registrar ciudadano |
| `POST` | `/api/auth/login` | Iniciar sesion |
| `GET` | `/api/auth/me` | Obtener sesion actual |
| `POST` | `/api/auth/logout` | Cerrar sesion |
| `GET` | `/api/auth/usuarios` | Listar usuarios, requiere admin |
| `POST` | `/api/auth/usuarios` | Crear admin/brigada, requiere admin |
| `GET` | `/api/auth/health` | Health check |

## Endpoints de notificaciones

| Metodo | URL | Descripcion |
| --- | --- | --- |
| `GET` | `/api/notificaciones` | Notificaciones pendientes del usuario autenticado |
| `PATCH` | `/api/notificaciones/{id}/leida` | Marcar como leida |
| `POST` | `/api/notificaciones/broadcast` | Crear notificacion para todos los usuarios |

## Registro ciudadano

```bash
curl -X POST http://localhost:8083/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Ana Soto",
    "email": "ana@test.cl",
    "password": "secreto1"
  }'
```

## Login

```bash
curl -X POST http://localhost:8083/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@valledelsol.cl",
    "password": "admin123"
  }'
```

Respuesta esperada:

```json
{
  "token": "uuid-de-sesion",
  "usuario": {
    "id": 1,
    "nombre": "Administrador General",
    "email": "admin@valledelsol.cl",
    "rol": "ADMIN",
    "creadoEn": "..."
  }
}
```

## Usar token Bearer

```bash
curl http://localhost:8083/api/auth/me \
  -H "Authorization: Bearer <token>"
```

## Crear brigada o administrador

Requiere token de usuario `ADMIN`.

```bash
curl -X POST http://localhost:8083/api/auth/usuarios \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <token-admin>" \
  -d '{
    "nombre": "Brigada Norte",
    "email": "brigada@test.cl",
    "password": "brigada1",
    "rol": "BRIGADA"
  }'
```

Roles administrativos validos:

- `ADMIN`
- `BRIGADA`

## Consola H2

URL:

```text
http://localhost:8083/h2-console
```

JDBC URL:

```text
jdbc:h2:file:./data/usuariosdb
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
