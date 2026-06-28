package cl.municipalidad.monitoreo.service;

import cl.municipalidad.monitoreo.dto.FocoActivoDTO;
import cl.municipalidad.monitoreo.model.FocoActivo;
import cl.municipalidad.monitoreo.repository.FocoActivoRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MonitoreoServiceComponentTest {

    private final FocoActivoRepository repository = mock(FocoActivoRepository.class);
    private final MonitoreoService service = new MonitoreoService(repository);

    @Test
    void registrarFoco_conBrigada_deberiaAsignarla() {
        FocoActivoDTO dto = new FocoActivoDTO();
        dto.setLatitud(-33.45);
        dto.setLongitud(-70.65);
        dto.setIntensidad("CRITICA");
        dto.setSector("Sur");
        dto.setBrigadaAsignada("Brigada 4");
        when(repository.save(any(FocoActivo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FocoActivo foco = service.registrarFoco(dto);

        assertEquals("Brigada 4", foco.getBrigadaAsignada());
        assertEquals("ACTIVO", foco.getEstado());
    }

    @Test
    void actualizarFoco_deberiaActualizarCamposParcialesYFecha() {
        FocoActivo foco = new FocoActivo(-33.45, -70.65, "MEDIA", "Centro");
        when(repository.findById(1L)).thenReturn(Optional.of(foco));
        when(repository.save(any(FocoActivo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FocoActivo actualizado = service.actualizarFoco(1L, Map.of(
                "estado", "CONTROLADO",
                "intensidad", "BAJA",
                "brigadaAsignada", "Brigada 2"));

        assertEquals("CONTROLADO", actualizado.getEstado());
        assertEquals("BAJA", actualizado.getIntensidad());
        assertEquals("Brigada 2", actualizado.getBrigadaAsignada());
        assertNotNull(actualizado.getFechaActualizacion());
    }
}
