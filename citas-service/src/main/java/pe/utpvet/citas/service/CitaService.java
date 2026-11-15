package pe.utpvet.citas.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.utpvet.citas.dto.CitaRequest;
import pe.utpvet.citas.model.*;
import pe.utpvet.citas.repository.*;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CitaService {

    private final CitaRepository citaRepository;
    private final EstadoCitaRepository estadoCitaRepository;
    private final MascotaRepository mascotaRepository;
    private final PropietarioRepository propietarioRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final HistorialEstadoCitaRepository historialRepository;
    private final EntityManager entityManager;

    public List<Cita> listar() {
        return citaRepository.findAll();
    }

    public Cita obtener(Integer id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Cita no encontrada: " + id));
    }

    public List<HistorialEstadoCita> historial(Integer citaId) {
        return historialRepository.findByCitaIdOrderByFechaCambioAsc(citaId);
    }

    @Transactional
    public Cita crear(CitaRequest request, Integer usuarioId) {
        Mascota mascota = mascotaRepository.findById(request.mascotaId())
                .orElseThrow(() -> new NoSuchElementException("Mascota no encontrada: " + request.mascotaId()));
        Propietario propietario = propietarioRepository.findById(request.propietarioId())
                .orElseThrow(() -> new NoSuchElementException("Propietario no encontrado: " + request.propietarioId()));
        Veterinario veterinario = veterinarioRepository.findById(request.veterinarioId())
                .orElseThrow(() -> new NoSuchElementException("Veterinario no encontrado: " + request.veterinarioId()));
        EstadoCita programada = estadoCitaRepository.findByNombre("PROGRAMADA")
                .orElseThrow(() -> new IllegalStateException("Estado PROGRAMADA no está sembrado en estados_cita"));

        Cita cita = new Cita();
        cita.setMascota(mascota);
        cita.setPropietario(propietario);
        cita.setVeterinario(veterinario);
        cita.setEstado(programada);
        cita.setFechaCita(request.fechaCita());
        cita.setHoraInicio(request.horaInicio());
        cita.setHoraFin(request.horaFin());
        cita.setMotivo(request.motivo());
        cita.setCreadoPor(usuarioId);

        // Si hay solapamiento con otra cita activa del mismo veterinario,
        // el trigger fn_validar_solapamiento_cita de la base de datos lanza
        // una excepción que GlobalExceptionHandler traduce a HTTP 409.
        Cita guardada = citaRepository.save(cita);
        entityManager.refresh(guardada); // trae fecha_creacion/fecha_modificacion puestas por la BD
        return guardada;
    }

    @Transactional
    public Cita cambiarEstado(Integer citaId, String nuevoEstadoNombre, String observacion, Integer usuarioId) {
        Cita cita = obtener(citaId);
        EstadoCita nuevoEstado = estadoCitaRepository.findByNombre(nuevoEstadoNombre)
                .orElseThrow(() -> new IllegalArgumentException("Estado inválido: " + nuevoEstadoNombre));

        cita.setEstado(nuevoEstado);
        cita.setActualizadoPor(usuarioId);
        Cita guardada = citaRepository.save(cita);
        entityManager.refresh(guardada); // trae fecha_modificacion puesta por el trigger

        if (observacion != null && !observacion.isBlank()) {
            List<HistorialEstadoCita> historial = historialRepository.findByCitaIdOrderByFechaCambioAsc(citaId);
            if (!historial.isEmpty()) {
                HistorialEstadoCita ultimo = historial.get(historial.size() - 1);
                ultimo.setObservacion(observacion);
                historialRepository.save(ultimo);
            }
        }

        return guardada;
    }
}
