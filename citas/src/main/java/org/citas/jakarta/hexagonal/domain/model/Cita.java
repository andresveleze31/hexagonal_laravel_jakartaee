package org.citas.jakarta.hexagonal.domain.model;

import java.time.LocalDateTime;

public class Cita {
    private Long id;
    private Long agenteId;
    private String clienteNombre;
    private String clienteEmail;
    private LocalDateTime fechaHora;
    private String motivo;

    public Cita() {
        // Constructor vacío necesario para JAX-RS
    }

    public Cita(Long id, Long agenteId, String clienteNombre, String clienteEmail,
            LocalDateTime fechaHora, String motivo) {
        this.id = id;
        this.agenteId = agenteId;
        this.clienteNombre = clienteNombre;
        this.clienteEmail = clienteEmail;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setAgenteId(Long agenteId) {
        this.agenteId = agenteId;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getAgenteId() {
        return agenteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public String getMotivo() {
        return motivo;
    }
}
