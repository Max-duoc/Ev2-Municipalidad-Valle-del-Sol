import { fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import AdminPanel from '../AdminPanel'
import { adminService, monitoreoService, reportesService } from '../../services/api'

vi.mock('../../services/api', () => ({
  adminService: {
    listarUsuarios: vi.fn(),
    crearUsuario: vi.fn(),
  },
  reportesService: {
    obtenerTodos: vi.fn(),
    actualizarEstado: vi.fn(),
    eliminar: vi.fn(),
  },
  monitoreoService: {
    obtenerFocosActivos: vi.fn(),
    actualizarFoco: vi.fn(),
  },
}))

const usuarios = [
  { id: 1, nombre: 'Admin Uno', email: 'admin@test.cl', rol: 'ADMIN' },
  { id: 2, nombre: 'Brigada Norte', email: 'brigada@test.cl', rol: 'BRIGADA' },
  { id: 3, nombre: 'Ciudadana Sur', email: 'sur@test.cl', rol: 'CIUDADANO' },
]

const reportes = [
  {
    id: 10,
    tipo: 'FORESTAL',
    estado: 'ACTIVO',
    intensidad: 'ALTA',
    descripcion: '[FORESTAL] Humo en quebrada',
    ciudadanoNombre: 'Ciudadana Sur',
    latitud: -33.4,
    longitud: -70.6,
    fechaCreacion: '2026-06-27T12:00:00',
  },
  {
    id: 11,
    tipo: 'URBANO',
    estado: 'PENDIENTE',
    intensidad: 'MEDIA',
    descripcion: '[URBANO] Humo en edificio',
    ciudadanoNombre: 'Vecino',
    latitud: -33.5,
    longitud: -70.7,
    fechaCreacion: '2026-06-26T12:00:00',
  },
]

const focos = [
  { id: 20, sector: 'Norte', intensidad: 'ALTA', latitud: -33.4, longitud: -70.6, brigadaAsignada: '' },
]

describe('AdminPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    adminService.listarUsuarios.mockResolvedValue({ data: usuarios })
    reportesService.obtenerTodos.mockResolvedValue({ data: reportes })
    monitoreoService.obtenerFocosActivos.mockResolvedValue({ data: focos })
    adminService.crearUsuario.mockResolvedValue({ data: { id: 4 } })
    reportesService.actualizarEstado.mockResolvedValue({ data: {} })
    reportesService.eliminar.mockResolvedValue({ data: {} })
    monitoreoService.actualizarFoco.mockResolvedValue({ data: {} })
  })

  it('carga usuarios, reportes, focos y permite acciones administrativas', async () => {
    render(<AdminPanel />)

    expect(await screen.findByText('Admin Uno')).toBeInTheDocument()
    expect(screen.getByText(/Humo en quebrada/)).toBeInTheDocument()
    expect(screen.getByText(/Foco #20/)).toBeInTheDocument()
    expect(screen.getByText('Administradores')).toBeInTheDocument()
    expect(screen.getByText('Brigadas')).toBeInTheDocument()
    expect(screen.getByText('Ciudadanos')).toBeInTheDocument()

    fireEvent.change(screen.getByLabelText(/nombre completo/i), { target: { value: 'Operador' } })
    fireEvent.change(screen.getByLabelText(/correo institucional/i), { target: { value: 'op@test.cl' } })
    fireEvent.change(screen.getByLabelText(/contraseña temporal/i), { target: { value: 'secreto1' } })
    fireEvent.change(screen.getByLabelText('Rol'), { target: { value: 'ADMIN' } })
    fireEvent.click(screen.getByRole('button', { name: /crear usuario/i }))

    await waitFor(() => expect(adminService.crearUsuario).toHaveBeenCalledWith({
      nombre: 'Operador',
      email: 'op@test.cl',
      password: 'secreto1',
      rol: 'ADMIN',
    }))
    expect(await screen.findByText(/administrador creado correctamente/i)).toBeInTheDocument()

    fireEvent.click(screen.getAllByRole('button', { name: /atendido/i })[0])
    await waitFor(() => expect(reportesService.actualizarEstado).toHaveBeenCalledWith(10, 'ATENDIDO'))

    fireEvent.click(screen.getAllByRole('button', { name: /eliminar/i })[0])
    await waitFor(() => expect(reportesService.eliminar).toHaveBeenCalledWith(10))

    fireEvent.change(screen.getByDisplayValue('Sin asignar'), { target: { value: 'Brigada Norte' } })
    await waitFor(() => expect(monitoreoService.actualizarFoco).toHaveBeenCalledWith(20, { brigadaAsignada: 'Brigada Norte' }))
  })

  it('filtra reportes y muestra errores de carga', async () => {
    const { rerender } = render(<AdminPanel />)
    await screen.findByText(/Humo en quebrada/)

    fireEvent.change(screen.getByLabelText('Tipo'), { target: { value: 'URBANO' } })

    expect(screen.queryByText(/Humo en quebrada/)).not.toBeInTheDocument()
    expect(screen.getByText(/Humo en edificio/)).toBeInTheDocument()

    adminService.listarUsuarios.mockRejectedValueOnce({ response: { data: { message: 'Sin permiso' } } })
    rerender(<AdminPanel />)
    fireEvent.click(screen.getByRole('button', { name: /actualizar/i }))

    expect(await screen.findByText('Sin permiso')).toBeInTheDocument()
  })

  it('no elimina si el usuario cancela la confirmacion', async () => {
    window.confirm.mockReturnValueOnce(false)
    render(<AdminPanel />)
    await screen.findByText(/Humo en quebrada/)

    fireEvent.click(screen.getAllByRole('button', { name: /eliminar/i })[0])

    expect(reportesService.eliminar).not.toHaveBeenCalled()
  })
})
