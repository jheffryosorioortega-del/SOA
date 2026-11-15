package pe.utpvet.citas.controller;

import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.utpvet.citas.model.Mascota;
import pe.utpvet.citas.repository.MascotaRepository;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaRepository mascotaRepository;
    private final EntityManager entityManager;

    @GetMapping
    public List<Mascota> listar() {
        return mascotaRepository.findAll();
    }

    @GetMapping("/{id}")
    public Mascota obtener(@PathVariable Integer id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Mascota no encontrada: " + id));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Mascota> crear(@Valid @RequestBody Mascota mascota) {
        mascota.setId(null);
        if (mascota.getActivo() == null) {
            mascota.setActivo(true);
        }
        Mascota guardada = mascotaRepository.save(mascota);
        entityManager.refresh(guardada); // trae fecha_registro puesta por la BD
        return ResponseEntity.status(HttpStatus.CREATED).body(guardada);
    }
}
