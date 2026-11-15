package pe.utpvet.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "sesiones")
@Getter
@Setter
public class Sesion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    private String token;

    @Column(name = "fecha_emision", insertable = false, updatable = false)
    private OffsetDateTime fechaEmision;

    @Column(name = "fecha_expiracion")
    private OffsetDateTime fechaExpiracion;

    private Boolean activo;
}
