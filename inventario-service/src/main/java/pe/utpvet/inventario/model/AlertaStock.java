package pe.utpvet.inventario.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "alertas_stock")
@Getter
@Setter
public class AlertaStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "stock_al_momento")
    private BigDecimal stockAlMomento;

    private String estado; // PENDIENTE, ATENDIDA

    @Column(name = "fecha_alerta", insertable = false, updatable = false)
    private OffsetDateTime fechaAlerta;

    @Column(name = "fecha_atencion")
    private OffsetDateTime fechaAtencion;
}
