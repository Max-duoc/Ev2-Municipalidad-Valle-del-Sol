package cl.municipalidad.reportes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@Service
public class MediaStorageService {

    private final Path uploadDir;

    public MediaStorageService(@Value("${app.upload-dir}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Debe enviar un archivo.");
        }

        String contentType = archivo.getContentType() == null ? "" : archivo.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.startsWith("image/") && !contentType.startsWith("video/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo se permiten imágenes o videos.");
        }

        String nombreOriginal = StringUtils.cleanPath(archivo.getOriginalFilename() == null ? "archivo" : archivo.getOriginalFilename());
        String extension = "";
        int punto = nombreOriginal.lastIndexOf('.');
        if (punto >= 0) {
            extension = nombreOriginal.substring(punto).toLowerCase(Locale.ROOT);
        }

        String nombre = UUID.randomUUID() + extension;
        try {
            Files.createDirectories(uploadDir);
            Files.copy(archivo.getInputStream(), uploadDir.resolve(nombre), StandardCopyOption.REPLACE_EXISTING);
            return nombre;
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo guardar el archivo.");
        }
    }

    public Resource cargar(String nombre) {
        try {
            Path archivo = uploadDir.resolve(StringUtils.cleanPath(nombre)).normalize();
            if (!archivo.startsWith(uploadDir)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre de archivo inválido.");
            }
            Resource resource = new UrlResource(archivo.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Archivo no encontrado.");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre de archivo inválido.");
        }
    }
}
