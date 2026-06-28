package cl.municipalidad.usuarios.service;

import cl.municipalidad.usuarios.model.Usuario;
import cl.municipalidad.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Map<String, Object> registrar(Map<String, String> body) {
        String nombre = textoRequerido(body.get("nombre"), "El nombre es obligatorio.");
        String email = normalizarEmail(body.get("email"));
        String password = textoRequerido(body.get("password"), "La contraseña es obligatoria.");

        if (password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 6 caracteres.");
        }

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario registrado con ese correo.");
        }

        Usuario usuario = new Usuario(nombre, email, passwordEncoder.encode(password), "CIUDADANO");
        usuario.setTokenSesion(generarToken());
        return respuestaAuth(usuarioRepository.save(usuario));
    }

    public List<Map<String, Object>> listarUsuarios(String authorizationHeader) {
        exigirAdmin(authorizationHeader);
        return usuarioRepository.findAllByOrderByCreadoEnDesc()
                .stream()
                .map(this::usuarioPublico)
                .toList();
    }

    public Map<String, Object> crearUsuarioAdministrativo(Map<String, String> body, String authorizationHeader) {
        exigirAdmin(authorizationHeader);

        String nombre = textoRequerido(body.get("nombre"), "El nombre es obligatorio.");
        String email = normalizarEmail(body.get("email"));
        String password = textoRequerido(body.get("password"), "La contraseña es obligatoria.");
        String rol = textoRequerido(body.get("rol"), "El rol es obligatorio.").toUpperCase();

        if (!rol.equals("ADMIN") && !rol.equals("BRIGADA")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se pueden crear usuarios ADMIN o BRIGADA desde este panel.");
        }

        if (password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 6 caracteres.");
        }

        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario registrado con ese correo.");
        }

        Usuario usuario = new Usuario(nombre, email, passwordEncoder.encode(password), rol);
        return usuarioPublico(usuarioRepository.save(usuario));
    }

    public Map<String, Object> login(Map<String, String> body) {
        String email = normalizarEmail(body.get("email"));
        String password = textoRequerido(body.get("password"), "La contraseña es obligatoria.");

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos."));

        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos.");
        }

        usuario.setTokenSesion(generarToken());
        return respuestaAuth(usuarioRepository.save(usuario));
    }

    public Map<String, Object> obtenerSesion(String authorizationHeader) {
        String token = extraerToken(authorizationHeader);
        Usuario usuario = usuarioRepository.findByTokenSesion(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida o expirada."));
        return Map.of("usuario", usuarioPublico(usuario));
    }

    public void cerrarSesion(String authorizationHeader) {
        String token = extraerToken(authorizationHeader);
        usuarioRepository.findByTokenSesion(token).ifPresent(usuario -> {
            usuario.setTokenSesion(null);
            usuarioRepository.save(usuario);
        });
    }

    private Map<String, Object> respuestaAuth(Usuario usuario) {
        return Map.of(
                "token", usuario.getTokenSesion(),
                "usuario", usuarioPublico(usuario));
    }

    private Map<String, Object> usuarioPublico(Usuario usuario) {
        return Map.of(
                "id", usuario.getId(),
                "nombre", usuario.getNombre(),
                "email", usuario.getEmail(),
                "rol", usuario.getRol(),
                "creadoEn", usuario.getCreadoEn());
    }

    private Usuario exigirAdmin(String authorizationHeader) {
        String token = extraerToken(authorizationHeader);
        Usuario usuario = usuarioRepository.findByTokenSesion(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida o expirada."));
        if (!"ADMIN".equals(usuario.getRol())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo un administrador puede realizar esta acción.");
        }
        return usuario;
    }

    private String extraerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debe enviar un token Bearer válido.");
        }
        return authorizationHeader.substring("Bearer ".length()).trim();
    }

    private String normalizarEmail(String email) {
        String normalizado = textoRequerido(email, "El correo es obligatorio.").toLowerCase();
        if (!normalizado.contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingrese un correo válido.");
        }
        return normalizado;
    }

    private String textoRequerido(String valor, String mensaje) {
        String texto = valor == null ? "" : valor.trim();
        if (texto.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
        }
        return texto;
    }

    private String textoOpcional(String valor, String defecto) {
        String texto = valor == null ? "" : valor.trim();
        return texto.isEmpty() ? defecto : texto;
    }

    private String generarToken() {
        return UUID.randomUUID().toString();
    }
}
