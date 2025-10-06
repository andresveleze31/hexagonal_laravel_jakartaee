package org.citas.jakarta.hexagonal.infraestructure.adapters.in.soap;

import org.citas.jakarta.hexagonal.domain.model.Agente;
import org.citas.jakarta.hexagonal.domain.ports.in.AgenteServiceUseCase;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.xml.bind.annotation.XmlElement;
import java.util.List;

@Stateless
@WebService(
    serviceName = "AgenteService",
    portName = "AgenteServicePort",
    targetNamespace = "http://citas.com/ws"
)
public class AgenteSoapAdapter {

    @Inject
    AgenteServiceUseCase agenteService;

    @WebMethod(operationName = "crearAgente")
    public AgenteResponse crearAgente(
            @WebParam(name = "nombre") @XmlElement(required = true) String nombre,
            @WebParam(name = "especialidad") String especialidad,
            @WebParam(name = "email") @XmlElement(required = true) String email) {

        try {
            System.out.println("📞 SOAP - Crear agente recibido:");
            System.out.println("Nombre: " + nombre);
            System.out.println("Especialidad: " + especialidad);
            System.out.println("Email: " + email);

            if (nombre == null || nombre.trim().isEmpty()) {
                return new AgenteResponse(false, "El nombre es requerido", null);
            }

            if (email == null || email.trim().isEmpty()) {
                return new AgenteResponse(false, "El email es requerido", null);
            }

            Agente agente = new Agente();
            agente.setNombre(nombre);
            agente.setEspecialidad(especialidad);
            agente.setEmail(email);
            agente.setActivo(true);

            Agente nuevoAgente = agenteService.crearAgente(agente);
            return new AgenteResponse(true, "Agente creado correctamente", nuevoAgente);

        } catch (IllegalArgumentException e) {
            return new AgenteResponse(false, e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return new AgenteResponse(false, "Error interno del servidor: " + e.getMessage(), null);
        }
    }

    @WebMethod(operationName = "obtenerAgente")
    public AgenteResponse obtenerAgente(@WebParam(name = "id") Long id) {
        try {
            return agenteService.obtenerAgente(id)
                    .map(agente -> new AgenteResponse(true, "Agente encontrado", agente))
                    .orElse(new AgenteResponse(false, "Agente no encontrado con ID: " + id, null));
        } catch (Exception e) {
            e.printStackTrace();
            return new AgenteResponse(false, "Error al obtener agente: " + e.getMessage(), null);
        }
    }

    @WebMethod(operationName = "obtenerTodosAgentes")
    public AgenteListResponse obtenerTodosAgentes() {
        try {
            List<Agente> agentes = agenteService.obtenerTodosAgentes();
            return new AgenteListResponse(true, "Agentes obtenidos correctamente", agentes);
        } catch (Exception e) {
            e.printStackTrace();
            return new AgenteListResponse(false, "Error al obtener agentes: " + e.getMessage(), null);
        }
    }

    @WebMethod(operationName = "obtenerAgentesActivos")
    public AgenteListResponse obtenerAgentesActivos() {
        try {
            List<Agente> agentes = agenteService.obtenerTodosAgentes()
                    .stream()
                    .filter(Agente::isActivo)
                    .toList();
            return new AgenteListResponse(true, "Agentes activos obtenidos correctamente", agentes);
        } catch (Exception e) {
            e.printStackTrace();
            return new AgenteListResponse(false, "Error al obtener agentes activos: " + e.getMessage(), null);
        }
    }

    @WebMethod(operationName = "actualizarAgente")
    public AgenteResponse actualizarAgente(
            @WebParam(name = "id") @XmlElement(required = true) Long id,
            @WebParam(name = "nombre") @XmlElement(required = true) String nombre,
            @WebParam(name = "especialidad") String especialidad,
            @WebParam(name = "email") @XmlElement(required = true) String email,
            @WebParam(name = "activo") boolean activo) {

        try {
            if (agenteService.obtenerAgente(id).isEmpty()) {
                return new AgenteResponse(false, "Agente no encontrado con ID: " + id, null);
            }

            Agente agente = new Agente();
            agente.setId(id);
            agente.setNombre(nombre);
            agente.setEspecialidad(especialidad);
            agente.setEmail(email);
            agente.setActivo(activo);

            Agente agenteActualizado = agenteService.crearAgente(agente);
            return new AgenteResponse(true, "Agente actualizado correctamente", agenteActualizado);

        } catch (IllegalArgumentException e) {
            return new AgenteResponse(false, e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            return new AgenteResponse(false, "Error al actualizar agente: " + e.getMessage(), null);
        }
    }

    @WebMethod(operationName = "eliminarAgente")
    public BasicResponse eliminarAgente(@WebParam(name = "id") Long id) {
        try {
            boolean eliminado = agenteService.eliminarAgente(id);
            if (eliminado) {
                return new BasicResponse(true, "Agente eliminado correctamente");
            } else {
                return new BasicResponse(false, "Agente no encontrado con ID: " + id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new BasicResponse(false, "Error al eliminar agente: " + e.getMessage());
        }
    }

    @WebMethod(operationName = "health")
    public String health() {
        return "AgenteSoapAdapter is working!";
    }

    // ------------------ Clases de respuesta ------------------
    public static class AgenteResponse {
        private boolean success;
        private String message;
        private Agente agente;

        public AgenteResponse() {}

        public AgenteResponse(boolean success, String message, Agente agente) {
            this.success = success;
            this.message = message;
            this.agente = agente;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public Agente getAgente() { return agente; }
        public void setAgente(Agente agente) { this.agente = agente; }
    }

    public static class AgenteListResponse {
        private boolean success;
        private String message;
        private List<Agente> agentes;

        public AgenteListResponse() {}

        public AgenteListResponse(boolean success, String message, List<Agente> agentes) {
            this.success = success;
            this.message = message;
            this.agentes = agentes;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public List<Agente> getAgentes() { return agentes; }
        public void setAgentes(List<Agente> agentes) { this.agentes = agentes; }
    }

    public static class BasicResponse {
        private boolean success;
        private String message;

        public BasicResponse() {}

        public BasicResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
