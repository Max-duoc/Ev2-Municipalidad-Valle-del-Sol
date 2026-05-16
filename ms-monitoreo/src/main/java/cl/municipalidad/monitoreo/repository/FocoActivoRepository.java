package cl.municipalidad.monitoreo.repository;

import cl.municipalidad.monitoreo.model.FocoActivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository Pattern: Abstrae el acceso a datos espaciales.
 * En producción esta capa se conectaría a PostGIS sin cambiar la lógica de negocio.
 */
@Repository
public interface FocoActivoRepository extends JpaRepository<FocoActivo, Long> {
    List<FocoActivo> findByEstado(String estado);
    List<FocoActivo> findByIntensidad(String intensidad);
    List<FocoActivo> findBySector(String sector);

    @Query("SELECT f FROM FocoActivo f WHERE f.estado = 'ACTIVO' ORDER BY f.fechaDeteccion DESC")
    List<FocoActivo> findFocosActivos();
}
