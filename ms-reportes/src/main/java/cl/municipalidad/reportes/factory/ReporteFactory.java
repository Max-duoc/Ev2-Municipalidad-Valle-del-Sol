package cl.municipalidad.reportes.factory;

import cl.municipalidad.reportes.dto.ReporteDTO;
import cl.municipalidad.reportes.model.Reporte;
import cl.municipalidad.reportes.model.TipoReporte;
import org.springframework.stereotype.Component;

/**
 * Factory Method Pattern: Centraliza la creación de los distintos tipos de reportes.
 * Permite añadir nuevos tipos (climáticos, sísmicos, etc.) de forma extensible
 * sin modificar la lógica de negocio existente.
 */
@Component
public class ReporteFactory {

    public Reporte crearReporte(ReporteDTO dto) {
        TipoReporte tipo = TipoReporte.valueOf(dto.getTipo().toUpperCase());

        return switch (tipo) {
            case FORESTAL -> crearReporteForestal(dto);
            case URBANO   -> crearReporteUrbano(dto);
            case SIMULACRO -> crearReporteSimulacro(dto);
        };
    }

    private Reporte crearReporteForestal(ReporteDTO dto) {
        Reporte r = new Reporte(
                TipoReporte.FORESTAL,
                "[FORESTAL] " + dto.getDescripcion(),
                dto.getLatitud(),
                dto.getLongitud(),
                dto.getMediaUrl(),
                dto.getCiudadanoId()
        );
        r.setEstado("ACTIVO");
        return r;
    }

    private Reporte crearReporteUrbano(ReporteDTO dto) {
        return new Reporte(
                TipoReporte.URBANO,
                "[URBANO] " + dto.getDescripcion(),
                dto.getLatitud(),
                dto.getLongitud(),
                dto.getMediaUrl(),
                dto.getCiudadanoId()
        );
    }

    private Reporte crearReporteSimulacro(ReporteDTO dto) {
        Reporte r = new Reporte(
                TipoReporte.SIMULACRO,
                "[SIMULACRO] " + dto.getDescripcion(),
                dto.getLatitud(),
                dto.getLongitud(),
                dto.getMediaUrl(),
                dto.getCiudadanoId()
        );
        r.setEstado("SIMULACRO");
        return r;
    }
}
