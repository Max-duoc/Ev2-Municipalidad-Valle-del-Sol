import axios from 'axios'

const BFF_URL = '/bff'

const api = axios.create({
  baseURL: BFF_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 10000,
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// ── Módulo de Autenticación ─────────────────────
export const authService = {
  login: (datos) => api.post('/auth/login', datos),
  registrar: (datos) => api.post('/auth/register', datos),
  obtenerSesion: () => api.get('/auth/me'),
  logout: () => api.post('/auth/logout'),
}

// ── Módulo de Reportes ──────────────────────────
export const reportesService = {
  crear: (datos) => api.post('/reportes', datos),
  obtenerTodos: () => api.get('/reportes'),
  obtenerPorId: (id) => api.get(`/reportes/${id}`),
}

// ── Módulo de Monitoreo ─────────────────────────
export const monitoreoService = {
  obtenerFocosActivos: () => api.get('/monitoreo/focos'),
  registrarFoco: (datos) => api.post('/monitoreo/focos', datos),
  actualizarFoco: (id, datos) => api.patch(`/monitoreo/focos/${id}`, datos),
}

export default api
