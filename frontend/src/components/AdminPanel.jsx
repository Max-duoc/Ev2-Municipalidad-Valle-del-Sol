import { useEffect, useState } from 'react'
import { adminService, monitoreoService, reportesService } from '../services/api'

const FORM_INICIAL = {
  nombre: '',
  email: '',
  password: '',
  rol: 'BRIGADA',
}

const FILTROS_INICIALES = {
  tipo: 'TODOS',
  estado: 'TODOS',
  intensidad: 'TODAS',
  fecha: '',
}

const etiquetaRol = {
  ADMIN: 'Administrador',
  BRIGADA: 'Brigada',
  CIUDADANO: 'Ciudadano',
}

const ESTADOS_REPORTE = ['PENDIENTE', 'ACTIVO', 'EN_REVISION', 'ATENDIDO', 'CERRADO', 'SIMULACRO']
const TIPOS_REPORTE = ['TODOS', 'FORESTAL', 'URBANO', 'SIMULACRO']
const INTENSIDADES = ['TODAS', 'BAJA', 'MEDIA', 'ALTA', 'CRITICA']

const limpiarDescripcion = (descripcion = '') => descripcion.replace(/^\[[^\]]+\]\s*/, '')

export default function AdminPanel() {
  const [usuarios, setUsuarios] = useState([])
  const [reportes, setReportes] = useState([])
  const [focos, setFocos] = useState([])
  const [form, setForm] = useState(FORM_INICIAL)
  const [filtros, setFiltros] = useState(FILTROS_INICIALES)
  const [loading, setLoading] = useState(true)
  const [guardando, setGuardando] = useState(false)
  const [procesando, setProcesando] = useState(false)
  const [mensaje, setMensaje] = useState(null)

  const cargarUsuarios = async () => {
    const response = await adminService.listarUsuarios()
    setUsuarios(Array.isArray(response.data) ? response.data : [])
  }

  const cargarOperacion = async () => {
    const [resReportes, resFocos] = await Promise.all([
      reportesService.obtenerTodos(),
      monitoreoService.obtenerFocosActivos(),
    ])
    setReportes(Array.isArray(resReportes.data) ? resReportes.data : [])
    setFocos(Array.isArray(resFocos.data) ? resFocos.data : [])
  }

  const cargarTodo = async () => {
    setLoading(true)
    try {
      await Promise.all([cargarUsuarios(), cargarOperacion()])
    } catch (error) {
      setMensaje({
        tipo: 'error',
        texto: error.response?.data?.message || 'No se pudo cargar la administración.',
      })
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    cargarTodo()
  }, [])

  const handleChange = (event) => {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  const cambiarFiltro = (event) => {
    setFiltros({ ...filtros, [event.target.name]: event.target.value })
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setGuardando(true)
    setMensaje(null)

    try {
      await adminService.crearUsuario(form)
      setMensaje({ tipo: 'success', texto: `${etiquetaRol[form.rol]} creado correctamente.` })
      setForm(FORM_INICIAL)
      await cargarTodo()
    } catch (error) {
      setMensaje({
        tipo: 'error',
        texto: error.response?.data?.message || 'No se pudo crear el usuario.',
      })
    } finally {
      setGuardando(false)
    }
  }

  const actualizarEstado = async (id, estado) => {
    setProcesando(true)
    setMensaje(null)
    try {
      await reportesService.actualizarEstado(id, estado)
      setMensaje({ tipo: 'success', texto: `Reporte #${id} actualizado a ${estado}.` })
      await cargarOperacion()
    } catch (error) {
      setMensaje({ tipo: 'error', texto: error.response?.data?.message || 'No se pudo actualizar el reporte.' })
    } finally {
      setProcesando(false)
    }
  }

  const eliminarReporte = async (id) => {
    if (!window.confirm(`¿Eliminar definitivamente el reporte #${id}?`)) return
    setProcesando(true)
    setMensaje(null)
    try {
      await reportesService.eliminar(id)
      setMensaje({ tipo: 'success', texto: `Reporte #${id} eliminado.` })
      await cargarOperacion()
    } catch (error) {
      setMensaje({ tipo: 'error', texto: error.response?.data?.message || 'No se pudo eliminar el reporte.' })
    } finally {
      setProcesando(false)
    }
  }

  const asignarBrigada = async (foco, brigadaAsignada) => {
    setProcesando(true)
    setMensaje(null)
    try {
      await monitoreoService.actualizarFoco(foco.id, { brigadaAsignada })
      setMensaje({ tipo: 'success', texto: `Foco #${foco.id} asignado a ${brigadaAsignada || 'Sin asignar'}.` })
      await cargarOperacion()
    } catch (error) {
      setMensaje({ tipo: 'error', texto: error.response?.data?.message || 'No se pudo asignar la brigada.' })
    } finally {
      setProcesando(false)
    }
  }

  const resumen = usuarios.reduce((acc, usuario) => {
    acc[usuario.rol] = (acc[usuario.rol] || 0) + 1
    return acc
  }, {})

  const brigadas = usuarios.filter((usuario) => usuario.rol === 'BRIGADA')
  const reportesFiltrados = reportes.filter((reporte) => {
    const fechaReporte = reporte.fechaCreacion ? reporte.fechaCreacion.slice(0, 10) : ''
    return (filtros.tipo === 'TODOS' || reporte.tipo === filtros.tipo)
      && (filtros.estado === 'TODOS' || reporte.estado === filtros.estado)
      && (filtros.intensidad === 'TODAS' || reporte.intensidad === filtros.intensidad)
      && (!filtros.fecha || fechaReporte === filtros.fecha)
  })

  return (
    <div className="admin-layout">
      <section className="card">
        <h2>Administración de usuarios</h2>

        {mensaje && (
          <div className={`alert alert-${mensaje.tipo}`}>
            {mensaje.texto}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="form-group">
              <label htmlFor="nombre">Nombre completo</label>
              <input id="nombre" name="nombre" value={form.nombre} onChange={handleChange} autoComplete="name" />
            </div>

            <div className="form-group">
              <label htmlFor="email">Correo institucional</label>
              <input id="email" name="email" type="email" value={form.email} onChange={handleChange} autoComplete="email" />
            </div>

            <div className="form-group">
              <label htmlFor="password">Contraseña temporal</label>
              <input id="password" name="password" type="password" value={form.password} onChange={handleChange} autoComplete="new-password" />
            </div>

            <div className="form-group">
              <label htmlFor="rol">Rol</label>
              <select id="rol" name="rol" value={form.rol} onChange={handleChange}>
                <option value="BRIGADA">Brigada</option>
                <option value="ADMIN">Administrador</option>
              </select>
            </div>
          </div>

          <div className="admin-actions">
            <button type="submit" className="btn btn-primary" disabled={guardando}>
              {guardando ? 'Creando...' : 'Crear usuario'}
            </button>
            <button type="button" className="btn btn-secondary" onClick={cargarTodo}>
              Actualizar
            </button>
          </div>
        </form>
      </section>

      <section className="stats-row">
        <div className="stat-card">
          <div className="number">{resumen.ADMIN || 0}</div>
          <div className="label">Administradores</div>
        </div>
        <div className="stat-card">
          <div className="number">{resumen.BRIGADA || 0}</div>
          <div className="label">Brigadas</div>
        </div>
        <div className="stat-card">
          <div className="number">{resumen.CIUDADANO || 0}</div>
          <div className="label">Ciudadanos</div>
        </div>
      </section>

      <section className="card">
        <h2>Gestión de reportes</h2>

        <div className="admin-filters">
          <div className="form-group">
            <label htmlFor="filtro-tipo">Tipo</label>
            <select id="filtro-tipo" name="tipo" value={filtros.tipo} onChange={cambiarFiltro}>
              {TIPOS_REPORTE.map((tipo) => <option key={tipo} value={tipo}>{tipo}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="filtro-estado">Estado</label>
            <select id="filtro-estado" name="estado" value={filtros.estado} onChange={cambiarFiltro}>
              <option value="TODOS">TODOS</option>
              {ESTADOS_REPORTE.map((estado) => <option key={estado} value={estado}>{estado}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="filtro-intensidad">Intensidad</label>
            <select id="filtro-intensidad" name="intensidad" value={filtros.intensidad} onChange={cambiarFiltro}>
              {INTENSIDADES.map((intensidad) => <option key={intensidad} value={intensidad}>{intensidad}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="filtro-fecha">Fecha</label>
            <input id="filtro-fecha" name="fecha" type="date" value={filtros.fecha} onChange={cambiarFiltro} />
          </div>
        </div>

        {loading ? (
          <div className="loading">Cargando reportes...</div>
        ) : (
          <div className="reportes-list admin-reportes">
            {reportesFiltrados.length === 0 ? (
              <p className="empty-state">No hay reportes para los filtros seleccionados.</p>
            ) : reportesFiltrados.map((reporte) => (
              <article key={reporte.id} className="reporte-item admin-reporte-item">
                <div className="reporte-info">
                  <h4>#{reporte.id} - {limpiarDescripcion(reporte.descripcion)}</h4>
                  <p>{reporte.ciudadanoNombre || 'Sin reportante'} · {reporte.latitud}, {reporte.longitud}</p>
                  <p>{reporte.fechaCreacion ? new Date(reporte.fechaCreacion).toLocaleString('es-CL') : 'Sin fecha'}</p>
                </div>

                <div className="admin-report-controls">
                  <div className="admin-badges">
                    <span className={`badge badge-${reporte.tipo?.toLowerCase()}`}>{reporte.tipo}</span>
                    <span className={`badge badge-${reporte.estado?.toLowerCase()}`}>{reporte.estado}</span>
                    <span className="badge badge-intensidad">{reporte.intensidad || 'MEDIA'}</span>
                  </div>
                  <div className="admin-inline-actions">
                    <button className="btn btn-secondary" disabled={procesando} onClick={() => actualizarEstado(reporte.id, 'EN_REVISION')}>
                      En revisión
                    </button>
                    <button className="btn btn-secondary" disabled={procesando} onClick={() => actualizarEstado(reporte.id, 'ATENDIDO')}>
                      Atendido
                    </button>
                    <button className="btn btn-secondary" disabled={procesando} onClick={() => actualizarEstado(reporte.id, 'CERRADO')}>
                      Cerrar
                    </button>
                    <button className="btn btn-danger" disabled={procesando} onClick={() => eliminarReporte(reporte.id)}>
                      Eliminar
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="card">
        <h2>Asignación de brigadas</h2>

        {brigadas.length === 0 && (
          <div className="alert alert-warning">Crea al menos una brigada para poder asignar focos.</div>
        )}

        <div className="users-table">
          {focos.length === 0 ? (
            <p className="empty-state">No hay focos activos para asignar.</p>
          ) : focos.map((foco) => (
            <article key={foco.id} className="user-row foco-row">
              <div>
                <strong>Foco #{foco.id} · {foco.sector}</strong>
                <span>{foco.intensidad} · {foco.latitud}, {foco.longitud}</span>
                <span>Asignado: {foco.brigadaAsignada || 'Sin asignar'}</span>
              </div>
              <select
                value={foco.brigadaAsignada || ''}
                onChange={(event) => asignarBrigada(foco, event.target.value)}
                disabled={procesando || brigadas.length === 0}
              >
                <option value="">Sin asignar</option>
                {brigadas.map((brigada) => (
                  <option key={brigada.id} value={brigada.nombre}>{brigada.nombre}</option>
                ))}
              </select>
            </article>
          ))}
        </div>
      </section>

      <section className="card">
        <h2>Usuarios registrados ({usuarios.length})</h2>

        {loading ? (
          <div className="loading">Cargando usuarios...</div>
        ) : usuarios.length === 0 ? (
          <p className="empty-state">No hay usuarios registrados.</p>
        ) : (
          <div className="users-table">
            {usuarios.map((usuario) => (
              <article key={usuario.id} className="user-row">
                <div>
                  <strong>{usuario.nombre}</strong>
                  <span>{usuario.email}</span>
                </div>
                <span className={`badge badge-${usuario.rol?.toLowerCase()}`}>
                  {etiquetaRol[usuario.rol] || usuario.rol}
                </span>
              </article>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}
