package cl.municipalidad.bff;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BffApplicationTest {

    @Test
    void application_deberiaInstanciarse() {
        assertNotNull(new BffApplication());
    }
}
