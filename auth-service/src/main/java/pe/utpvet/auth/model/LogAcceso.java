package pe.utpvet.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "log_accesos")
@Getter
@Setter
public class LogAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Integer usuarioId;

    @Column(name = "username_intento")
    private String usernameIntento;

    @Column(name = "tipo_evento")
    private String tipoEvento; // LOGIN_EXITOSO, LOGIN_FALLIDO, BLOQUEO, LOGOUT

    private String canal;

    @Column(name = "fecha_hora", insertable = false, updatable = false)
    private OffsetDateTime fechaHora;
}
