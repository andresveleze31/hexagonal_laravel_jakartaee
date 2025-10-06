package org.citas.jakarta.hexagonal.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.citas.jakarta.hexagonal.domain.model.Cita;
import org.citas.jakarta.hexagonal.domain.ports.in.CitaServiceUseCase;
import org.citas.jakarta.hexagonal.domain.ports.out.AgenteRepository;
import org.citas.jakarta.hexagonal.domain.ports.out.CitaRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CitaServiceImpl implements CitaServiceUseCase {

    @Inject
    private CitaRepository citaRepository;
    
    @Inject
    private AgenteRepository agenteRepository;

    @Override
    public Cita agendarCita(Cita cita) {
        // Validar que el agente existe
        var agente = agenteRepository.findById(cita.getAgenteId());
        if (agente.isEmpty()) {
            throw new IllegalArgumentException("Agente no encontrado");
        }
        
        // Validar disponibilidad
        var citasExistentes = citaRepository.findByAgenteId(cita.getAgenteId());
        boolean conflicto = citasExistentes.stream()
            .anyMatch(c -> c.getFechaHora().equals(cita.getFechaHora()));
            
        if (conflicto) {
            throw new IllegalStateException("El agente ya tiene una cita en ese horario");
        }
        
        return citaRepository.guardar(cita);
    }

    @Override
    public Optional<Cita> obtenerCita(Long id) {
        return citaRepository.findById(id);
    }

    @Override
    public boolean eliminarCita(Long id) {
        return citaRepository.deleteById(id);
    }

    @Override
    public List<Cita> obtenerCitasPorAgente(Long agenteId) {
        return citaRepository.findByAgenteId(agenteId);
    }

    @Override
    public List<Cita> obtenerCitas() {
        return citaRepository.findAll();
    }

    @Override
    public List<Cita> obtenerCitasPorFecha(LocalDateTime fecha) {
        return citaRepository.findByFecha(fecha);
    }

   
}
