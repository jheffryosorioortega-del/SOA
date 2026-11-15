package pe.utpvet.inventario.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "proveedores")
@Getter
@Setter
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "razon_social")
    private String razonSocial;

    private String ruc;
    private String telefono;
    private String correo;
    private String direccion;
    private Boolean activo;
}
