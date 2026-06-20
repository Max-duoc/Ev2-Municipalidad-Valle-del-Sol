import { useState } from 'react'
import { reportesService } from '../services/api'

const PRECISION_MINIMA_METROS = 150
const TIEMPO_BUSQUEDA_GPS_MS = 15000

/**
 * Componente reutilizable: Formulario de reporte de incidentes.
 * Permite al ciudadano enviar un reporte con coordenadas GPS.
 */
export default function ReporteForm({ usuario, onReporteCreado }) {
  const [form, setForm] = useState({
    tipo: 'FORESTAL',
    descripcion: '',
    latitud: '',
    longitud: '',
    mediaUrl: '',
    archivoNombre: '',
    intensidad: 'MEDIA',
    ciudadanoId: usuario?.id ? String(usuario.id) : '',
  })
  const [loading, setLoading] = useState(false)
  const [mensaje, setMensaje] = useState(null)
  const [gpsLoading, setGpsLoading] = useState(false)
  const [ubicacionRegistrada, setUbicacionRegistrada] = useState(false)
  const [precisionGps, setPrecisionGps] = useState(null)

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleArchivoChange = (e) => {
    const archivo = e.target.files?.[0]
    setForm((f) => ({
      ...f,
      archivoNombre: archivo ? archivo.name : '',
      mediaUrl: archivo ? `archivo:${archivo.name}` : '',
    }))
  }

  const obtenerUbicacion = () => {
    if (!window.isSecureContext) {
      setMensaje({
        tipo: 'error',
        texto: 'El navegador bloquea la ubicación porque la app no está abierta en una conexión segura. En teléfono debes usar HTTPS; con una URL local http://192.168... no se mostrará el permiso.'
      })
      setUbicacionRegistrada(false)
      return
    }

    if (!navigator.geolocation) {
      setMensaje({ tipo: 'error', texto: 'Tu navegador no soporta geolocalización.' })
      return
    }
    setGpsLoading(true)
    setUbicacionRegistrada(false)
    setPrecisionGps(null)
    setMensaje({ tipo: 'success', texto: 'Buscando ubicación precisa. Esto puede tardar unos segundos.' })

    const opcionesGps = {
      enableHighAccuracy: true,
      timeout: TIEMPO_BUSQUEDA_GPS_MS,
      maximumAge: 0
    }

    let mejorPosicion = null
    let finalizado = false
    let watchId = null

    const registrarUbicacion = (pos) => {
      const precision = Math.round(pos.coords.accuracy)
      setForm((f) => ({
        ...f,
        latitud: pos.coords.latitude.toFixed(6),
        longitud: pos.coords.longitude.toFixed(6),
      }))
      setPrecisionGps(precision)
      setUbicacionRegistrada(true)
      setMensaje({
        tipo: 'success',
        texto: `Ubicación registrada correctamente con precisión aproximada de ${precision} m.`
      })
      setGpsLoading(false)
    }

    const terminarBusqueda = () => {
      if (finalizado) return
      finalizado = true
      if (watchId !== null) navigator.geolocation.clearWatch(watchId)

      if (!mejorPosicion) {
        setMensaje({
          tipo: 'error',
          texto: 'No se pudo obtener la ubicación. Revisa que el navegador tenga permiso de ubicación y que el GPS esté activo.'
        })
        setGpsLoading(false)
        return
      }

      const precision = Math.round(mejorPosicion.coords.accuracy)
      if (precision > PRECISION_MINIMA_METROS) {
        setForm((f) => ({ ...f, latitud: '', longitud: '' }))
        setPrecisionGps(precision)
        setUbicacionRegistrada(false)
        setMensaje({
          tipo: 'error',
          texto: `La ubicación detectada no es suficientemente precisa (±${precision} m). Intenta desde un teléfono con GPS activo o acércate a una zona con mejor señal.`
        })
        setGpsLoading(false)
        return
      }

      registrarUbicacion(mejorPosicion)
    }

    watchId = navigator.geolocation.watchPosition(
      (pos) => {
        if (!mejorPosicion || pos.coords.accuracy < mejorPosicion.coords.accuracy) {
          mejorPosicion = pos
          setPrecisionGps(Math.round(pos.coords.accuracy))
        }

        if (pos.coords.accuracy <= PRECISION_MINIMA_METROS) {
          terminarBusqueda()
        }
      },
      (error) => {
        console.warn(`Error de geolocalización (${error.code}): ${error.message}`)
        const texto = error.code === error.PERMISSION_DENIED
          ? 'El permiso de ubicación fue bloqueado. Actívalo en la configuración del navegador para este sitio.'
          : 'No se pudo obtener la ubicación. Revisa que el GPS esté activo y que el navegador tenga permiso de ubicación.'
        setMensaje({
          tipo: 'error',
          texto
        })
        setUbicacionRegistrada(false)
        setGpsLoading(false)
      },
      opcionesGps
    )

    setTimeout(terminarBusqueda, TIEMPO_BUSQUEDA_GPS_MS)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!form.descripcion || !form.latitud || !form.longitud) {
      setMensaje({ tipo: 'error', texto: 'Ingresa una descripción y registra tu ubicación antes de enviar.' })
      return
    }

    if (!usuario?.id) {
      setMensaje({ tipo: 'error', texto: 'No se pudo asociar el reporte a la sesión actual. Vuelve a iniciar sesión.' })
      return
    }

    setLoading(true)
    setMensaje(null)
    try {
      const { archivoNombre, ...datosReporte } = form
      const res = await reportesService.crear({
        ...datosReporte,
        ciudadanoId: String(usuario.id),
        ciudadanoNombre: usuario.nombre,
        latitud: parseFloat(form.latitud),
        longitud: parseFloat(form.longitud),
      })
      setMensaje({ tipo: 'success', texto: `✅ Reporte #${res.data.id} creado exitosamente.` })
      setForm((f) => ({ ...f, descripcion: '', mediaUrl: '', archivoNombre: '' }))
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
            <label htmlFor="intensidad">Intensidad Sugerida</label>
            <select id="intensidad" name="intensidad" value={form.intensidad} onChange={handleChange}>
              <option value="BAJA">🟡 Baja</option>
              <option value="MEDIA">🟠 Media</option>
              <option value="ALTA">🔴 Alta</option>
              <option value="CRITICA">🟣 Crítica</option>
            </select>
          </div>

          <div className="form-group full-width location-field">
            <label>Ubicación de la emergencia</label>
            <button
              type="button"
              className={`btn ${ubicacionRegistrada ? 'btn-location-ready' : 'btn-secondary'}`}
              onClick={obtenerUbicacion}
              disabled={gpsLoading}
            >
              {gpsLoading
                ? precisionGps
                  ? `Mejorando precisión... ±${precisionGps} m`
                  : 'Obteniendo ubicación...'
                : ubicacionRegistrada
                  ? `Ubicación registrada${precisionGps ? ` ±${precisionGps} m` : ''}`
                  : 'Usar mi ubicación'}
            </button>
            <span className="field-hint">
              {ubicacionRegistrada
                ? 'La ubicación quedó asociada a este reporte.'
                : gpsLoading
                  ? 'Mantén esta pantalla abierta mientras buscamos la lectura más precisa.'
                  : 'Necesitamos permiso de ubicación para enviar el reporte.'}
            </span>
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
            <label htmlFor="archivoReporte">Archivo adjunto (opcional)</label>
            <label className="file-upload-button" htmlFor="archivoReporte">
              {form.archivoNombre || 'Subir imagen o video'}
            </label>
            <input
              id="archivoReporte"
              className="file-input"
              name="archivoReporte"
              type="file"
              accept="image/*,video/*"
              onChange={handleArchivoChange}
            />
            <span className="field-hint">Puedes adjuntar una imagen o video como evidencia.</span>
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
