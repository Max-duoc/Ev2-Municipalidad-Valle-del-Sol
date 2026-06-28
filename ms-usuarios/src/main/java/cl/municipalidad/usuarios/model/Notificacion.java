package cl.municipalidad.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column
    private String url;

    @Column(nullable = false)
    private boolean leida;

    @Column(nullable = false)
    private LocalDateTime creadaEn;

    protected Notificacion() {
    }

    public Notificacion(Usuario usuario, String titulo, String mensaje, String url) {
        this.usuario = usuario;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.url = url;
        this.leida = false;
        this.creadaEn = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public String getUrl() {
        return url;
    }

    public boolean isLeida() {
        return leida;
    }

    public LocalDateTime getCreadaEn() {
        return creadaEn;
    }

    public void marcarLeida() {
        this.leida = true;
    }
}
