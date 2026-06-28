package cl.municipalidad.usuarios.service;

import cl.municipalidad.usuarios.model.Usuario;
import cl.municipalidad.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UsuarioRepository repository = mock(UsuarioRepository.class);
    private final AuthService service = new AuthService(repository);

    @Test
    void registrar_conDatosValidos_deberiaCrearCiudadanoConToken() {
        when(repository.existsByEmailIgnoreCase("ana@test.cl")).thenReturn(false);
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            ReflectionTestUtils.setField(usuario, "id", 1L);
            return usuario;
        });

        Map<String, Object> response = service.registrar(Map.of(
                "nombre", "Ana Soto",
                "email", "ANA@Test.cl",
                "password", "secreto1"));

        assertNotNull(response.get("token"));
        Map<?, ?> usuario = (Map<?, ?>) response.get("usuario");
        assertEquals("ana@test.cl", usuario.get("email"));
        assertEquals("CIUDADANO", usuario.get("rol"));
    }

    @Test
    void registrar_conPasswordCorta_deberiaResponderBadRequest() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.registrar(Map.of(
                "nombre", "Ana",
                "email", "ana@test.cl",
                "password", "123")));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void registrar_conEmailDuplicado_deberiaResponderConflict() {
        when(repository.existsByEmailIgnoreCase("ana@test.cl")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.registrar(Map.of(
                "nombre", "Ana",
                "email", "ana@test.cl",
                "password", "secreto1")));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void login_conCredencialesValidas_deberiaRenovarToken() {
        Usuario usuario = new Usuario("Ana", "ana@test.cl", new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("secreto1"), "ADMIN");
        ReflectionTestUtils.setField(usuario, "id", 1L);
        when(repository.findByEmailIgnoreCase("ana@test.cl")).thenReturn(Optional.of(usuario));
        when(repository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Map<String, Object> response = service.login(Map.of("email", "ana@test.cl", "password", "secreto1"));

        assertNotNull(response.get("token"));
        verify(repository).save(usuario);
    }

    @Test
    void login_conPasswordIncorrecta_deberiaResponderUnauthorized() {
        Usuario usuario = new Usuario("Ana", "ana@test.cl", new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("secreto1"), "ADMIN");
        when(repository.findByEmailIgnoreCase("ana@test.cl")).thenReturn(Optional.of(usuario));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.login(Map.of("email", "ana@test.cl", "password", "mala")));

        assertEquals(401, ex.getStatusCode().value());
    }

    @Test
    void listarUsuarios_conAdmin_deberiaRetornarUsuariosPublicos() {
        Usuario admin = usuario(1L, "Admin", "admin@test.cl", "ADMIN", "token-admin");
        Usuario brigada = usuario(2L, "Brigada", "brigada@test.cl", "BRIGADA", null);
        when(repository.findByTokenSesion("token-admin")).thenReturn(Optional.of(admin));
        when(repository.findAllByOrderByCreadoEnDesc()).thenReturn(List.of(brigada, admin));

        List<Map<String, Object>> usuarios = service.listarUsuarios("Bearer token-admin");

        assertEquals(2, usuarios.size());
        assertEquals("BRIGADA", usuarios.get(0).get("rol"));
    }

    @Test
    void crearUsuarioAdministrativo_conRolInvalido_deberiaResponderBadRequest() {
        Usuario admin = usuario(1L, "Admin", "admin@test.cl", "ADMIN", "token-admin");
        when(repository.findByTokenSesion("token-admin")).thenReturn(Optional.of(admin));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.crearUsuarioAdministrativo(
                Map.of("nombre", "Operador", "email", "op@test.cl", "password", "secreto1", "rol", "CIUDADANO"),
                "Bearer token-admin"));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void obtenerSesion_conTokenValido_deberiaRetornarUsuario() {
        Usuario usuario = usuario(1L, "Ana", "ana@test.cl", "CIUDADANO", "token");
        when(repository.findByTokenSesion("token")).thenReturn(Optional.of(usuario));

        Map<String, Object> response = service.obtenerSesion("Bearer token");

        assertNotNull(response.get("usuario"));
    }

    @Test
    void cerrarSesion_deberiaLimpiarTokenSiExiste() {
        Usuario usuario = usuario(1L, "Ana", "ana@test.cl", "CIUDADANO", "token");
        when(repository.findByTokenSesion("token")).thenReturn(Optional.of(usuario));

        service.cerrarSesion("Bearer token");

        assertNull(usuario.getTokenSesion());
        verify(repository).save(usuario);
    }

    @Test
    void obtenerSesion_sinBearer_deberiaResponderUnauthorized() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.obtenerSesion("token"));

        assertEquals(401, ex.getStatusCode().value());
    }

    private Usuario usuario(Long id, String nombre, String email, String rol, String token) {
        Usuario usuario = new Usuario(nombre, email, "hash", rol);
        ReflectionTestUtils.setField(usuario, "id", id);
        usuario.setTokenSesion(token);
        return usuario;
    }
}
