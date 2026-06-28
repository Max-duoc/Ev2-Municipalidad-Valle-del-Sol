package cl.municipalidad.usuarios.service;

import cl.municipalidad.usuarios.model.Notificacion;
import cl.municipalidad.usuarios.model.Usuario;
import cl.municipalidad.usuarios.repository.NotificacionRepository;
import cl.municipalidad.usuarios.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacionService(NotificacionRepository notificacionRepository, UsuarioRepository usuarioRepository) {
        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Map<String, Object>> obtenerPendientes(String authorizationHeader) {
        Usuario usuario = obtenerUsuarioSesion(authorizationHeader);
        return notificacionRepository.findByUsuarioAndLeidaFalseOrderByCreadaEnAsc(usuario)
                .stream()
                .map(this::notificacionPublica)
                .toList();
    }

    public void marcarLeida(Long id, String authorizationHeader) {
        Usuario usuario = obtenerUsuarioSesion(authorizationHeader);
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificación no encontrada."));

        if (!notificacion.getUsuario().getId().equals(usuario.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No puede modificar esta notificación.");
        }

        notificacion.marcarLeida();
        notificacionRepository.save(notificacion);
    }

    public Map<String, Object> enviarATodos(Map<String, String> body) {
        String titulo = textoRequerido(body.get("titulo"), "El título es obligatorio.");
        String mensaje = textoRequerido(body.get("mensaje"), "El mensaje es obligatorio.");
        String url = textoOpcional(body.get("url"), "/");

        List<Notificacion> notificaciones = usuarioRepository.findAll()
                .stream()
                .map(usuario -> new Notificacion(usuario, titulo, mensaje, url))
                .toList();

        notificacionRepository.saveAll(notificaciones);
        return Map.of("creadas", notificaciones.size());
    }

    private Map<String, Object> notificacionPublica(Notificacion notificacion) {
        return Map.of(
                "id", notificacion.getId(),
                "titulo", notificacion.getTitulo(),
                "mensaje", notificacion.getMensaje(),
                "url", notificacion.getUrl() == null ? "/" : notificacion.getUrl(),
                "creadaEn", notificacion.getCreadaEn());
    }

    private Usuario obtenerUsuarioSesion(String authorizationHeader) {
        String token = extraerToken(authorizationHeader);
        return usuarioRepository.findByTokenSesion(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida o expirada."));
    }

    private String extraerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Debe enviar un token Bearer válido.");
        }
        return authorizationHeader.substring("Bearer ".length()).trim();
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
}
