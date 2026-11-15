package pe.utpvet.citas.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.utpvet.citas.model.HorarioVeterinario;
import pe.utpvet.citas.model.Veterinario;
import pe.utpvet.citas.repository.HorarioVeterinarioRepository;
import pe.utpvet.citas.repository.VeterinarioRepository;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/veterinarios")
@RequiredArgsConstructor
public class VeterinarioController {

    private final VeterinarioRepository veterinarioRepository;
    private final HorarioVeterinarioRepository horarioRepository;

    @GetMapping
    public List<Veterinario> listar() {
        return veterinarioRepository.findAll();
    }

    @GetMapping("/{id}")
    public Veterinario obtener(@PathVariable Integer id) {
        return veterinarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Veterinario no encontrado: " + id));
    }

    @GetMapping("/{id}/horarios")
    public List<HorarioVeterinario> horarios(@PathVariable Integer id) {
        return horarioRepository.findByVeterinarioId(id);
    }

    @PostMapping
    public ResponseEntity<Veterinario> crear(@Valid @RequestBody Veterinario veterinario) {
        veterinario.setId(null);
        if (veterinario.getActivo() == null) {
            veterinario.setActivo(true);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(veterinarioRepository.save(veterinario));
    }
}
