package cl.municipalidad.usuarios.config;

import cl.municipalidad.usuarios.model.Usuario;
import cl.municipalidad.usuarios.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminSeeder(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.existsByRol("ADMIN")) {
            return;
        }

        Usuario admin = new Usuario(
                "Administrador General",
                "admin@valledelsol.cl",
                passwordEncoder.encode("admin123"),
                "ADMIN"
        );
        usuarioRepository.save(admin);
        System.out.println("Admin inicial creado: admin@valledelsol.cl / admin123");
    }
}
