package pe.utpvet.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.utpvet.auth.dto.LoginRequest;
import pe.utpvet.auth.dto.LoginResponse;
import pe.utpvet.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Inicio y cierre de sesión")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Iniciar sesión",
            description = "Valida usuario y contraseña. Devuelve el token JWT con el rol. Responde 401 si las credenciales son incorrectas y 423 si la cuenta está bloqueada (3 intentos fallidos).")
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Cerrar sesión", description = "Marca como inactiva la sesión del token enviado.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String token = authorizationHeader.replaceFirst("(?i)^Bearer ", "");
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("auth-service OK");
    }
}
