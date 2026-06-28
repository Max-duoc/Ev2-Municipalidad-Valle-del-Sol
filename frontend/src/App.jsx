import { useEffect, useState } from 'react'
import AuthForm from './components/AuthForm'
import ReporteForm from './components/ReporteForm'
import ReportesList from './components/ReportesList'
import MonitoreoMapa from './components/MonitoreoMapa'
import AdminPanel from './components/AdminPanel'
import NotificationCenter from './components/NotificationCenter'
import { authService } from './services/api'

export default function App() {
  const [activeTab, setActiveTab] = useState('monitoreo')
  const [refreshReportes, setRefreshReportes] = useState(0)
  const [usuario, setUsuario] = useState(() => {
    const guardado = localStorage.getItem('authUser')
    return guardado ? JSON.parse(guardado) : null
  })
  const [verificandoSesion, setVerificandoSesion] = useState(Boolean(localStorage.getItem('authToken')))

  useEffect(() => {
    const verificarSesion = async () => {
      const token = localStorage.getItem('authToken')
      if (!token) {
        setVerificandoSesion(false)
        return
      }

      try {
        const response = await authService.obtenerSesion()
        setUsuario(response.data.usuario)
        localStorage.setItem('authUser', JSON.stringify(response.data.usuario))
      } catch {
        localStorage.removeItem('authToken')
        localStorage.removeItem('authUser')
        setUsuario(null)
      } finally {
        setVerificandoSesion(false)
      }
    }

    verificarSesion()
  }, [])

  const handleNuevoReporte = () => {
    setRefreshReportes((n) => n + 1)
  }

  const handleAuth = (usuarioAutenticado) => {
    setUsuario(usuarioAutenticado)
    setActiveTab(usuarioAutenticado.rol === 'ADMIN' ? 'admin' : 'monitoreo')
  }

  const handleLogout = async () => {
    try {
      await authService.logout()
    } catch {
      // El cierre local igual se completa si el backend no responde.
    }
    localStorage.removeItem('authToken')
    localStorage.removeItem('authUser')
    setUsuario(null)
  }

  if (verificandoSesion) {
    return <div className="loading">Verificando sesión...</div>
  }

  if (!usuario) {
    return <AuthForm onAuth={handleAuth} />
  }

  return (
    <div className="app">
      <nav className="navbar">
        <div className="nav-brand">
          <div>
            <h1>🔥 Municipalidad Valle del Sol</h1>
            <div className="subtitle">Sistema de Gestión de Emergencias</div>
          </div>
          <div className="session-user">
            <strong>{usuario.nombre}</strong>
            <span>{usuario.rol}</span>
          </div>
        </div>
        <div className="nav-actions">
          <NotificationCenter />
          <div className="nav-tabs">
            <button
              className={`nav-tab ${activeTab === 'reportar' ? 'active' : ''}`}
              onClick={() => setActiveTab('reportar')}
            >
              🚨 Reportar
            </button>
            <button
              className={`nav-tab ${activeTab === 'monitoreo' ? 'active' : ''}`}
              onClick={() => setActiveTab('monitoreo')}
            >
              🗺️ Monitoreo
            </button>
            <button
              className={`nav-tab ${activeTab === 'historial' ? 'active' : ''}`}
              onClick={() => setActiveTab('historial')}
            >
              📋 Historial
            </button>
            {usuario.rol === 'ADMIN' && (
              <button
                className={`nav-tab ${activeTab === 'admin' ? 'active' : ''}`}
                onClick={() => setActiveTab('admin')}
              >
                Usuarios
              </button>
            )}
            <button className="nav-tab" onClick={handleLogout}>
              Salir
            </button>
          </div>
        </div>
      </nav>

      <main className={`main-content ${activeTab === 'monitoreo' ? 'monitoring-content' : ''}`}>
        {activeTab === 'reportar' && (
          <>
            <ReporteForm usuario={usuario} onReporteCreado={handleNuevoReporte} />
            <ReportesList refresh={refreshReportes} usuario={usuario} />
          </>
        )}

        {activeTab === 'monitoreo' && (
          <MonitoreoMapa nuevosReportes={refreshReportes} />
        )}

        {activeTab === 'historial' && (
          <ReportesList refresh={refreshReportes} usuario={usuario} />
        )}

        {activeTab === 'admin' && usuario.rol === 'ADMIN' && (
          <AdminPanel />
        )}
      </main>
    </div>
  )
}
