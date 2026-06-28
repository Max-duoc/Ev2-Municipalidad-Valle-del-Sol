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

export const adminService = {
  listarUsuarios: () => api.get('/auth/usuarios'),
  crearUsuario: (datos) => api.post('/auth/usuarios', datos),
}

export const notificacionesService = {
  obtenerPendientes: () => api.get('/auth/notificaciones'),
  marcarLeida: (id) => api.patch(`/auth/notificaciones/${id}/leida`),
}

// ── Módulo de Reportes ──────────────────────────
export const reportesService = {
  crear: (datos) => api.post('/reportes', datos),
  subirMedia: (archivo) => {
    const formData = new FormData()
    formData.append('archivo', archivo)
    return api.post('/reportes/media', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 30000,
    })
  },
  obtenerTodos: () => api.get('/reportes'),
  obtenerPorId: (id) => api.get(`/reportes/${id}`),
  actualizarEstado: (id, estado) => api.patch(`/reportes/${id}/estado`, { estado }),
  eliminar: (id) => api.delete(`/reportes/${id}`),
}

// ── Módulo de Monitoreo ─────────────────────────
export const monitoreoService = {
  obtenerFocosActivos: () => api.get('/monitoreo/focos'),
  registrarFoco: (datos) => api.post('/monitoreo/focos', datos),
  actualizarFoco: (id, datos) => api.patch(`/monitoreo/focos/${id}`, datos),
}

export default api
