import { useState } from 'react'
import ReporteForm from './components/ReporteForm'
import ReportesList from './components/ReportesList'
import MonitoreoMapa from './components/MonitoreoMapa'

export default function App() {
  const [activeTab, setActiveTab] = useState('reportar')
  const [refreshReportes, setRefreshReportes] = useState(0)

  const handleNuevoReporte = () => {
    setRefreshReportes((n) => n + 1)
  }

  return (
    <div className="app">
      <nav className="navbar">
        <div>
          <h1>🔥 Municipalidad Valle del Sol</h1>
          <div className="subtitle">Sistema de Gestión de Emergencias</div>
        </div>
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
        </div>
      </nav>

      <main className="main-content">
        {activeTab === 'reportar' && (
          <>
            <ReporteForm onReporteCreado={handleNuevoReporte} />
            <ReportesList refresh={refreshReportes} />
          </>
        )}

        {activeTab === 'monitoreo' && (
          <MonitoreoMapa nuevosReportes={refreshReportes} />
        )}

        {activeTab === 'historial' && (
          <ReportesList refresh={refreshReportes} />
        )}
      </main>
    </div>
  )
}
