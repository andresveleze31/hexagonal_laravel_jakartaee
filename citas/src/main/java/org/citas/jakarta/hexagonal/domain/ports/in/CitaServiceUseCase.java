package org.citas.jakarta.hexagonal.domain.ports.in;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.citas.jakarta.hexagonal.domain.model.Cita;

public interface CitaServiceUseCase {
    Cita agendarCita(Cita cita);
    Optional<Cita> obtenerCita(Long id);
    List<Cita> obtenerCitas();
    boolean eliminarCita(Long id);
    List<Cita> obtenerCitasPorAgente(Long agenteId);
    List<Cita> obtenerCitasPorFecha(LocalDateTime fecha);
}
