package pe.utpvet.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    private String username;

    private String correo;

    @Column(name = "password_hash")
    private String passwordHash;

    private String nombres;

    private String apellidos;

    private String estado;

    @Column(name = "intentos_fallidos")
    private Short intentosFallidos;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

    @Column(name = "ultimo_acceso")
    private OffsetDateTime ultimoAcceso;
}
