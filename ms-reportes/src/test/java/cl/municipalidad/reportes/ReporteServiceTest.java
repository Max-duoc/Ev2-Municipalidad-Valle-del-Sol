package cl.municipalidad.reportes;

import cl.municipalidad.reportes.dto.ReporteDTO;
import cl.municipalidad.reportes.factory.ReporteFactory;
import cl.municipalidad.reportes.model.Reporte;
import cl.municipalidad.reportes.model.TipoReporte;
import cl.municipalidad.reportes.repository.ReporteRepository;
import cl.municipalidad.reportes.service.ReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReporteServiceTest {

    @Mock
    private ReporteRepository reporteRepository;

    @Mock
    private ReporteFactory reporteFactory;

    @InjectMocks
    private ReporteService reporteService;

    private Reporte reporteEjemplo;
    private ReporteDTO dtoEjemplo;

    @BeforeEach
    void setUp() {
        dtoEjemplo = new ReporteDTO();
        dtoEjemplo.setTipo("FORESTAL");
        dtoEjemplo.setDescripcion("Incendio sector norte");
        dtoEjemplo.setLatitud(-33.45);
        dtoEjemplo.setLongitud(-70.65);
        dtoEjemplo.setCiudadanoId("ciudadano-001");

        reporteEjemplo = new Reporte(
                TipoReporte.FORESTAL,
                "[FORESTAL] Incendio sector norte",
                -33.45, -70.65,
                null, "ciudadano-001"
        );
        reporteEjemplo.setId(1L);
    }

    @Test
    void crearReporte_deberiaRetornarReporteGuardado() {
        when(reporteFactory.crearReporte(any(ReporteDTO.class))).thenReturn(reporteEjemplo);
        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteEjemplo);

        Reporte resultado = reporteService.crearReporte(dtoEjemplo);

        assertNotNull(resultado);
        assertEquals(TipoReporte.FORESTAL, resultado.getTipo());
        verify(reporteRepository, times(1)).save(any(Reporte.class));
    }

    @Test
    void obtenerTodos_deberiaRetornarListaDeReportes() {
        when(reporteRepository.findAll()).thenReturn(List.of(reporteEjemplo));

        List<Reporte> resultado = reporteService.obtenerTodos();

        assertFalse(resultado.isEmpty());
        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerPorId_cuandoExiste_deberiaRetornarReporte() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporteEjemplo));

        Optional<Reporte> resultado = reporteService.obtenerPorId(1L);

        assertTrue(resultado.isPresent());
        assertEquals(1L, resultado.get().getId());
    }

    @Test
    void obtenerPorId_cuandoNoExiste_deberiaRetornarVacio() {
        when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Reporte> resultado = reporteService.obtenerPorId(99L);

        assertFalse(resultado.isPresent());
    }

    @Test
    void actualizarEstado_deberiaActualizarCorrectamente() {
        when(reporteRepository.findById(1L)).thenReturn(Optional.of(reporteEjemplo));
        when(reporteRepository.save(any(Reporte.class))).thenReturn(reporteEjemplo);

        Reporte resultado = reporteService.actualizarEstado(1L, "ATENDIDO");

        verify(reporteRepository, times(1)).save(any(Reporte.class));
        assertNotNull(resultado);
    }

    @Test
    void actualizarEstado_cuandoNoExiste_deberiaLanzarExcepcion() {
        when(reporteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reporteService.actualizarEstado(99L, "ATENDIDO"));
    }
}
