package pe.utpvet.citas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "estados_cita")
@Getter
@Setter
public class EstadoCita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre; // PROGRAMADA, CONFIRMADA, REPROGRAMADA, CANCELADA, ATENDIDA, NO_ASISTIO
}
