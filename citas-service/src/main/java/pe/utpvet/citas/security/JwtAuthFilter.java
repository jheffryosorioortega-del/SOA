package pe.utpvet.citas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Valida el JWT emitido por el Servicio de Autenticación de forma totalmente
 * local (verificación de firma con la clave compartida), sin llamar a la
 * base de datos de Autenticación. Así, aunque el servicio de Autenticación
 * esté caído, las peticiones ya autenticadas de Citas siguen funcionando.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (uri.endsWith("/health") || uri.startsWith("/actuator")
                || uri.startsWith("/v3/api-docs") || uri.startsWith("/swagger-ui")) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Falta el token de autenticación");
            return;
        }

        try {
            String token = header.substring(7);
            Claims claims = Jwts.parser().verifyWith(key()).build()
                    .parseSignedClaims(token).getPayload();
            request.setAttribute("usuarioId", claims.get("usuarioId", Integer.class));
            request.setAttribute("rol", claims.get("rol", String.class));
            request.setAttribute("username", claims.getSubject());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado");
            return;
        }

        chain.doFilter(request, response);
    }
}
