package org.citas.jakarta.hexagonal.domain.ports.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.citas.jakarta.hexagonal.domain.model.Cita;

public interface CitaRepository {
    Cita guardar(Cita cita);
    Optional<Cita> findById(Long id);
    List<Cita> findAll();
    List<Cita> findByAgenteId(Long agenteId);
    List<Cita> findByFecha(LocalDateTime fecha);
    boolean deleteById(Long id);
}