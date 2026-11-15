package pe.utpvet.citas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "propietarios")
@Getter
@Setter
public class Propietario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Referencia lógica a autenticacion.usuarios(id) - sin FK física (bases separadas)
    @Column(name = "usuario_id")
    private Integer usuarioId;

    private String nombres;
    private String apellidos;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    private String telefono;
    private String correo;
    private String direccion;

    @Column(name = "fecha_registro", insertable = false, updatable = false)
    private OffsetDateTime fechaRegistro;
}
