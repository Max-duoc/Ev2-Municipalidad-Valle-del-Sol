import { useState, useEffect } from 'react'
import { MapContainer, TileLayer, Marker, Popup, Circle } from 'react-leaflet'
import L from 'leaflet'
import { monitoreoService, reportesService } from '../services/api'

// Fix Leaflet default marker icons
delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
})

const INTENSIDAD_COLOR = {
  BAJA: '#ffeb3b',
  MEDIA: '#ff9800',
  ALTA: '#f44336',
  CRITICA: '#9c27b0',
}

const INTENSIDAD_RADIO = {
  BAJA: 300,
  MEDIA: 600,
  ALTA: 1000,
  CRITICA: 1500,
}

const RADIO_AGRUPACION_METROS = 350
const INTERVALO_ACTUALIZACION_MS = 5000
const PRIORIDAD_INTENSIDAD = {
  BAJA: 1,
  MEDIA: 2,
  ALTA: 3,
  CRITICA: 4,
}

const limpiarDescripcion = (descripcion = '') => descripcion.replace(/^\[[^\]]+\]\s*/, '')

const nombreReportante = (reporte) => (
  reporte.ciudadanoNombre || (reporte.ciudadanoId ? `Usuario #${reporte.ciudadanoId}` : 'Sin identificar')
)

const esVideo = (url = '') => /\.(mp4|webm|ogg|mov)$/i.test(url)

const distanciaMetros = (a, b) => {
  const radioTierra = 6371000
  const lat1 = a[0] * Math.PI / 180
  const lat2 = b[0] * Math.PI / 180
  const deltaLat = (b[0] - a[0]) * Math.PI / 180
  const deltaLng = (b[1] - a[1]) * Math.PI / 180
  const haversine = Math.sin(deltaLat / 2) ** 2
    + Math.cos(lat1) * Math.cos(lat2) * Math.sin(deltaLng / 2) ** 2

  return radioTierra * 2 * Math.atan2(Math.sqrt(haversine), Math.sqrt(1 - haversine))
}

const intensidadPrincipal = (reportes) => reportes.reduce((principal, reporte) => {
  const intensidad = reporte.intensidad || 'MEDIA'
  return (PRIORIDAD_INTENSIDAD[intensidad] || 0) > (PRIORIDAD_INTENSIDAD[principal] || 0)
    ? intensidad
    : principal
}, 'MEDIA')

const crearGruposCercanos = (reportes) => reportes.reduce((grupos, reporte) => {
  const posicion = [Number(reporte.latitud), Number(reporte.longitud)]
  const grupoCercano = grupos.find((grupo) => distanciaMetros(grupo.centro, posicion) <= RADIO_AGRUPACION_METROS)

  if (grupoCercano) {
    grupoCercano.reportes.push(reporte)
    const total = grupoCercano.reportes.length
    grupoCercano.centro = [
      ((grupoCercano.centro[0] * (total - 1)) + posicion[0]) / total,
      ((grupoCercano.centro[1] * (total - 1)) + posicion[1]) / total,
    ]
    grupoCercano.intensidad = intensidadPrincipal(grupoCercano.reportes)
    return grupos
  }

  grupos.push({
    key: `foco-${reporte.id}`,
    centro: posicion,
    intensidad: reporte.intensidad || 'MEDIA',
    reportes: [reporte],
  })
  return grupos
}, [])

const etiquetaFoco = (grupo) => {
  const primero = grupo.reportes[0]
  const extra = grupo.reportes.length > 1 ? ` +${grupo.reportes.length - 1}` : ''
  return `${nombreReportante(primero)}${extra} · ${grupo.intensidad || 'MEDIA'}`
}

const crearIconoFoco = (grupo) => L.divIcon({
  className: 'report-marker-wrapper',
  html: `<div class="report-marker" style="--marker-color: ${INTENSIDAD_COLOR[grupo.intensidad] || '#e94560'}">${grupo.reportes.length}</div>`,
  iconSize: [38, 38],
  iconAnchor: [19, 19],
})

