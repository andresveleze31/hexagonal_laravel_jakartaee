package org.citas.jakarta.hexagonal.domain.ports.in;

import java.util.List;
import java.util.Optional;

import org.citas.jakarta.hexagonal.domain.model.Agente;

public interface AgenteServiceUseCase {
    Agente crearAgente(Agente agente);
    Agente actualizarAgente(Agente agente);
    Optional<Agente> obtenerAgente(Long id);
    List<Agente> obtenerTodosAgentes();
    boolean eliminarAgente(Long id);
}
