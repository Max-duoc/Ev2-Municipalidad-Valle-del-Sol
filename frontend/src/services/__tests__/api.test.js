import { beforeEach, describe, expect, it, vi } from 'vitest'

const post = vi.fn()
const get = vi.fn()
const patch = vi.fn()
const deleteRequest = vi.fn()
let requestInterceptor

vi.mock('axios', () => ({
  default: {
    create: vi.fn(() => ({
      post,
      get,
      patch,
      delete: deleteRequest,
      interceptors: {
        request: {
          use: vi.fn((callback) => {
            requestInterceptor = callback
          }),
        },
      },
    })),
  },
}))

describe('api services', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('agrega token bearer al interceptor cuando existe sesion', async () => {
    await import('../api')
    localStorage.setItem('authToken', 'abc')

    const config = requestInterceptor({ headers: {} })

    expect(config.headers.Authorization).toBe('Bearer abc')
  })

  it('expone operaciones de autenticacion y administracion', async () => {
    const { authService, adminService, notificacionesService } = await import('../api')

    authService.login({ email: 'a@test.cl' })
    authService.registrar({ email: 'a@test.cl' })
    authService.obtenerSesion()
    authService.logout()
    adminService.listarUsuarios()
    adminService.crearUsuario({ rol: 'ADMIN' })
    notificacionesService.obtenerPendientes()
    notificacionesService.marcarLeida(3)

    expect(post).toHaveBeenCalledWith('/auth/login', { email: 'a@test.cl' })
    expect(post).toHaveBeenCalledWith('/auth/register', { email: 'a@test.cl' })
    expect(get).toHaveBeenCalledWith('/auth/me')
    expect(post).toHaveBeenCalledWith('/auth/logout')
    expect(get).toHaveBeenCalledWith('/auth/usuarios')
    expect(post).toHaveBeenCalledWith('/auth/usuarios', { rol: 'ADMIN' })
    expect(get).toHaveBeenCalledWith('/auth/notificaciones')
    expect(patch).toHaveBeenCalledWith('/auth/notificaciones/3/leida')
  })

  it('expone operaciones de reportes y monitoreo', async () => {
    const { reportesService, monitoreoService } = await import('../api')
    const archivo = new File(['x'], 'foto.jpg', { type: 'image/jpeg' })

    reportesService.crear({ tipo: 'FORESTAL' })
    reportesService.subirMedia(archivo)
    reportesService.obtenerTodos()
    reportesService.obtenerPorId(9)
    reportesService.actualizarEstado(9, 'ATENDIDO')
    reportesService.eliminar(9)
    monitoreoService.obtenerFocosActivos()
    monitoreoService.registrarFoco({ sector: 'Norte' })
    monitoreoService.actualizarFoco(4, { estado: 'CONTROLADO' })

    expect(post).toHaveBeenCalledWith('/reportes', { tipo: 'FORESTAL' })
    expect(post).toHaveBeenCalledWith('/reportes/media', expect.any(FormData), expect.objectContaining({ timeout: 30000 }))
    expect(get).toHaveBeenCalledWith('/reportes')
    expect(get).toHaveBeenCalledWith('/reportes/9')
    expect(patch).toHaveBeenCalledWith('/reportes/9/estado', { estado: 'ATENDIDO' })
    expect(deleteRequest).toHaveBeenCalledWith('/reportes/9')
    expect(get).toHaveBeenCalledWith('/monitoreo/focos')
    expect(post).toHaveBeenCalledWith('/monitoreo/focos', { sector: 'Norte' })
    expect(patch).toHaveBeenCalledWith('/monitoreo/focos/4', { estado: 'CONTROLADO' })
  })
})
