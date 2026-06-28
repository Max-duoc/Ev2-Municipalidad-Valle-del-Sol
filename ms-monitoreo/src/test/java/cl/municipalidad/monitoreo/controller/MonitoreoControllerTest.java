package cl.municipalidad.monitoreo.controller;

import cl.municipalidad.monitoreo.dto.FocoActivoDTO;
import cl.municipalidad.monitoreo.model.FocoActivo;
import cl.municipalidad.monitoreo.service.MonitoreoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MonitoreoControllerTest {

    private final MonitoreoService service = mock(MonitoreoService.class);
    private final MonitoreoController controller = new MonitoreoController(service);

    @Test
    void registrarFoco_deberiaResponderCreated() {
        FocoActivoDTO dto = new FocoActivoDTO();
        FocoActivo foco = foco();
        when(service.registrarFoco(dto)).thenReturn(foco);

        ResponseEntity<FocoActivo> response = controller.registrarFoco(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(foco, response.getBody());
    }

    @Test
    void obtenerPorId_cuandoExiste_deberiaResponderOk() {
        when(service.obtenerPorId(1L)).thenReturn(Optional.of(foco()));

        assertEquals(HttpStatus.OK, controller.obtenerPorId(1L).getStatusCode());
    }

    @Test
    void obtenerPorId_cuandoNoExiste_deberiaResponderNotFound() {
        when(service.obtenerPorId(99L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, controller.obtenerPorId(99L).getStatusCode());
    }

    @Test
    void actualizarYEliminar_deberianDelegarAlServicio() {
        when(service.actualizarFoco(1L, Map.of("estado", "CONTROLADO"))).thenReturn(foco());

        assertEquals(HttpStatus.OK, controller.actualizar(1L, Map.of("estado", "CONTROLADO")).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.eliminar(1L).getStatusCode());
        verify(service).eliminar(1L);
    }

    @Test
    void listadosYHealth_deberianResponderOk() {
        when(service.obtenerTodos()).thenReturn(List.of(foco()));
        when(service.obtenerFocosActivos()).thenReturn(List.of(foco()));
        when(service.obtenerPorSector("Norte")).thenReturn(List.of(foco()));

        assertEquals(1, controller.obtenerTodos().getBody().size());
        assertEquals(1, controller.obtenerActivos().getBody().size());
        assertEquals(1, controller.obtenerPorSector("Norte").getBody().size());
        assertEquals("ms-monitoreo", controller.health().getBody().get("service"));
    }

    private FocoActivo foco() {
        FocoActivo foco = new FocoActivo(-33.45, -70.65, "ALTA", "Norte");
        foco.setId(1L);
        return foco;
    }
}
