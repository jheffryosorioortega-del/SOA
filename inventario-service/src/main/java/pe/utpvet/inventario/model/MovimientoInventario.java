package pe.utpvet.inventario.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "movimientos_inventario")
@Getter
@Setter
public class MovimientoInventario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne
    @JoinColumn(name = "tipo_movimiento_id", nullable = false)
    private TipoMovimiento tipoMovimiento;

    private BigDecimal cantidad;

    @ManyToOne
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    // Referencia lógica a citas.citas(id) - sin FK física (bases separadas)
    @Column(name = "cita_id")
    private Integer citaId;

    // Referencia lógica a autenticacion.usuarios(id) - sin FK física (bases separadas)
    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Column(name = "saldo_resultante", insertable = false, updatable = false)
    private BigDecimal saldoResultante;

    private String observacion;

    @Column(name = "fecha_movimiento", insertable = false, updatable = false)
    private OffsetDateTime fechaMovimiento;
}
