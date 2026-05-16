Puerto: `8081`

Patrones implementados

- Repository Pattern — `ReporteRepository`: desacopla la lógica de negocio del acceso a datos JPA
- Factory Method — `ReporteFactory`: centraliza la creación de reportes FORESTAL, URBANO y SIMULACRO

Ejecutar

mvn spring-boot:run

Endpoints

| Método | URL                         | Descripción       |
| ------ | --------------------------- | ----------------- |
| POST   | `/api/reportes`             | Crear reporte     |
| GET    | `/api/reportes`             | Listar todos      |
| GET    | `/api/reportes/{id}`        | Obtener por ID    |
| GET    | `/api/reportes/tipo/{tipo}` | Filtrar por tipo  |
| PATCH  | `/api/reportes/{id}/estado` | Actualizar estado |
| GET    | `/api/reportes/health`      | Health check      |

Ejemplo POST

json
{
"tipo": "FORESTAL",
"descripcion": "Incendio en sector norte",
"latitud": -33.45,
"longitud": -70.65,
"ciudadanoId": "ciudadano-001"
}

Consola H2

Disponible en: http://localhost:8081/h2-console  
JDBC URL: `jdbc:h2:mem:reportesdb`

Tests

bash
mvn test
