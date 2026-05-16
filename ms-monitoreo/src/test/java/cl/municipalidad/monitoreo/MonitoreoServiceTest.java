package cl.municipalidad.monitoreo;

import cl.municipalidad.monitoreo.dto.FocoActivoDTO;
import cl.municipalidad.monitoreo.model.FocoActivo;
import cl.municipalidad.monitoreo.repository.FocoActivoRepository;
import cl.municipalidad.monitoreo.service.MonitoreoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoreoServiceTest {

    @Mock
    private FocoActivoRepository focoActivoRepository;

    @InjectMocks
    private MonitoreoService monitoreoService;

    private FocoActivo focoEjemplo;
    private FocoActivoDTO dtoEjemplo;

    @BeforeEach
    void setUp() {
        dtoEjemplo = new FocoActivoDTO();
        dtoEjemplo.setLatitud(-33.45);
        dtoEjemplo.setLongitud(-70.65);
        dtoEjemplo.setIntensidad("ALTA");
        dtoEjemplo.setSector("Valle Norte");

        focoEjemplo = new FocoActivo(-33.45, -70.65, "ALTA", "Valle Norte");
        focoEjemplo.setId(1L);
    }

    @Test
    void registrarFoco_deberiaGuardarCorrectamente() {
        when(focoActivoRepository.save(any(FocoActivo.class))).thenReturn(focoEjemplo);

        FocoActivo resultado = monitoreoService.registrarFoco(dtoEjemplo);

        assertNotNull(resultado);
        assertEquals("ALTA", resultado.getIntensidad());
        assertEquals("ACTIVO", resultado.getEstado());
        verify(focoActivoRepository, times(1)).save(any(FocoActivo.class));
    }

    @Test
    void obtenerTodos_deberiaRetornarLista() {
        when(focoActivoRepository.findAll()).thenReturn(List.of(focoEjemplo));

        List<FocoActivo> resultado = monitoreoService.obtenerTodos();

        assertEquals(1, resultado.size());
    }

    @Test
    void obtenerFocosActivos_deberiaRetornarSoloActivos() {
        when(focoActivoRepository.findFocosActivos()).thenReturn(List.of(focoEjemplo));

        List<FocoActivo> resultado = monitoreoService.obtenerFocosActivos();

        assertFalse(resultado.isEmpty());
        assertEquals("ACTIVO", resultado.get(0).getEstado());
    }

    @Test
    void actualizarFoco_deberiaActualizarEstado() {
        when(focoActivoRepository.findById(1L)).thenReturn(Optional.of(focoEjemplo));
        when(focoActivoRepository.save(any(FocoActivo.class))).thenReturn(focoEjemplo);

        FocoActivo resultado = monitoreoService.actualizarFoco(1L, Map.of("estado", "CONTROLADO"));

        verify(focoActivoRepository, times(1)).save(any(FocoActivo.class));
        assertNotNull(resultado);
    }

    @Test
    void actualizarFoco_cuandoNoExiste_deberiaLanzarExcepcion() {
        when(focoActivoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> monitoreoService.actualizarFoco(99L, Map.of("estado", "CONTROLADO")));
    }
}
