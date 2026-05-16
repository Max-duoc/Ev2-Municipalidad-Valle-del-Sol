package cl.municipalidad.reportes.repository;

import cl.municipalidad.reportes.model.Reporte;
import cl.municipalidad.reportes.model.TipoReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Pattern: Desacopla la lógica de negocio del acceso a datos.
 * Si la municipalidad migra de H2/MySQL a PostGIS, solo se modifica esta capa.
 */
@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    List<Reporte> findByTipo(TipoReporte tipo);
    List<Reporte> findByEstado(String estado);
    List<Reporte> findByCiudadanoId(String ciudadanoId);
}
