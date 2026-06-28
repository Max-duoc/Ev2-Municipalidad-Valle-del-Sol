package cl.municipalidad.usuarios.service;

import cl.municipalidad.usuarios.model.Notificacion;
import cl.municipalidad.usuarios.model.Usuario;
import cl.municipalidad.usuarios.repository.NotificacionRepository;
import cl.municipalidad.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificacionServiceTest {

    private final NotificacionRepository notificacionRepository = mock(NotificacionRepository.class);
    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final NotificacionService service = new NotificacionService(notificacionRepository, usuarioRepository);

    @Test
    void obtenerPendientes_deberiaRetornarNotificacionesPublicas() {
        Usuario usuario = usuario(1L, "Ana", "token");
        Notificacion notificacion = notificacion(usuario, 10L, "/admin");
        when(usuarioRepository.findByTokenSesion("token")).thenReturn(Optional.of(usuario));
        when(notificacionRepository.findByUsuarioAndLeidaFalseOrderByCreadaEnAsc(usuario)).thenReturn(List.of(notificacion));

        List<Map<String, Object>> pendientes = service.obtenerPendientes("Bearer token");

        assertEquals(1, pendientes.size());
        assertEquals("Alerta", pendientes.get(0).get("titulo"));
        assertEquals("/admin", pendientes.get(0).get("url"));
    }

    @Test
    void marcarLeida_deberiaGuardarSiPerteneceAlUsuario() {
        Usuario usuario = usuario(1L, "Ana", "token");
        Notificacion notificacion = notificacion(usuario, 10L, null);
        when(usuarioRepository.findByTokenSesion("token")).thenReturn(Optional.of(usuario));
        when(notificacionRepository.findById(10L)).thenReturn(Optional.of(notificacion));

        service.marcarLeida(10L, "Bearer token");

        assertTrue(notificacion.isLeida());
        verify(notificacionRepository).save(notificacion);
    }

    @Test
    void marcarLeida_conOtroUsuario_deberiaResponderForbidden() {
        Usuario usuario = usuario(1L, "Ana", "token");
        Notificacion notificacion = notificacion(usuario(2L, "Luis", null), 10L, null);
        when(usuarioRepository.findByTokenSesion("token")).thenReturn(Optional.of(usuario));
        when(notificacionRepository.findById(10L)).thenReturn(Optional.of(notificacion));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.marcarLeida(10L, "Bearer token"));

        assertEquals(403, ex.getStatusCode().value());
    }

    @Test
    void enviarATodos_deberiaCrearUnaNotificacionPorUsuario() {
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario(1L, "Ana", null), usuario(2L, "Luis", null)));

        Map<String, Object> response = service.enviarATodos(Map.of("titulo", "Alerta", "mensaje", "Mensaje"));

        assertEquals(2, response.get("creadas"));
        verify(notificacionRepository).saveAll(anyList());
    }

    @Test
    void enviarATodos_sinTitulo_deberiaResponderBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.enviarATodos(Map.of("mensaje", "Mensaje")));

        assertEquals(400, ex.getStatusCode().value());
    }

    private Usuario usuario(Long id, String nombre, String token) {
        Usuario usuario = new Usuario(nombre, nombre.toLowerCase() + "@test.cl", "hash", "CIUDADANO");
        ReflectionTestUtils.setField(usuario, "id", id);
        usuario.setTokenSesion(token);
        return usuario;
    }

    private Notificacion notificacion(Usuario usuario, Long id, String url) {
        Notificacion notificacion = new Notificacion(usuario, "Alerta", "Mensaje", url);
        ReflectionTestUtils.setField(notificacion, "id", id);
        return notificacion;
    }
}
