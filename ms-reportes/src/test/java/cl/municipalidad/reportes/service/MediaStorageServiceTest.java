package cl.municipalidad.reportes.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MediaStorageServiceTest {

    @TempDir
    Path uploadDir;

    @Test
    void guardar_conImagenValida_deberiaPersistirArchivoYCargarlo() throws Exception {
        MediaStorageService service = new MediaStorageService(uploadDir.toString());
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "evidencia.JPG",
                "image/jpeg",
                "contenido".getBytes());

        String nombre = service.guardar(archivo);
        Resource resource = service.cargar(nombre);

        assertTrue(nombre.endsWith(".jpg"));
        assertTrue(resource.exists());
        assertEquals(nombre, resource.getFilename());
    }

    @Test
    void guardar_conArchivoVacio_deberiaResponderBadRequest() {
        MediaStorageService service = new MediaStorageService(uploadDir.toString());
        MockMultipartFile archivo = new MockMultipartFile("archivo", "foto.jpg", "image/jpeg", new byte[0]);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.guardar(archivo));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void guardar_conTipoNoPermitido_deberiaResponderBadRequest() {
        MediaStorageService service = new MediaStorageService(uploadDir.toString());
        MockMultipartFile archivo = new MockMultipartFile("archivo", "nota.txt", "text/plain", "x".getBytes());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.guardar(archivo));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    void cargar_conRutaFueraDelDirectorio_deberiaResponderBadRequest() {
        MediaStorageService service = new MediaStorageService(uploadDir.toString());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.cargar("../secreto.jpg"));

        assertEquals(400, ex.getStatusCode().value());
    }
}
