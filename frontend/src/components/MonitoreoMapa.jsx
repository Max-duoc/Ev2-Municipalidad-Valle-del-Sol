import { useState, useEffect } from 'react'
import { MapContainer, TileLayer, Marker, Popup, Circle } from 'react-leaflet'
import L from 'leaflet'
import { monitoreoService } from '../services/api'

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

/**
 * Componente reutilizable: Mapa de monitoreo geográfico en tiempo real.
 * Muestra focos activos con círculos de intensidad sobre mapa Leaflet.
 */
export default function MonitoreoMapa({ nuevosReportes }) {
  const [focos, setFocos] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [ultimaActualizacion, setUltimaActualizacion] = useState(null)

  const cargarFocos = async () => {
    try {
      const res = await monitoreoService.obtenerFocosActivos()
      const data = res.data
      // Manejo de respuesta Circuit Breaker
      if (data?.status === 'CIRCUIT_OPEN') {
        setError('⚠️ Servicio de monitoreo temporalmente no disponible (Circuit Breaker activo).')
        setFocos([])
      } else {
        setFocos(Array.isArray(data) ? data : [])
        setError(null)
      }
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

  return (
    <div className="card">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
        <h2>🗺️ Monitoreo Geográfico en Tiempo Real</h2>
        <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
          {ultimaActualizacion && (
            <span style={{ fontSize: '0.78rem', color: '#888' }}>
              Actualizado: {ultimaActualizacion}
            </span>
          )}
          <button className="btn btn-secondary" onClick={cargarFocos} style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem' }}>
            🔄 Actualizar
          </button>
        </div>
      </div>

      {error && <div className="alert alert-warning">{error}</div>}

      {loading ? (
        <div className="loading">Cargando mapa...</div>
      ) : (
        <div className="map-container">
          <MapContainer center={centro} zoom={11} style={{ height: '100%', width: '100%' }}>
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            {focos.map((foco) => (
              <div key={foco.id}>
                <Circle
                  center={[foco.latitud, foco.longitud]}
                  radius={INTENSIDAD_RADIO[foco.intensidad] || 500}
                  pathOptions={{
                    color: INTENSIDAD_COLOR[foco.intensidad] || '#888',
                    fillColor: INTENSIDAD_COLOR[foco.intensidad] || '#888',
                    fillOpacity: 0.3,
                  }}
                />
                <Marker position={[foco.latitud, foco.longitud]}>
                  <Popup>
                    <strong>🔥 Foco #{foco.id}</strong><br />
                    <strong>Sector:</strong> {foco.sector}<br />
                    <strong>Intensidad:</strong> {foco.intensidad}<br />
                    <strong>Estado:</strong> {foco.estado}<br />
                    {foco.brigadaAsignada && <><strong>Brigada:</strong> {foco.brigadaAsignada}</>}
                  </Popup>
                </Marker>
              </div>
            ))}
          </MapContainer>
        </div>
      )}

      {/* Leyenda */}
      <div style={{ marginTop: '1rem', display: 'flex', gap: '1rem', flexWrap: 'wrap' }}>
        {Object.entries(INTENSIDAD_COLOR).map(([nivel, color]) => (
          <div key={nivel} style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.82rem' }}>
            <div style={{ width: 12, height: 12, borderRadius: '50%', background: color }} />
            {nivel}
          </div>
        ))}
      </div>

      {/* Stats */}
      <div style={{ marginTop: '1rem', padding: '0.75rem', background: '#f8f9fa', borderRadius: '8px', fontSize: '0.85rem' }}>
        <strong>Focos activos: {focos.length}</strong>
        {focos.filter(f => f.intensidad === 'CRITICA').length > 0 && (
          <span style={{ color: '#9c27b0', marginLeft: '1rem' }}>
            ⚠️ {focos.filter(f => f.intensidad === 'CRITICA').length} crítico(s)
          </span>
        )}
      </div>
    </div>
  )
}
