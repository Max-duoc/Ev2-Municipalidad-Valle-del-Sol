import { useState } from 'react'
import { reportesService } from '../services/api'

/**
 * Componente reutilizable: Formulario de reporte de incidentes.
 * Permite al ciudadano enviar un reporte con coordenadas GPS.
 */
export default function ReporteForm({ onReporteCreado }) {
  const [form, setForm] = useState({
    tipo: 'FORESTAL',
    descripcion: '',
    latitud: '',
    longitud: '',
    mediaUrl: '',
    ciudadanoId: 'ciudadano-web-' + Date.now(),
  })
  const [loading, setLoading] = useState(false)
  const [mensaje, setMensaje] = useState(null)
  const [gpsLoading, setGpsLoading] = useState(false)

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const obtenerUbicacion = () => {
    if (!navigator.geolocation) {
      setMensaje({ tipo: 'error', texto: 'Tu navegador no soporta geolocalización.' })
      return
    }
    setGpsLoading(true)
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setForm((f) => ({
          ...f,
          latitud: pos.coords.latitude.toFixed(6),
          longitud: pos.coords.longitude.toFixed(6),
        }))
        setGpsLoading(false)
      },
      () => {
        setMensaje({ tipo: 'error', texto: 'No se pudo obtener la ubicación.' })
        setGpsLoading(false)
      }
    )
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.descripcion || !form.latitud || !form.longitud) {
      setMensaje({ tipo: 'error', texto: 'Descripción, latitud y longitud son obligatorios.' })
      return
    }

    setLoading(true)
    setMensaje(null)
    try {
      const res = await reportesService.crear({
        ...form,
        latitud: parseFloat(form.latitud),
        longitud: parseFloat(form.longitud),
      })
      setMensaje({ tipo: 'success', texto: `✅ Reporte #${res.data.id} creado exitosamente.` })
      setForm((f) => ({ ...f, descripcion: '', mediaUrl: '' }))
      if (onReporteCreado) onReporteCreado(res.data)
    } catch (err) {
      const msg = err.response?.data?.message || 'Error al enviar el reporte. Verifique que los servicios estén activos.'
      setMensaje({ tipo: 'error', texto: msg })
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="card">
      <h2>🚨 Reportar Incidente</h2>

      {mensaje && (
        <div className={`alert alert-${mensaje.tipo === 'success' ? 'success' : 'error'}`}>
          {mensaje.texto}
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-grid">
          <div className="form-group">
            <label htmlFor="tipo">Tipo de Incidente</label>
            <select id="tipo" name="tipo" value={form.tipo} onChange={handleChange}>
              <option value="FORESTAL">🌲 Incendio Forestal</option>
              <option value="URBANO">🏙️ Incendio Urbano</option>
              <option value="SIMULACRO">🔔 Simulacro</option>
            </select>
          </div>

          <div className="form-group">
            <label>Coordenadas GPS</label>
            <button
              type="button"
              className="btn btn-secondary"
              onClick={obtenerUbicacion}
              disabled={gpsLoading}
            >
              {gpsLoading ? 'Obteniendo...' : '📍 Usar mi ubicación'}
            </button>
          </div>

          <div className="form-group">
            <label htmlFor="latitud">Latitud</label>
            <input
              id="latitud"
              name="latitud"
              type="number"
              step="any"
              placeholder="-33.45"
              value={form.latitud}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label htmlFor="longitud">Longitud</label>
            <input
              id="longitud"
              name="longitud"
              type="number"
              step="any"
              placeholder="-70.65"
              value={form.longitud}
              onChange={handleChange}
            />
          </div>

          <div className="form-group full-width">
            <label htmlFor="descripcion">Descripción del Incidente</label>
            <textarea
              id="descripcion"
              name="descripcion"
              placeholder="Describa lo que está observando..."
              value={form.descripcion}
              onChange={handleChange}
            />
          </div>

          <div className="form-group full-width">
            <label htmlFor="mediaUrl">URL de imagen/video (opcional)</label>
            <input
              id="mediaUrl"
              name="mediaUrl"
              type="url"
              placeholder="https://..."
              value={form.mediaUrl}
              onChange={handleChange}
            />
          </div>
        </div>

        <div style={{ marginTop: '1.2rem' }}>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Enviando...' : '🚀 Enviar Reporte'}
          </button>
        </div>
      </form>
    </div>
  )
}
