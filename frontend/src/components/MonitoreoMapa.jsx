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

const coordenadaKey = (item) => `${Number(item.latitud).toFixed(6)},${Number(item.longitud).toFixed(6)}`

const limpiarDescripcion = (descripcion = '') => descripcion.replace(/^\[[^\]]+\]\s*/, '')

const nombreReportante = (reporte) => (
  reporte.ciudadanoNombre || (reporte.ciudadanoId ? `Usuario #${reporte.ciudadanoId}` : 'Sin identificar')
)

const offsetCoordenada = ([lat, lng], index, total) => {
  if (total <= 1) return [lat, lng]
  const radio = 0.00022
  const angulo = (Math.PI * 2 * index) / total - Math.PI / 2
  return [lat + Math.sin(angulo) * radio, lng + Math.cos(angulo) * radio]
}

const crearIconoReporte = (reporte, totalGrupo = 1) => L.divIcon({
  className: 'report-marker-wrapper',
  html: `<div class="report-marker" style="--marker-color: ${INTENSIDAD_COLOR[reporte.intensidad] || '#e94560'}">${totalGrupo > 1 ? totalGrupo : '!'}</div>`,
  iconSize: [38, 38],
  iconAnchor: [19, 19],
})

const crearIconoBurbuja = (reporte, indice) => L.divIcon({
  className: 'report-bubble-wrapper',
  html: `<div class="report-bubble" style="--marker-color: ${INTENSIDAD_COLOR[reporte.intensidad] || '#e94560'}">${indice + 1}</div>`,
  iconSize: [34, 34],
  iconAnchor: [17, 17],
})

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
  const [grupoExpandido, setGrupoExpandido] = useState(null)
  const [reporteResumen, setReporteResumen] = useState(null)
  const [reporteDetalle, setReporteDetalle] = useState(null)

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
    const interval = setInterval(cargarFocos, 30000) // refresh cada 30s
    return () => clearInterval(interval)
  }, [])

  useEffect(() => {
    if (nuevosReportes) cargarFocos()
  }, [nuevosReportes])

  // Centro en Santiago, Chile
  const centro = [-33.45, -70.65]
  const focosCriticos = focos.filter(f => f.intensidad === 'CRITICA').length
  const reportesConUbicacion = reportes.filter((reporte) => Number.isFinite(Number(reporte.latitud)) && Number.isFinite(Number(reporte.longitud)))
  const gruposReportes = Object.values(reportesConUbicacion.reduce((grupos, reporte) => {
    const key = coordenadaKey(reporte)
    grupos[key] = grupos[key] || { key, centro: [Number(reporte.latitud), Number(reporte.longitud)], reportes: [] }
    grupos[key].reportes.push(reporte)
    return grupos
  }, {}))

  const seleccionarReporte = (reporte) => {
    if (reporteResumen?.id === reporte.id) {
      setReporteDetalle(reporte)
      return
    }
    setReporteResumen(reporte)
    setReporteDetalle(null)
  }

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
              {gruposReportes.map((grupo) => {
                if (grupo.reportes.length > 1 && grupoExpandido !== grupo.key) {
                  return (
                    <Marker
                      key={grupo.key}
                      position={grupo.centro}
                      icon={crearIconoReporte(grupo.reportes[0], grupo.reportes.length)}
                      eventHandlers={{
                        click: () => {
                          setGrupoExpandido(grupo.key)
                          setReporteResumen(null)
                          setReporteDetalle(null)
                        }
                      }}
                    >
                      <Popup>
                        <strong>{grupo.reportes.length} reportes en esta ubicación</strong><br />
                        Toca el marcador para desplegarlos.
                      </Popup>
                    </Marker>
                  )
                }

                return grupo.reportes.map((reporte, index) => (
                  <Marker
                    key={reporte.id}
                    position={offsetCoordenada(grupo.centro, index, grupo.reportes.length)}
                    icon={grupo.reportes.length > 1 ? crearIconoBurbuja(reporte, index) : crearIconoReporte(reporte)}
                    eventHandlers={{
                      click: () => seleccionarReporte(reporte)
                    }}
                  >
                    <Popup>
                      <strong>Reporte #{reporte.id}</strong><br />
                      <strong>Reportante:</strong> {nombreReportante(reporte)}<br />
                      <strong>Intensidad:</strong> {reporte.intensidad || 'MEDIA'}<br />
                      <span>Toca nuevamente el marcador para ver detalles.</span>
                    </Popup>
                  </Marker>
                ))
              })}
            </MapContainer>
          </div>
        )}
      </div>

      {reporteDetalle && (
        <aside className="report-detail-panel">
          <div className="report-detail-header">
            <div>
              <span className={`badge badge-${reporteDetalle.tipo?.toLowerCase()}`}>{reporteDetalle.tipo}</span>
              <h3>Reporte #{reporteDetalle.id}</h3>
            </div>
            <button type="button" className="detail-close" onClick={() => setReporteDetalle(null)}>Cerrar</button>
          </div>

          <div className="detail-row">
            <strong>Reportante</strong>
            <span>{nombreReportante(reporteDetalle)}</span>
          </div>
          <div className="detail-row">
            <strong>Intensidad</strong>
            <span>{reporteDetalle.intensidad || 'MEDIA'}</span>
          </div>
          <div className="detail-row">
            <strong>Descripción</strong>
            <p>{limpiarDescripcion(reporteDetalle.descripcion)}</p>
          </div>
          <div className="detail-row">
            <strong>Imagen o evidencia</strong>
            {reporteDetalle.mediaUrl ? (
              reporteDetalle.mediaUrl.startsWith('archivo:') ? (
                <span>{reporteDetalle.mediaUrl.replace('archivo:', '')}</span>
              ) : (
                <img className="detail-media" src={reporteDetalle.mediaUrl} alt="Evidencia del reporte" />
              )
            ) : (
              <span>Sin archivo adjunto</span>
            )}
          </div>
          <div className="detail-static-map">
            <MapContainer
              key={`detail-map-${reporteDetalle.id}`}
              center={[reporteDetalle.latitud, reporteDetalle.longitud]}
              zoom={15}
              dragging={false}
              scrollWheelZoom={false}
              doubleClickZoom={false}
              zoomControl={false}
              attributionControl={false}
              style={{ height: '100%', width: '100%' }}
            >
              <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
              <Marker position={[reporteDetalle.latitud, reporteDetalle.longitud]} />
            </MapContainer>
          </div>
        </aside>
      )}

      <div className="monitoring-footer">
        <div className="monitoring-summary">
          <strong>Reportes: {reportesConUbicacion.length}</strong>
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
