package pe.utpvet.citas.exception;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CitaConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(CitaConflictException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    // Cubre la excepción que lanza el trigger fn_validar_solapamiento_cita
    // de la base de datos cuando dos citas del mismo veterinario se solapan.
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> handleDataAccess(DataAccessException e) {
        String rootMessage = rootCauseMessage(e);
        if (rootMessage != null && rootMessage.contains("se solapa")) {
            String limpio = rootMessage.lines().findFirst().orElse(rootMessage).replaceFirst("^ERROR:\\s*", "");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", limpio));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", rootMessage != null ? rootMessage : "Error de acceso a datos"));
    }

    private String rootCauseMessage(Throwable e) {
        Throwable cursor = e;
        while (cursor.getCause() != null && cursor.getCause() != cursor) {
            cursor = cursor.getCause();
        }
        return cursor.getMessage();
    }
}
