import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import NotificationCenter from '../NotificationCenter'
import { notificacionesService } from '../../services/api'

vi.mock('../../services/api', () => ({
  notificacionesService: {
    obtenerPendientes: vi.fn(),
    marcarLeida: vi.fn(),
  },
}))

describe('NotificationCenter', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.useRealTimers()
  })

  it('pide permiso y procesa notificaciones pendientes', async () => {
    const notificationMock = vi.fn()
    notificationMock.permission = 'default'
    notificationMock.requestPermission = vi.fn().mockResolvedValue('granted')
    vi.stubGlobal('Notification', notificationMock)
    notificacionesService.obtenerPendientes.mockResolvedValue({
      data: [{ id: 1, titulo: 'Alerta', mensaje: 'Nuevo reporte' }],
    })
    notificacionesService.marcarLeida.mockResolvedValue({})

    render(<NotificationCenter />)

    fireEvent.click(screen.getByRole('button', { name: /activar/i }))

    await waitFor(() => expect(notificationMock.requestPermission).toHaveBeenCalled())
    await waitFor(() => expect(notificacionesService.marcarLeida).toHaveBeenCalledWith(1))
    expect(notificationMock).toHaveBeenCalledWith('Alerta', expect.objectContaining({ body: 'Nuevo reporte' }))
    expect(screen.getByText('Activas')).toBeInTheDocument()
    expect(screen.getByText('1')).toBeInTheDocument()
  })

  it('no renderiza cuando las notificaciones no estan soportadas o fueron denegadas', () => {
    Reflect.deleteProperty(window, 'Notification')
    Reflect.deleteProperty(globalThis, 'Notification')
    const { container, rerender } = render(<NotificationCenter />)
    expect(container).toBeEmptyDOMElement()

    const notificationMock = vi.fn()
    notificationMock.permission = 'denied'
    notificationMock.requestPermission = vi.fn()
    vi.stubGlobal('Notification', notificationMock)
    rerender(<NotificationCenter />)

    expect(container).toBeEmptyDOMElement()
  })
})
