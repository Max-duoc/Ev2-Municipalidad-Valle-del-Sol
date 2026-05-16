Puerto: `8080`

##Patrones implementados

- Circuit Breaker — Reciliente en todos los endpoints de `ms-monitoreo`: si el servicio de mapas falla, los reportes siguen operativos

Requisitos previos

- `ms-reportes` corriendo en puerto `8081`
- `ms-monitoreo` corriendo en puerto `8082`

Ejecutar

mvn spring-boot:run

Endpoints

| Método | URL                         | Servicio destino        |
| ------ | --------------------------- | ----------------------- |
| POST   | `/bff/reportes`             | ms-reportes             |
| GET    | `/bff/reportes`             | ms-reportes             |
| GET    | `/bff/reportes/{id}`        | ms-reportes             |
| GET    | `/bff/monitoreo/focos`      | ms-monitoreo _(con CB)_ |
| POST   | `/bff/monitoreo/focos`      | ms-monitoreo _(con CB)_ |
| PATCH  | `/bff/monitoreo/focos/{id}` | ms-monitoreo _(con CB)_ |
| GET    | `/bff/health`               | —                       |

Circuit Breaker

Configurado en `application.properties`:

- Ventana deslizante: 5 llamadas
- Umbral de fallo: 50%
- Tiempo en estado abierto: 10 segundos
- Transición automática a half-open: activada

Cuando el circuit está abierto, el BFF devuelve un fallback con `status: "CIRCUIT_OPEN"` en lugar de bloquear la plataforma.

Tests

mvn test
