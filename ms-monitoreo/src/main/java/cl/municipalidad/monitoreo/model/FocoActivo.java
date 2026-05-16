package cl.municipalidad.monitoreo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "focos_activos")
public class FocoActivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    @Column(nullable = false)
    private String intensidad; // BAJA, MEDIA, ALTA, CRITICA

    @Column(nullable = false)
    private String estado; // ACTIVO, CONTROLADO, EXTINGUIDO

    @Column(nullable = false)
    private String sector;

    @Column(name = "fecha_deteccion", nullable = false)
    private LocalDateTime fechaDeteccion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @Column(name = "brigada_asignada")
    private String brigadaAsignada;

    public FocoActivo() {}

    public FocoActivo(Double latitud, Double longitud, String intensidad, String sector) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.intensidad = intensidad;
        this.sector = sector;
        this.estado = "ACTIVO";
        this.fechaDeteccion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getIntensidad() { return intensidad; }
    public void setIntensidad(String intensidad) { this.intensidad = intensidad; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public LocalDateTime getFechaDeteccion() { return fechaDeteccion; }
    public void setFechaDeteccion(LocalDateTime fechaDeteccion) { this.fechaDeteccion = fechaDeteccion; }

    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) { this.fechaActualizacion = fechaActualizacion; }

    public String getBrigadaAsignada() { return brigadaAsignada; }
    public void setBrigadaAsignada(String brigadaAsignada) { this.brigadaAsignada = brigadaAsignada; }
}
