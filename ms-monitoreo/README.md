Puerto: `8082`

Patrones implementados

- Repository Pattern — `FocoActivoRepository`: abstrae el acceso a datos espaciales (preparado para migración a PostGIS)

Ejecutar

bash
mvn spring-boot:run

Endpoints

| Método | URL                                    | Descripción            |
| ------ | -------------------------------------- | ---------------------- |
| POST   | `/api/monitoreo/focos`                 | Registrar foco activo  |
| GET    | `/api/monitoreo/focos`                 | Listar todos los focos |
| GET    | `/api/monitoreo/focos/activos`         | Solo focos activos     |
| GET    | `/api/monitoreo/focos/{id}`            | Obtener por ID         |
| GET    | `/api/monitoreo/focos/sector/{sector}` | Filtrar por sector     |
| PATCH  | `/api/monitoreo/focos/{id}`            | Actualizar foco        |
| GET    | `/api/monitoreo/health`                | Health check           |

Ejemplo POST

json
{
"latitud": -33.45,
"longitud": -70.65,
"intensidad": "ALTA",
"sector": "Valle Norte",
"brigadaAsignada": "Brigada-1"
}

Intensidades válidas: `BAJA`, `MEDIA`, `ALTA`, `CRITICA`

Consola H2

Disponible en: http://localhost:8082/h2-console  
JDBC URL: `jdbc:h2:mem:monitoreodb`

Tests

bash
mvn test
