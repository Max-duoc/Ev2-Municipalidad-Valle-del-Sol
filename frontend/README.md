# municipalidad-frontend

Interfaz Web — Módulo de Detección y Reporte / Monitoreo Geográfico  
Municipalidad Valle del Sol

**Desarrollador:** Maxi Martinez (Maxi-duoc)  
**Puerto:** `5173`

## Tecnología
- React 18 + Vite
- React Leaflet (mapa interactivo)
- Axios (llamadas al BFF)
- Módulos NPM reutilizables

## Módulos

| Componente | Descripción |
|-----------|-------------|
| `ReporteForm` | Formulario de reporte con geolocalización GPS |
| `ReportesList` | Listado de reportes con badges de estado |
| `MonitoreoMapa` | Mapa Leaflet con focos activos en tiempo real |
| `services/api.js` | Módulo de servicios HTTP hacia el BFF |

## Requisitos previos
- Node.js >= 18
- BFF corriendo en `http://localhost:8080`

## Instalar y ejecutar

```bash
npm install
npm run dev
```

Abre en: http://localhost:5173

## Build de producción

```bash
npm run build
```

## Tests

```bash
npm test
```
