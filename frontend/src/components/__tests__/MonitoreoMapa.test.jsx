import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import MonitoreoMapa from '../MonitoreoMapa'
import { monitoreoService, reportesService } from '../../services/api'

vi.mock('leaflet', () => ({
  default: {
    Icon: {
      Default: {
        prototype: {},
        mergeOptions: vi.fn(),
      },
    },
    divIcon: vi.fn((options) => options),
  },
}))

vi.mock('react-leaflet', () => ({
  MapContainer: ({ children }) => <div data-testid="map">{children}</div>,
  TileLayer: () => <div data-testid="tile" />,
  Circle: ({ center }) => <div data-testid="circle">{center.join(',')}</div>,
  Marker: ({ children, eventHandlers }) => (
    <button type="button" onClick={eventHandlers?.click} data-testid="marker">
      marcador
      {children}
    </button>
  ),
  Popup: ({ children }) => <div>{children}</div>,
}))

vi.mock('../../services/api', () => ({
  monitoreoService: {
    obtenerFocosActivos: vi.fn(),
  },
  reportesService: {
    obtenerTodos: vi.fn(),
  },
}))

describe('MonitoreoMapa', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    vi.spyOn(window, 'open').mockImplementation(() => null)
    monitoreoService.obtenerFocosActivos.mockResolvedValue({
      data: [{ id: 1, latitud: -33.45, longitud: -70.65, intensidad: 'CRITICA' }],
    })
    reportesService.obtenerTodos.mockResolvedValue({
      data: [
        {
          id: 100,
          ciudadanoNombre: 'Ana',
          descripcion: '[FORESTAL] Humo cercano',
          latitud: -33.451,
          longitud: -70.651,
          intensidad: 'ALTA',
          mediaUrl: '/media/foto.jpg',
        },
        {
          id: 101,
          ciudadanoId: '7',
          descripcion: '[FORESTAL] Video del foco',
          latitud: -33.452,
          longitud: -70.652,
          intensidad: 'CRITICA',
          mediaUrl: '/media/video.mp4',
        },
      ],
    })
  })

  it('muestra focos, agrupa reportes y abre el detalle', async () => {
    render(<MonitoreoMapa nuevosReportes={0} />)

    expect(await screen.findByText(/Avisos: 2/i)).toBeInTheDocument()
    expect(screen.getByText(/Focos activos: 1/i)).toBeInTheDocument()
    expect(screen.getByText(/1 crítico/i)).toBeInTheDocument()

    fireEvent.click(screen.getByTestId('marker'))

    expect(screen.getAllByText(/Ana \+1/i).length).toBeGreaterThan(0)
    expect(screen.getByText('Humo cercano')).toBeInTheDocument()
    expect(screen.getByAltText(/evidencia del aviso/i)).toHaveAttribute('src', '/media/foto.jpg')
    expect(screen.getByText('Video del foco')).toBeInTheDocument()
  })

  it('muestra alerta cuando el circuit breaker responde abierto', async () => {
    monitoreoService.obtenerFocosActivos.mockResolvedValueOnce({ data: { status: 'CIRCUIT_OPEN' } })
    reportesService.obtenerTodos.mockResolvedValueOnce({ data: [] })

    render(<MonitoreoMapa nuevosReportes={0} />)

    expect(await screen.findByText(/circuit breaker activo/i)).toBeInTheDocument()
    expect(screen.getByText(/Focos activos: 0/i)).toBeInTheDocument()
  })

  it('permite actualizar manualmente y maneja error de servicio', async () => {
    render(<MonitoreoMapa nuevosReportes={0} />)
    await screen.findByText(/Avisos: 2/i)

    monitoreoService.obtenerFocosActivos.mockRejectedValueOnce(new Error('offline'))
    fireEvent.click(screen.getByRole('button', { name: /actualizar/i }))

    await waitFor(() => expect(screen.getByText(/no se pudo conectar/i)).toBeInTheDocument())
  })
})
