import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ReporteForm from '../ReporteForm'
import { reportesService } from '../../services/api'

vi.mock('../../services/api', () => ({
  reportesService: {
    crear: vi.fn(),
    subirMedia: vi.fn(),
  },
}))

describe('ReporteForm', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    Object.defineProperty(window, 'isSecureContext', { value: true, configurable: true })
    Object.defineProperty(navigator, 'geolocation', {
      value: {
        watchPosition: vi.fn((success) => {
          success({ coords: { latitude: -33.456789, longitude: -70.654321, accuracy: 25 } })
          return 11
        }),
        clearWatch: vi.fn(),
      },
      configurable: true,
    })
  })

  it('valida descripcion y ubicacion antes de enviar', async () => {
    render(<ReporteForm usuario={{ id: 1, nombre: 'Ana' }} />)

    fireEvent.click(screen.getByRole('button', { name: /enviar reporte/i }))

    expect(await screen.findByText(/ingresa una descripción/i)).toBeInTheDocument()
    expect(reportesService.crear).not.toHaveBeenCalled()
  })

  it('obtiene ubicacion y crea reporte asociado al usuario', async () => {
    const onReporteCreado = vi.fn()
    reportesService.crear.mockResolvedValue({ data: { id: 50 } })

    render(<ReporteForm usuario={{ id: 7, nombre: 'Ana Soto' }} onReporteCreado={onReporteCreado} />)

    fireEvent.click(screen.getByRole('button', { name: /usar mi ubicación/i }))
    expect(await screen.findByText(/ubicación registrada correctamente/i)).toBeInTheDocument()

    fireEvent.change(screen.getByLabelText(/descripción del incidente/i), {
      target: { value: 'Humo visible en quebrada' },
    })
    fireEvent.click(screen.getByRole('button', { name: /enviar reporte/i }))

    await waitFor(() => expect(reportesService.crear).toHaveBeenCalled())
    expect(reportesService.crear).toHaveBeenCalledWith(expect.objectContaining({
      ciudadanoId: '7',
      ciudadanoNombre: 'Ana Soto',
      descripcion: 'Humo visible en quebrada',
      latitud: -33.456789,
      longitud: -70.654321,
    }))
    expect(onReporteCreado).toHaveBeenCalledWith({ id: 50 })
    expect(await screen.findByText(/reporte #50 creado exitosamente/i)).toBeInTheDocument()
  })

  it('sube archivo antes de crear el reporte', async () => {
    reportesService.subirMedia.mockResolvedValue({ data: { mediaUrl: '/media/foto.jpg' } })
    reportesService.crear.mockResolvedValue({ data: { id: 51 } })

    render(<ReporteForm usuario={{ id: 7, nombre: 'Ana Soto' }} />)

    fireEvent.click(screen.getByRole('button', { name: /usar mi ubicación/i }))
    await screen.findByText(/ubicación registrada correctamente/i)
    fireEvent.change(screen.getByLabelText(/descripción del incidente/i), {
      target: { value: 'Foco con evidencia' },
    })
    fireEvent.change(screen.getByLabelText(/subir imagen o video/i), {
      target: { files: [new File(['foto'], 'foto.jpg', { type: 'image/jpeg' })] },
    })
    fireEvent.click(screen.getByRole('button', { name: /enviar reporte/i }))

    await waitFor(() => expect(reportesService.subirMedia).toHaveBeenCalled())
    expect(reportesService.crear).toHaveBeenCalledWith(expect.objectContaining({ mediaUrl: '/media/foto.jpg' }))
  })
})
