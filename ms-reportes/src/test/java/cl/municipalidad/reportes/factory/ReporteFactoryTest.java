package cl.municipalidad.reportes.factory;

import cl.municipalidad.reportes.dto.ReporteDTO;
import cl.municipalidad.reportes.model.Reporte;
import cl.municipalidad.reportes.model.TipoReporte;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReporteFactoryTest {

    private final ReporteFactory factory = new ReporteFactory();

    @Test
    void crearReporteForestal_deberiaInicializarEstadoActivo() {
        Reporte reporte = factory.crearReporte(dto("FORESTAL", "Incendio ladera norte", "ALTA"));

        assertEquals(TipoReporte.FORESTAL, reporte.getTipo());
        assertEquals("[FORESTAL] Incendio ladera norte", reporte.getDescripcion());
        assertEquals("ACTIVO", reporte.getEstado());
        assertEquals("ALTA", reporte.getIntensidad());
    }

    @Test
    void crearReporteUrbano_deberiaMantenerEstadoPendiente() {
        Reporte reporte = factory.crearReporte(dto("urbano", "Humo en edificio", ""));

        assertEquals(TipoReporte.URBANO, reporte.getTipo());
        assertEquals("[URBANO] Humo en edificio", reporte.getDescripcion());
        assertEquals("PENDIENTE", reporte.getEstado());
        assertEquals("MEDIA", reporte.getIntensidad());
    }

    @Test
    void crearReporteSimulacro_deberiaInicializarEstadoSimulacro() {
        Reporte reporte = factory.crearReporte(dto("SIMULACRO", "Ejercicio municipal", "BAJA"));

        assertEquals(TipoReporte.SIMULACRO, reporte.getTipo());
        assertEquals("SIMULACRO", reporte.getEstado());
    }

    @Test
    void crearReporte_conTipoInvalido_deberiaLanzarExcepcion() {
        ReporteDTO dto = dto("CLIMATICO", "Lluvia intensa", "MEDIA");

        assertThrows(IllegalArgumentException.class, () -> factory.crearReporte(dto));
    }

    private ReporteDTO dto(String tipo, String descripcion, String intensidad) {
        ReporteDTO dto = new ReporteDTO();
        dto.setTipo(tipo);
        dto.setDescripcion(descripcion);
        dto.setLatitud(-33.45);
        dto.setLongitud(-70.65);
        dto.setMediaUrl("/media/prueba.jpg");
        dto.setIntensidad(intensidad);
        dto.setCiudadanoId("7");
        dto.setCiudadanoNombre("Ana Soto");
        return dto;
    }
}
