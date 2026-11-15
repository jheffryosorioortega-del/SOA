package pe.utpvet.citas.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "veterinarios")
@Getter
@Setter
public class Veterinario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Referencia lógica a autenticacion.usuarios(id) - sin FK física (bases separadas)
    @Column(name = "usuario_id")
    private Integer usuarioId;

    private String nombres;
    private String apellidos;
    private String colegiatura;
    private String especialidad;
    private Boolean activo;
}
