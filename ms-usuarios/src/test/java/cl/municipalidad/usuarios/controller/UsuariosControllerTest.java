package cl.municipalidad.usuarios.controller;

import cl.municipalidad.usuarios.service.AuthService;
import cl.municipalidad.usuarios.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UsuariosControllerTest {

    private final AuthService authService = mock(AuthService.class);
    private final NotificacionService notificacionService = mock(NotificacionService.class);
    private final AuthController authController = new AuthController(authService);
    private final NotificacionController notificacionController = new NotificacionController(notificacionService);

    @Test
    void authController_deberiaDelegarOperaciones() {
        Map<String, String> body = Map.of("email", "ana@test.cl");
        when(authService.registrar(body)).thenReturn(Map.of("token", "abc"));
        when(authService.login(body)).thenReturn(Map.of("token", "abc"));
        when(authService.obtenerSesion("Bearer abc")).thenReturn(Map.of("usuario", Map.of("id", 1)));
        when(authService.listarUsuarios("Bearer abc")).thenReturn(List.of(Map.of("id", 1)));
        when(authService.crearUsuarioAdministrativo(body, "Bearer abc")).thenReturn(Map.of("id", 2));

        assertEquals(HttpStatus.CREATED, authController.registrar(body).getStatusCode());
        assertEquals(HttpStatus.OK, authController.login(body).getStatusCode());
        assertEquals(HttpStatus.OK, authController.me("Bearer abc").getStatusCode());
        assertEquals(HttpStatus.OK, authController.listarUsuarios("Bearer abc").getStatusCode());
        assertEquals(HttpStatus.CREATED, authController.crearUsuarioAdministrativo(body, "Bearer abc").getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, authController.logout("Bearer abc").getStatusCode());
        assertEquals("ms-usuarios", authController.health().getBody().get("service"));
        verify(authService).cerrarSesion("Bearer abc");
    }

    @Test
    void notificacionController_deberiaDelegarOperaciones() {
        when(notificacionService.obtenerPendientes("Bearer abc")).thenReturn(List.of(Map.of("id", 1)));
        when(notificacionService.enviarATodos(Map.of("titulo", "T", "mensaje", "M"))).thenReturn(Map.of("creadas", 1));

        assertEquals(HttpStatus.OK, notificacionController.pendientes("Bearer abc").getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, notificacionController.marcarLeida(1L, "Bearer abc").getStatusCode());
        assertEquals(HttpStatus.CREATED, notificacionController.broadcast(Map.of("titulo", "T", "mensaje", "M")).getStatusCode());
        verify(notificacionService).marcarLeida(1L, "Bearer abc");
    }
}