const abrirUbicacionEnMapa = (reporte) => {
  const confirmado = window.confirm('¿Quieres abrir esta ubicación en tu aplicación de mapas?')
  if (!confirmado) return

  const lat = Number(reporte.latitud)
  const lng = Number(reporte.longitud)
  window.open(`https://www.google.com/maps/search/?api=1&query=${lat},${lng}`, '_blank', 'noopener,noreferrer')
}

/**
 * Componente reutilizable: Mapa de monitoreo geográfico en tiempo real.
 * Muestra focos activos con círculos de intensidad sobre mapa Leaflet.
 */
export default function MonitoreoMapa({ nuevosReportes }) {
  const [focos, setFocos] = useState([])
  const [reportes, setReportes] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [ultimaActualizacion, setUltimaActualizacion] = useState(null)
  const [grupoDetalle, setGrupoDetalle] = useState(null)

  const cargarFocos = async () => {
    try {
      const [resFocos, resReportes] = await Promise.all([
        monitoreoService.obtenerFocosActivos(),
        reportesService.obtenerTodos(),
      ])
      const data = resFocos.data
      // Manejo de respuesta Circuit Breaker
      if (data?.status === 'CIRCUIT_OPEN') {
        setError('⚠️ Servicio de monitoreo temporalmente no disponible (Circuit Breaker activo).')
        setFocos([])
      } else {
        setFocos(Array.isArray(data) ? data : [])
        setError(null)
      }
      setReportes(Array.isArray(resReportes.data) ? resReportes.data : [])
      setUltimaActualizacion(new Date().toLocaleTimeString('es-CL'))
    } catch {
      setError('No se pudo conectar con el servicio de monitoreo.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    cargarFocos()
    const interval = setInterval(cargarFocos, INTERVALO_ACTUALIZACION_MS)
    return () => clearInterval(interval)
  }, [])

  useEffect(() => {
    if (nuevosReportes) cargarFocos()
  }, [nuevosReportes])

  // Centro en Santiago, Chile
  const centro = [-33.45, -70.65]
  const focosCriticos = focos.filter(f => f.intensidad === 'CRITICA').length
  const reportesConUbicacion = reportes.filter((reporte) => Number.isFinite(Number(reporte.latitud)) && Number.isFinite(Number(reporte.longitud)))
  const gruposReportes = crearGruposCercanos(reportesConUbicacion)

  useEffect(() => {
    if (!grupoDetalle) return

    const grupoActualizado = gruposReportes.find((grupo) => (
      grupo.reportes.some((reporte) => grupoDetalle.reportes.some((actual) => actual.id === reporte.id))
    ))

    setGrupoDetalle(grupoActualizado || null)
  }, [reportes])

  return (
    <section className="monitoring-card">
      <div className="monitoring-header">
        <div>
          <h2>Monitoreo geográfico</h2>
          <p>Focos activos y zonas de intensidad en tiempo real.</p>
        </div>

        <div className="monitoring-actions">
          {ultimaActualizacion && (
            <span className="monitoring-updated">
              Actualizado: {ultimaActualizacion}
            </span>
          )}
          <button className="btn btn-secondary monitoring-refresh" onClick={cargarFocos}>
            Actualizar
          </button>
        </div>
      </div>

      {error && <div className="alert alert-warning">{error}</div>}

      <div className="monitoring-map-shell">
        {loading ? (
          <div className="loading">Cargando mapa...</div>
        ) : (
          <div className="map-container monitoring-map">
            <MapContainer center={centro} zoom={11} style={{ height: '100%', width: '100%' }}>
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
              {focos.map((foco) => (
                <Circle
                  key={foco.id}
                  center={[foco.latitud, foco.longitud]}
                  radius={INTENSIDAD_RADIO[foco.intensidad] || 500}
                  pathOptions={{
                    color: INTENSIDAD_COLOR[foco.intensidad] || '#888',
                    fillColor: INTENSIDAD_COLOR[foco.intensidad] || '#888',
                    fillOpacity: 0.3,
                  }}
                />
              ))}
              {gruposReportes.map((grupo) => (
                <Marker
                  key={grupo.key}
                  position={grupo.centro}
                  icon={crearIconoFoco(grupo)}
                  eventHandlers={{
                    click: () => setGrupoDetalle(grupo)
                  }}
                >
                  <Popup>
                    <strong>{etiquetaFoco(grupo)}</strong><br />
                    <span>{grupo.reportes.length === 1 ? '1 aviso en este foco' : `${grupo.reportes.length} avisos agrupados en este foco`}</span>
                  </Popup>
                </Marker>
              ))}
            </MapContainer>
          </div>
        )}
      </div>

      {grupoDetalle && (
        <aside className="report-detail-panel">
          <div className="report-detail-header">
            <div>
              <span className="badge badge-intensidad">{grupoDetalle.intensidad}</span>
              <h3>{etiquetaFoco(grupoDetalle)}</h3>
              <p>{grupoDetalle.reportes.length === 1 ? '1 aviso asociado' : `${grupoDetalle.reportes.length} avisos asociados`}</p>
            </div>
            <button type="button" className="detail-close" onClick={() => setGrupoDetalle(null)}>Cerrar</button>
          </div>

          <div className="detail-report-list">
            {grupoDetalle.reportes.map((reporte) => (
              <article key={reporte.id} className="detail-report-card">
                <div className="detail-report-title">
                  <strong>{nombreReportante(reporte)}</strong>
                  <span className="badge badge-intensidad">{reporte.intensidad || 'MEDIA'}</span>
                </div>

                <div className="detail-row">
                  <strong>Descripción</strong>
                  <p>{limpiarDescripcion(reporte.descripcion)}</p>
                </div>
                <div className="detail-row">
                  <strong>Imagen o evidencia</strong>
                  {reporte.mediaUrl ? (
                    esVideo(reporte.mediaUrl) ? (
                      <video className="detail-media" src={reporte.mediaUrl} controls />
                    ) : (
                      <img className="detail-media" src={reporte.mediaUrl} alt="Evidencia del aviso" />
                    )
                  ) : (
                    <span>Sin archivo adjunto</span>
                  )}
                </div>
                <div
                  className="detail-static-map"
                  role="button"
                  tabIndex={0}
                  onClick={() => abrirUbicacionEnMapa(reporte)}
                  onKeyDown={(event) => {
                    if (event.key === 'Enter' || event.key === ' ') abrirUbicacionEnMapa(reporte)
                  }}
                  title="Abrir ubicación en mapas"
                >
                  <MapContainer
                    key={`detail-map-${reporte.id}`}
                    center={[reporte.latitud, reporte.longitud]}
                    zoom={15}
                    dragging={false}
                    scrollWheelZoom={false}
                    doubleClickZoom={false}
                    zoomControl={false}
                    attributionControl={false}
                    style={{ height: '100%', width: '100%' }}
                  >
                    <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                    <Marker position={[reporte.latitud, reporte.longitud]} />
                  </MapContainer>
                  <span className="detail-map-hint">Abrir ubicación</span>
                </div>
              </article>
            ))}
          </div>
        </aside>
      )}

      <div className="monitoring-footer">
        <div className="monitoring-summary">
          <strong>Avisos: {reportesConUbicacion.length}</strong>
          <span>Focos activos: {focos.length}</span>
          {focosCriticos > 0 && (
            <span>{focosCriticos} crítico(s)</span>
          )}
        </div>

        <div className="monitoring-legend" aria-label="Leyenda de intensidad">
          {Object.entries(INTENSIDAD_COLOR).map(([nivel, color]) => (
            <div key={nivel} className="legend-item">
              <span className="legend-dot" style={{ background: color }} />
              {nivel}
            </div>
          ))}
        </div>
      </div>
    </section>
  )
}
