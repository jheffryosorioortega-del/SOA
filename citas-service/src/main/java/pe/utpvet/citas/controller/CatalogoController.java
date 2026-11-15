package pe.utpvet.citas.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.utpvet.citas.model.Especie;
import pe.utpvet.citas.model.EstadoCita;
import pe.utpvet.citas.model.Raza;
import pe.utpvet.citas.repository.EspecieRepository;
import pe.utpvet.citas.repository.EstadoCitaRepository;
import pe.utpvet.citas.repository.RazaRepository;

import java.util.List;

/** Catálogos de referencia (especies, razas, estados de cita) usados al crear mascotas/citas. */
@RestController
@RequiredArgsConstructor
public class CatalogoController {

    private final EspecieRepository especieRepository;
    private final RazaRepository razaRepository;
    private final EstadoCitaRepository estadoCitaRepository;

    @GetMapping("/api/especies")
    public List<Especie> especies() {
        return especieRepository.findAll();
    }

    @GetMapping("/api/razas")
    public List<Raza> razas() {
        return razaRepository.findAll();
    }

    @GetMapping("/api/estados-cita")
    public List<EstadoCita> estadosCita() {
        return estadoCitaRepository.findAll();
    }
}
