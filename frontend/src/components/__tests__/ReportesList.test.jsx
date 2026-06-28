import { render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ReportesList from '../ReportesList'
import { reportesService } from '../../services/api'

vi.mock('../../services/api', () => ({
  reportesService: {
    obtenerTodos: vi.fn(),
  },
}))

describe('ReportesList', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('muestra solo reportes del usuario autenticado', async () => {
    reportesService.obtenerTodos.mockResolvedValue({
      data: [
        {
          id: 1,
          ciudadanoId: '7',
          descripcion: 'Reporte propio',
          latitud: -33.4,
          longitud: -70.6,
          fechaCreacion: '2026-06-27T12:00:00',
          tipo: 'FORESTAL',
          estado: 'ACTIVO',
        },
        {
          id: 2,
          ciudadanoId: '8',
          descripcion: 'Reporte ajeno',
          latitud: -33.5,
          longitud: -70.7,
          fechaCreacion: '2026-06-27T12:00:00',
          tipo: 'URBANO',
          estado: 'PENDIENTE',
        },
      ],
    })

    render(<ReportesList refresh={0} usuario={{ id: 7 }} />)

    expect(screen.getByText(/cargando reportes/i)).toBeInTheDocument()
    await waitFor(() => expect(screen.getByText(/reportes registrados \(1\)/i)).toBeInTheDocument())
    expect(screen.getByText(/reporte propio/i)).toBeInTheDocument()
    expect(screen.queryByText(/reporte ajeno/i)).not.toBeInTheDocument()
  })

  it('muestra estado vacio cuando falla la carga', async () => {
    reportesService.obtenerTodos.mockRejectedValue(new Error('sin servicio'))

    render(<ReportesList refresh={0} usuario={{ id: 7 }} />)

    expect(await screen.findByText(/no tienes reportes registrados/i)).toBeInTheDocument()
  })
})
