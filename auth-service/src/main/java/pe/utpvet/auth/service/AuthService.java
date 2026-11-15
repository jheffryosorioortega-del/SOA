package pe.utpvet.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utpvet.auth.dto.LoginRequest;
import pe.utpvet.auth.dto.LoginResponse;
import pe.utpvet.auth.exception.AccountLockedException;
import pe.utpvet.auth.exception.InvalidCredentialsException;
import pe.utpvet.auth.model.LogAcceso;
import pe.utpvet.auth.model.Sesion;
import pe.utpvet.auth.model.Usuario;
import pe.utpvet.auth.repository.LogAccesoRepository;
import pe.utpvet.auth.repository.SesionRepository;
import pe.utpvet.auth.repository.UsuarioRepository;
import pe.utpvet.auth.security.JwtUtil;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final LogAccesoRepository logAccesoRepository;
    private final SesionRepository sesionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Replica el flujo descrito en la Tabla 1 del documento: valida existencia,
     * valida contraseña, registra el intento en log_accesos (el trigger de la
     * base de datos aplica el bloqueo automático al 3er intento fallido) y,
     * si todo es correcto, emite el token de sesión.
     */
    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByUsername(request.username()).orElse(null);

        if (usuario == null) {
            registrarIntento(null, request.username(), "LOGIN_FALLIDO", request.canal());
            throw new InvalidCredentialsException("Usuario o contraseña incorrectos");
        }

        if ("BLOQUEADO".equals(usuario.getEstado())) {
            throw new AccountLockedException("La cuenta está bloqueada por superar los intentos fallidos permitidos");
        }

        boolean passwordOk = passwordEncoder.matches(request.password(), usuario.getPasswordHash());
        registrarIntento(usuario.getId(), request.username(), passwordOk ? "LOGIN_EXITOSO" : "LOGIN_FALLIDO", request.canal());

        if (!passwordOk) {
            throw new InvalidCredentialsException("Usuario o contraseña incorrectos");
        }

        String token = jwtUtil.generateToken(usuario.getId(), usuario.getUsername(), usuario.getRol().getNombre());

        Sesion sesion = new Sesion();
        sesion.setUsuarioId(usuario.getId());
        sesion.setToken(token);
        sesion.setFechaExpiracion(OffsetDateTime.now().plusHours(1));
        sesion.setActivo(true);
        sesionRepository.save(sesion);

        return new LoginResponse(token, usuario.getId(), usuario.getUsername(), usuario.getRol().getNombre());
    }

    @Transactional
    public void logout(String token) {
        sesionRepository.findByToken(token).ifPresent(sesion -> {
            sesion.setActivo(false);
            sesionRepository.save(sesion);
        });
    }

    private void registrarIntento(Integer usuarioId, String username, String tipoEvento, String canal) {
        LogAcceso log = new LogAcceso();
        log.setUsuarioId(usuarioId);
        log.setUsernameIntento(username);
        log.setTipoEvento(tipoEvento);
        log.setCanal(canal);
        logAccesoRepository.save(log);
    }
}
