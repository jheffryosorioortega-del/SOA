package pe.utpvet.citas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "historial_estados_cita")
@Getter
@Setter
public class HistorialEstadoCita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cita_id")
    private Integer citaId;

    @Column(name = "estado_id")
    private Integer estadoId;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    private String observacion;

    @Column(name = "fecha_cambio", insertable = false, updatable = false)
    private OffsetDateTime fechaCambio;
}
