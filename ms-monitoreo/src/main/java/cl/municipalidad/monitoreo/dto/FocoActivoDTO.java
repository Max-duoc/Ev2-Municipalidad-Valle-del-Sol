package cl.municipalidad.monitoreo.dto;

public class FocoActivoDTO {
    private Double latitud;
    private Double longitud;
    private String intensidad;
    private String sector;
    private String brigadaAsignada;

    public FocoActivoDTO() {}

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getIntensidad() { return intensidad; }
    public void setIntensidad(String intensidad) { this.intensidad = intensidad; }

    public String getSector() { return sector; }
    public void setSector(String sector) { this.sector = sector; }

    public String getBrigadaAsignada() { return brigadaAsignada; }
    public void setBrigadaAsignada(String brigadaAsignada) { this.brigadaAsignada = brigadaAsignada; }
}
