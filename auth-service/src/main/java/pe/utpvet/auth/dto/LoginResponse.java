package pe.utpvet.auth.dto;

public record LoginResponse(
        String token,
        Integer usuarioId,
        String username,
        String rol
) {
}
