package org.citas.jakarta.hexagonal.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Agente {
    private Long id;
    private String nombre;
    private String especialidad;
    private String email;
    private boolean activo;

    private List<Cita> citas;

      // ✅ Constructor vacío (necesario para frameworks y mapeos automáticos)
    public Agente() {}

    public Agente(Long id, String nombre, String especialidad, String email) {
        this.id = id;
        this.nombre = nombre;
        this.especialidad = especialidad;
        this.email = email;
        this.activo = true;
        this.citas = new ArrayList<>();
    }

      // ✅ Setters para TODOS los campos (CRÍTICO)
    public void setId(Long id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public void setEmail(String email) { this.email = email; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public void setCitas(List<Cita> citas) { 
        this.citas = citas != null ? new ArrayList<>(citas) : new ArrayList<>(); 
    }

    // Getters y métodos de dominio
    public boolean puedeAgendarCita() {
        return activo && citas.size() < 10; // Límite de citas
    }

    public void agregarCita(Cita cita) {
        if (puedeAgendarCita()) {
            citas.add(cita);
        }
    }

    // Getters y Setters
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEspecialidad() { return especialidad; }
    public String getEmail() { return email; }
    public boolean isActivo() { return activo; }
    public List<Cita> getCitas() { return new ArrayList<>(citas); }
}
