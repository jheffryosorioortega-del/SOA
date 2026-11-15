package pe.utpvet.citas.controller;

import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.utpvet.citas.model.Propietario;
import pe.utpvet.citas.repository.PropietarioRepository;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/propietarios")
@RequiredArgsConstructor
public class PropietarioController {

    private final PropietarioRepository propietarioRepository;
    private final EntityManager entityManager;

    @GetMapping
    public List<Propietario> listar() {
        return propietarioRepository.findAll();
    }

    @GetMapping("/{id}")
    public Propietario obtener(@PathVariable Integer id) {
        return propietarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Propietario no encontrado: " + id));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Propietario> crear(@Valid @RequestBody Propietario propietario) {
        propietario.setId(null);
        if (propietario.getTipoDocumento() == null) {
            propietario.setTipoDocumento("DNI");
        }
        Propietario guardado = propietarioRepository.save(propietario);
        entityManager.refresh(guardado); // trae fecha_registro puesta por la BD
        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }
}
