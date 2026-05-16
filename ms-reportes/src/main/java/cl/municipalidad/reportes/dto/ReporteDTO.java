package cl.municipalidad.reportes.dto;

public class ReporteDTO {
    private String tipo;
    private String descripcion;
    private Double latitud;
    private Double longitud;
    private String mediaUrl;
    private String ciudadanoId;

    public ReporteDTO() {}

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }

    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }

    public String getMediaUrl() { return mediaUrl; }
    public void setMediaUrl(String mediaUrl) { this.mediaUrl = mediaUrl; }

    public String getCiudadanoId() { return ciudadanoId; }
    public void setCiudadanoId(String ciudadanoId) { this.ciudadanoId = ciudadanoId; }
}
