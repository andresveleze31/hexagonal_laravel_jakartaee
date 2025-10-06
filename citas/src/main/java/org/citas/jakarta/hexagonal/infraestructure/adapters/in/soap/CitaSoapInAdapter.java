package org.citas.jakarta.hexagonal.infraestructure.adapters.in.soap;

import org.citas.jakarta.hexagonal.domain.model.Cita;
import org.citas.jakarta.hexagonal.domain.ports.in.CitaServiceUseCase;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Stateless
@WebService(
    serviceName = "CitaService",
    portName = "CitaPort",
    targetNamespace = "http://soap.citas.org/"
)
public class CitaSoapInAdapter {

    @Inject
    private CitaServiceUseCase citaService;

    // ✅ 1. Agendar nueva cita
    @WebMethod
    public String agendarCita(@WebParam(name = "cita") Cita cita) {
        try {
            if (cita.getAgenteId() == null)
                return "❌ Error: El ID del agente es requerido";

            if (cita.getClienteNombre() == null || cita.getClienteNombre().isBlank())
                return "❌ Error: El nombre del cliente es requerido";

            if (cita.getClienteEmail() == null || cita.getClienteEmail().isBlank())
                return "❌ Error: El email del cliente es requerido";

            if (cita.getFechaHora() == null)
                return "❌ Error: La fecha y hora son requeridas";

            if (cita.getFechaHora().isBefore(LocalDateTime.now()))
                return "❌ Error: No se pueden agendar citas en fechas pasadas";

            citaService.agendarCita(cita);
            return "✅ Cita agendada correctamente";

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ Error interno: " + e.getMessage();
        }
    }

    // ✅ 2. Obtener cita por ID
    @WebMethod
    public Cita obtenerCita(@WebParam(name = "id") Long id) {
        try {
            Optional<Cita> cita = citaService.obtenerCita(id);
            return cita.orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ✅ 3. Obtener citas por agente
    @WebMethod
    public List<Cita> obtenerCitasPorAgente(@WebParam(name = "agenteId") Long agenteId) {
        try {
            return citaService.obtenerCitasPorAgente(agenteId);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // ✅ 4. Obtener citas por fecha
    @WebMethod
    public List<Cita> obtenerCitasPorFecha(@WebParam(name = "fecha") String fechaStr) {
        try {
            LocalDateTime fecha = LocalDateTime.parse(fechaStr + "T00:00:00");
            return citaService.obtenerCitasPorFecha(fecha);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // ✅ 5. Obtener todas las citas
    @WebMethod
    public List<Cita> obtenerTodasCitas() {
        try {
            return citaService.obtenerCitasPorAgente(null);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    // ✅ 6. Actualizar cita
    @WebMethod
    public String actualizarCita(
            @WebParam(name = "id") Long id,
            @WebParam(name = "cita") Cita cita) {

        try {
            if (!id.equals(cita.getId()))
                return "❌ Error: El ID del cuerpo no coincide con el de la URL";

            if (cita.getFechaHora().isBefore(LocalDateTime.now()))
                return "❌ Error: No se pueden agendar citas en fechas pasadas";

            citaService.agendarCita(cita);
            return "✅ Cita actualizada correctamente";

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ Error al actualizar cita: " + e.getMessage();
        }
    }


    // ✅ 8. Health check
    @WebMethod
    public String health() {
        return "CitaSoapInAdapter is working!";
    }
}
