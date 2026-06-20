package cl.municipalidad.usuarios;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class UsuariosApplicationTest {

    @Test
    void application_deberiaInstanciarse() {
        assertNotNull(new UsuariosApplication());
    }
}
