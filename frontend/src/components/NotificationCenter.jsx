import { useEffect, useRef, useState } from 'react'
import { notificacionesService } from '../services/api'

const INTERVALO_NOTIFICACIONES_MS = 15000

export default function NotificationCenter() {
  const [permiso, setPermiso] = useState(() => (
    'Notification' in window ? Notification.permission : 'unsupported'
  ))
  const [pendientes, setPendientes] = useState(0)
  const procesadas = useRef(new Set())

  const puedeNotificar = permiso === 'granted'

  const activarNotificaciones = async () => {
    if (!('Notification' in window)) {
      setPermiso('unsupported')
      return
    }

    const nuevoPermiso = await Notification.requestPermission()
    setPermiso(nuevoPermiso)
  }

  useEffect(() => {
    if (!puedeNotificar) return undefined

    let cancelado = false

    const cargarNotificaciones = async () => {
      try {
        const response = await notificacionesService.obtenerPendientes()
        const notificaciones = Array.isArray(response.data) ? response.data : []
        if (cancelado) return

        setPendientes(notificaciones.length)

        for (const notificacion of notificaciones) {
          if (procesadas.current.has(notificacion.id)) continue
          procesadas.current.add(notificacion.id)

          new Notification(notificacion.titulo, {
            body: notificacion.mensaje,
            tag: `municipalidad-${notificacion.id}`,
          })

          await notificacionesService.marcarLeida(notificacion.id)
        }
      } catch (error) {
        if (error.response?.status === 401) return
        console.warn('No se pudieron cargar notificaciones.', error)
      }
    }

    cargarNotificaciones()
    const intervalo = window.setInterval(cargarNotificaciones, INTERVALO_NOTIFICACIONES_MS)

    return () => {
      cancelado = true
      window.clearInterval(intervalo)
    }
  }, [puedeNotificar])

  if (permiso === 'unsupported' || permiso === 'denied') {
    return null
  }

  return (
    <button
      type="button"
      className={`notification-button ${puedeNotificar ? 'enabled' : ''}`}
      onClick={activarNotificaciones}
      title={puedeNotificar ? 'Notificaciones activas' : 'Activar notificaciones del navegador'}
    >
      <span aria-hidden="true">🔔</span>
      <span>{puedeNotificar ? 'Activas' : 'Activar'}</span>
      {pendientes > 0 && <strong>{pendientes}</strong>}
    </button>
  )
}
