# Pruebas unitarias y cobertura

## Ejecutar backend

Desde la raiz del repositorio:

```bash
cd ms-reportes && mvn clean verify
cd ../ms-monitoreo && mvn clean verify
cd ../ms-usuarios && mvn clean verify
cd ../bff && mvn clean verify
```

Reportes HTML:

- `ms-reportes/target/site/jacoco/index.html`
- `ms-monitoreo/target/site/jacoco/index.html`
- `ms-usuarios/target/site/jacoco/index.html`
- `bff/target/site/jacoco/index.html`

## Ejecutar frontend

```bash
cd frontend
npm install
npm run test:coverage
```

Reporte HTML:

- `frontend/coverage/index.html`

## Organizacion de pruebas

- Servicios backend: `src/test/java/.../service`
- Factories backend: `src/test/java/.../factory`
- Controladores backend/BFF: `src/test/java/.../controller`
- Componentes React: `frontend/src/components/__tests__`
- Servicios HTTP React: `frontend/src/services/__tests__`
