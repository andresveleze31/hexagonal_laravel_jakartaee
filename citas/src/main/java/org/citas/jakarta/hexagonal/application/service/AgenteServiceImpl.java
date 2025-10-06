package org.citas.jakarta.hexagonal.application.service;

import java.util.List;
import java.util.Optional;

import org.citas.jakarta.hexagonal.domain.model.Agente;
import org.citas.jakarta.hexagonal.domain.ports.in.AgenteServiceUseCase;
import org.citas.jakarta.hexagonal.domain.ports.out.AgenteRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AgenteServiceImpl implements AgenteServiceUseCase {

    @Inject
    private AgenteRepository agenteRepository;

    @Override
    public Agente crearAgente(Agente agente) {
        return agenteRepository.guardar(agente);
    }

    @Override
    public Agente actualizarAgente(Agente agente) {
        return agenteRepository.guardar(agente);
    }

    @Override
    public Optional<Agente> obtenerAgente(Long id) {
        return agenteRepository.findById(id);
    }

    @Override
    public List<Agente> obtenerTodosAgentes() {
        return agenteRepository.findAll();
    }

    @Override
    public boolean eliminarAgente(Long id) {
        return agenteRepository.deleteById(id);
    }
}