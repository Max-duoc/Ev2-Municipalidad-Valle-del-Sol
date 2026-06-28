package cl.municipalidad.reportes.controller;

import cl.municipalidad.reportes.dto.ReporteDTO;
import cl.municipalidad.reportes.model.Reporte;
import cl.municipalidad.reportes.model.TipoReporte;
import cl.municipalidad.reportes.service.MediaStorageService;
import cl.municipalidad.reportes.service.ReporteService;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReporteControllerTest {

    private final ReporteService reporteService = mock(ReporteService.class);
    private final MediaStorageService mediaStorageService = mock(MediaStorageService.class);
    private final ReporteController controller = new ReporteController(reporteService, mediaStorageService);

    @Test
    void crear_deberiaResponderCreated() {
        Reporte reporte = reporte();
        ReporteDTO dto = new ReporteDTO();
        when(reporteService.crearReporte(dto)).thenReturn(reporte);

        ResponseEntity<Reporte> response = controller.crear(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(reporte, response.getBody());
    }

    @Test
    void obtenerPorId_cuandoNoExiste_deberiaResponderNotFound() {
        when(reporteService.obtenerPorId(8L)).thenReturn(Optional.empty());

        assertEquals(HttpStatus.NOT_FOUND, controller.obtenerPorId(8L).getStatusCode());
    }

    @Test
    void actualizarEstado_deberiaDelegarAlServicio() {
        Reporte reporte = reporte();
        when(reporteService.actualizarEstado(1L, "ATENDIDO")).thenReturn(reporte);

        ResponseEntity<Reporte> response = controller.actualizarEstado(1L, Map.of("estado", "ATENDIDO"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reporte, response.getBody());
    }

    @Test
    void subirMedia_deberiaResponderUrlDelBff() {
        MockMultipartFile archivo = new MockMultipartFile("archivo", "foto.jpg", "image/jpeg", "x".getBytes());
        when(mediaStorageService.guardar(archivo)).thenReturn("abc.jpg");

        ResponseEntity<Map<String, String>> response = controller.subirMedia(archivo);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("/bff/reportes/media/abc.jpg", response.getBody().get("mediaUrl"));
    }

    @Test
    void obtenerMedia_deberiaUsarContentDispositionInline() {
        ByteArrayResource resource = new ByteArrayResource("x".getBytes()) {
            @Override
            public String getFilename() {
                return "abc.jpg";
            }
        };
        when(mediaStorageService.cargar("abc.jpg")).thenReturn(resource);

        ResponseEntity<?> response = controller.obtenerMedia("abc.jpg");

        assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("abc.jpg"));
        assertEquals(resource, response.getBody());
    }

    @Test
    void eliminar_deberiaResponderNoContent() {
        assertEquals(HttpStatus.NO_CONTENT, controller.eliminar(3L).getStatusCode());

        verify(reporteService).eliminar(3L);
    }

    @Test
    void filtrosYHealth_deberianResponderOk() {
        when(reporteService.obtenerTodos()).thenReturn(List.of(reporte()));
        when(reporteService.obtenerPorTipo("FORESTAL")).thenReturn(List.of(reporte()));
        when(reporteService.obtenerPorEstado("ACTIVO")).thenReturn(List.of(reporte()));

        assertEquals(1, controller.obtenerTodos().getBody().size());
        assertEquals(1, controller.obtenerPorTipo("FORESTAL").getBody().size());
        assertEquals(1, controller.obtenerPorEstado("ACTIVO").getBody().size());
        assertEquals("ms-reportes", controller.health().getBody().get("service"));
    }

    private Reporte reporte() {
        Reporte reporte = new Reporte(TipoReporte.FORESTAL, "[FORESTAL] Prueba", -33.4, -70.6, null, "ALTA", "1", "Ana");
        reporte.setId(1L);
        return reporte;
    }
}
