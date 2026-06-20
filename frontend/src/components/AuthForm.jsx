import { useState } from 'react'
import { authService } from '../services/api'

const initialForm = {
  nombre: '',
  email: '',
  password: '',
  rol: 'CIUDADANO',
}

export default function AuthForm({ onAuth }) {
  const [modo, setModo] = useState('login')
  const [form, setForm] = useState(initialForm)
  const [loading, setLoading] = useState(false)
  const [mensaje, setMensaje] = useState(null)

  const handleChange = (event) => {
    setForm({ ...form, [event.target.name]: event.target.value })
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setLoading(true)
    setMensaje(null)

    try {
      const response = modo === 'login'
        ? await authService.login({ email: form.email, password: form.password })
        : await authService.registrar(form)

      localStorage.setItem('authToken', response.data.token)
      localStorage.setItem('authUser', JSON.stringify(response.data.usuario))
      onAuth(response.data.usuario)
    } catch (error) {
      const texto = error.response?.data?.message || 'No se pudo completar la autenticación.'
      setMensaje({ tipo: 'error', texto })
    } finally {
      setLoading(false)
    }
  }

  const cambiarModo = (nuevoModo) => {
    setModo(nuevoModo)
    setMensaje(null)
  }

  return (
    <div className="auth-page">
      <section className="auth-shell">
        <div className="auth-hero">
          <div className="auth-kicker">Central municipal</div>
          <h1>Municipalidad Valle del Sol</h1>
          <p>
            Plataforma para coordinar reportes, monitorear focos activos y mantener
            a los equipos municipales alineados durante una emergencia.
          </p>

          <div className="auth-status-card" aria-label="Resumen operativo">
            <div>
              <span className="auth-status-dot" />
              Monitoreo operativo
            </div>
            <strong>Mapa de focos en tiempo real</strong>
          </div>

          <div className="auth-metrics">
            <div>
              <strong>24/7</strong>
              <span>Seguimiento</span>
            </div>
            <div>
              <strong>3</strong>
              <span>Perfiles</span>
            </div>
            <div>
              <strong>1</strong>
              <span>Centro de control</span>
            </div>
          </div>
        </div>

        <div className="auth-panel">
          <div className="auth-brand">
            <span className="auth-brand-mark">VS</span>
            <div>
              <h2>{modo === 'login' ? 'Iniciar sesión' : 'Crear cuenta'}</h2>
              <p>Accede al sistema de gestión de emergencias.</p>
            </div>
          </div>

          <div className="auth-card">
            <div className="auth-tabs">
              <button
                type="button"
                className={`auth-tab ${modo === 'login' ? 'active' : ''}`}
                onClick={() => cambiarModo('login')}
              >
                Iniciar sesión
              </button>
              <button
                type="button"
                className={`auth-tab ${modo === 'registro' ? 'active' : ''}`}
                onClick={() => cambiarModo('registro')}
              >
                Registrarse
              </button>
            </div>

            {mensaje && (
              <div className={`alert alert-${mensaje.tipo}`}>
                {mensaje.texto}
              </div>
            )}

            <form onSubmit={handleSubmit} className="auth-form">
              {modo === 'registro' && (
                <>
                  <div className="form-group">
                    <label htmlFor="nombre">Nombre completo</label>
                    <input
                      id="nombre"
                      name="nombre"
                      value={form.nombre}
                      onChange={handleChange}
                      autoComplete="name"
                    />
                  </div>

                  <div className="form-group">
                    <label htmlFor="rol">Tipo de perfil</label>
                    <select id="rol" name="rol" value={form.rol} onChange={handleChange}>
                      <option value="CIUDADANO">Ciudadano</option>
                      <option value="OPERADOR">Operador municipal</option>
                      <option value="BRIGADA">Brigada</option>
                    </select>
                  </div>
                </>
              )}

              <div className="form-group">
                <label htmlFor="email">Correo electrónico</label>
                <input
                  id="email"
                  name="email"
                  type="email"
                  value={form.email}
                  onChange={handleChange}
                  autoComplete="email"
                />
              </div>

              <div className="form-group">
                <label htmlFor="password">Contraseña</label>
                <input
                  id="password"
                  name="password"
                  type="password"
                  value={form.password}
                  onChange={handleChange}
                  autoComplete={modo === 'login' ? 'current-password' : 'new-password'}
                />
              </div>

              <button type="submit" className="btn btn-primary auth-submit" disabled={loading}>
                {loading ? 'Procesando...' : modo === 'login' ? 'Entrar' : 'Crear cuenta'}
              </button>
            </form>
          </div>
        </div>
      </section>
    </div>
  )
}
