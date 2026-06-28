# Frontend - Municipalidad Valle del Sol

Interfaz web React para reportar emergencias, visualizar el monitoreo geografico y administrar usuarios/brigadas.

## Stack

- React 18
- Vite
- Axios
- React Leaflet + Leaflet
- Vitest + Testing Library

## Puerto

`5173`

## Requisitos

- Node.js 18+
- BFF disponible en `http://localhost:8080`

En desarrollo local, Vite proxya las llamadas `/bff` hacia el BFF. El destino se configura con:

```bash
VITE_BFF_PROXY_TARGET=http://localhost:8080
```

Si no se define, usa `http://localhost:8080`.

## Instalacion

```bash
npm install
```

## Ejecutar en desarrollo

```bash
npm run dev
```

Abrir:

```text
http://localhost:5173
```

## Uso de la aplicacion

1. Inicia sesion con una cuenta existente o registra una cuenta ciudadana.
2. Cuenta admin inicial del backend:
   - Email: `admin@valledelsol.cl`
   - Password: `admin123`
3. Pestanas principales:
   - `Reportar`: crea reportes con descripcion, ubicacion GPS y archivo opcional.
   - `Monitoreo`: muestra focos activos y reportes agrupados en el mapa.
   - `Historial`: lista reportes creados por el usuario actual.
   - `Usuarios`: panel solo para administradores.

## Permisos de ubicacion

El formulario de reportes usa geolocalizacion del navegador. En moviles o redes externas se requiere HTTPS para que el navegador muestre el permiso GPS. En desarrollo local funciona normalmente con `localhost`.

## Scripts

```bash
npm run dev
```

Inicia Vite.

```bash
npm run build
```

Genera build de produccion en `dist`.

```bash
npm test
```

Ejecuta Vitest en modo interactivo.

```bash
npm run test:coverage
```

Ejecuta pruebas unitarias y genera cobertura HTML.

## Pruebas

Las pruebas estan organizadas por componente:

- `src/components/__tests__/AuthForm.test.jsx`
- `src/components/__tests__/ReporteForm.test.jsx`
- `src/components/__tests__/ReportesList.test.jsx`
- `src/components/__tests__/MonitoreoMapa.test.jsx`
- `src/components/__tests__/AdminPanel.test.jsx`
- `src/components/__tests__/NotificationCenter.test.jsx`
- `src/services/__tests__/api.test.js`

Reporte:

```text
coverage/index.html
```

## Variables utiles

| Variable | Valor por defecto | Uso |
| --- | --- | --- |
| `VITE_BFF_PROXY_TARGET` | `http://localhost:8080` | Destino del proxy `/bff` en Vite |

## Problemas comunes

- Si el login falla, revisa que `ms-usuarios` y `bff` esten corriendo.
- Si el mapa no carga datos, revisa `ms-monitoreo` y `ms-reportes`.
- Si el GPS no pide permisos en celular, usa HTTPS o abre desde `localhost` en desarrollo.
