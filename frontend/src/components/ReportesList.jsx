import { useState, useEffect } from 'react'
import { reportesService } from '../services/api'

export default function ReportesList({ refresh, usuario }) {
  const [reportes, setReportes] = useState([])
  const [loading, setLoading] = useState(true)

  const cargar = async () => {
    try {
      const res = await reportesService.obtenerTodos()
      const todos = Array.isArray(res.data) ? res.data : []
      const reportesUsuario = usuario?.id
        ? todos.filter((reporte) => String(reporte.ciudadanoId) === String(usuario.id))
        : []
      setReportes(reportesUsuario)
    } catch {
      setReportes([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { cargar() }, [refresh, usuario?.id])

  if (loading) return <div className="loading">Cargando reportes...</div>

  return (
    <div className="card">
      <h2>📋 Reportes Registrados ({reportes.length})</h2>
      {reportes.length === 0 ? (
        <p style={{ color: '#888', textAlign: 'center', padding: '1rem' }}>
          No tienes reportes registrados en esta cuenta.
        </p>
      ) : (
        <div className="reportes-list">
          {reportes.map((r) => (
            <div key={r.id} className="reporte-item">
              <div className="reporte-info">
                <h4>#{r.id} — {r.descripcion}</h4>
                <p>📍 {r.latitud}, {r.longitud}</p>
                <p>🕒 {new Date(r.fechaCreacion).toLocaleString('es-CL')}</p>
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.4rem', alignItems: 'flex-end' }}>
                <span className={`badge badge-${r.tipo?.toLowerCase()}`}>{r.tipo}</span>
                <span className={`badge badge-${r.estado?.toLowerCase()}`}>{r.estado}</span>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
