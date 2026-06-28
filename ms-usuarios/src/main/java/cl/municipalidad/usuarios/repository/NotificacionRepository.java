package cl.municipalidad.usuarios.repository;

import cl.municipalidad.usuarios.model.Notificacion;
import cl.municipalidad.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    List<Notificacion> findByUsuarioAndLeidaFalseOrderByCreadaEnAsc(Usuario usuario);
}
