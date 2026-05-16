package cl.municipalidad.monitoreo.service;

import cl.municipalidad.monitoreo.dto.FocoActivoDTO;
import cl.municipalidad.monitoreo.model.FocoActivo;
import cl.municipalidad.monitoreo.repository.FocoActivoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MonitoreoService {

    private final FocoActivoRepository focoActivoRepository;

    public MonitoreoService(FocoActivoRepository focoActivoRepository) {
        this.focoActivoRepository = focoActivoRepository;
    }

    public FocoActivo registrarFoco(FocoActivoDTO dto) {
        FocoActivo foco = new FocoActivo(
                dto.getLatitud(),
                dto.getLongitud(),
                dto.getIntensidad(),
                dto.getSector()
        );
        if (dto.getBrigadaAsignada() != null) {
            foco.setBrigadaAsignada(dto.getBrigadaAsignada());
        }
        return focoActivoRepository.save(foco);
    }

    public List<FocoActivo> obtenerTodos() {
        return focoActivoRepository.findAll();
    }

    public List<FocoActivo> obtenerFocosActivos() {
        return focoActivoRepository.findFocosActivos();
    }

    public Optional<FocoActivo> obtenerPorId(Long id) {
        return focoActivoRepository.findById(id);
    }

    public List<FocoActivo> obtenerPorSector(String sector) {
        return focoActivoRepository.findBySector(sector);
    }

    public FocoActivo actualizarFoco(Long id, Map<String, String> cambios) {
        FocoActivo foco = focoActivoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Foco no encontrado: " + id));

        if (cambios.containsKey("estado")) foco.setEstado(cambios.get("estado"));
        if (cambios.containsKey("intensidad")) foco.setIntensidad(cambios.get("intensidad"));
        if (cambios.containsKey("brigadaAsignada")) foco.setBrigadaAsignada(cambios.get("brigadaAsignada"));
        foco.setFechaActualizacion(LocalDateTime.now());

        return focoActivoRepository.save(foco);
    }

    public void eliminar(Long id) {
        focoActivoRepository.deleteById(id);
    }
}
