package pe.utpvet.inventario.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tipos_movimiento")
@Getter
@Setter
public class TipoMovimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre; // ENTRADA_COMPRA, SALIDA_CONSUMO, AJUSTE, DEVOLUCION
    private Short signo;
}
