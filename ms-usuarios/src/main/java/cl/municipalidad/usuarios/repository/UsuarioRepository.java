package cl.municipalidad.usuarios.repository;

import cl.municipalidad.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    Optional<Usuario> findByTokenSesion(String tokenSesion);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByRol(String rol);

    List<Usuario> findAllByOrderByCreadoEnDesc();
}
