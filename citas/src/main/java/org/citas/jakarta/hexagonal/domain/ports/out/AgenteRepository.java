package org.citas.jakarta.hexagonal.domain.ports.out;

import java.util.List;
import java.util.Optional;

import org.citas.jakarta.hexagonal.domain.model.Agente;

public interface AgenteRepository {
    Agente guardar(Agente agente);
    Optional<Agente> findById(Long id);
    List<Agente> findAll();
    boolean deleteById(Long id);
}