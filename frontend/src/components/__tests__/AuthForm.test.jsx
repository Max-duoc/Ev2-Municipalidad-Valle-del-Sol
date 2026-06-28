import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import AuthForm from '../AuthForm'
import { authService } from '../../services/api'

vi.mock('../../services/api', () => ({
  authService: {
    login: vi.fn(),
    registrar: vi.fn(),
  },
}))

describe('AuthForm', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('inicia sesion y persiste token de autenticacion', async () => {
    const onAuth = vi.fn()
    authService.login.mockResolvedValue({
      data: {
        token: 'token-123',
        usuario: { id: 1, nombre: 'Ana', rol: 'CIUDADANO' },
      },
    })

    render(<AuthForm onAuth={onAuth} />)

    fireEvent.change(screen.getByLabelText(/correo electrónico/i), { target: { value: 'ana@test.cl' } })
    fireEvent.change(screen.getByLabelText(/contraseña/i), { target: { value: 'secreto1' } })
    fireEvent.click(screen.getByRole('button', { name: /entrar/i }))

    await waitFor(() => expect(onAuth).toHaveBeenCalledWith({ id: 1, nombre: 'Ana', rol: 'CIUDADANO' }))
    expect(authService.login).toHaveBeenCalledWith({ email: 'ana@test.cl', password: 'secreto1' })
    expect(localStorage.getItem('authToken')).toBe('token-123')
  })

  it('registra cuenta ciudadana desde el modo registro', async () => {
    const onAuth = vi.fn()
    authService.registrar.mockResolvedValue({
      data: {
        token: 'token-456',
        usuario: { id: 2, nombre: 'Luis', rol: 'CIUDADANO' },
      },
    })

    render(<AuthForm onAuth={onAuth} />)

    fireEvent.click(screen.getByRole('button', { name: /registrarse/i }))
    fireEvent.change(screen.getByLabelText(/nombre completo/i), { target: { value: 'Luis Perez' } })
    fireEvent.change(screen.getByLabelText(/correo electrónico/i), { target: { value: 'luis@test.cl' } })
    fireEvent.change(screen.getByLabelText(/contraseña/i), { target: { value: 'secreto1' } })
    fireEvent.click(screen.getByRole('button', { name: /crear cuenta/i }))

    await waitFor(() => expect(onAuth).toHaveBeenCalled())
    expect(authService.registrar).toHaveBeenCalledWith({
      nombre: 'Luis Perez',
      email: 'luis@test.cl',
      password: 'secreto1',
    })
  })

  it('muestra mensaje de error cuando falla la autenticacion', async () => {
    authService.login.mockRejectedValue({ response: { data: { message: 'Credenciales invalidas' } } })

    render(<AuthForm onAuth={vi.fn()} />)

    fireEvent.change(screen.getByLabelText(/correo electrónico/i), { target: { value: 'ana@test.cl' } })
    fireEvent.change(screen.getByLabelText(/contraseña/i), { target: { value: 'mala' } })
    fireEvent.click(screen.getByRole('button', { name: /entrar/i }))

    expect(await screen.findByText('Credenciales invalidas')).toBeInTheDocument()
  })
})
