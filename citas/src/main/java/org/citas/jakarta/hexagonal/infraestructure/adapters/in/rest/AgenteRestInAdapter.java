package org.citas.jakarta.hexagonal.infraestructure.adapters.in.rest;

import java.util.List;
import java.util.Map;

import org.citas.jakarta.hexagonal.domain.model.Agente;
import org.citas.jakarta.hexagonal.domain.ports.in.AgenteServiceUseCase;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/agentes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AgenteRestInAdapter {

    @Inject
    AgenteServiceUseCase agenteService;

    // ✅ CREAR NUEVO AGENTE
    @POST
    public Response crearAgente(Agente agente) {
        try {
            System.out.println(agente.getNombre());
            // Validaciones básicas en el adaptador
            if (agente.getNombre() == null || agente.getNombre().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El nombre es requerido"))
                        .build();
            }
            
            if (agente.getEmail() == null || agente.getEmail().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El email es requerido"))
                        .build();
            }

            Agente nuevoAgente = agenteService.crearAgente(agente);
            
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of(
                        "mensaje", "Agente creado correctamente",
                        "agente", nuevoAgente
                    ))
                    .build();

        } catch (IllegalArgumentException e) {
            // Errores de validación de negocio
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            // Errores inesperados
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error interno del servidor: " + e.getMessage()))
                    .build();
        }
    }

    // ✅ OBTENER AGENTE POR ID
    @GET
    @Path("/{id}")
    public Response obtenerAgente(@PathParam("id") Long id) {
        try {
            return agenteService.obtenerAgente(id)
                    .map(agente -> Response.ok(
                        Map.of("agente", agente)
                    ).build())
                    .orElse(Response.status(Response.Status.NOT_FOUND)
                            .entity(Map.of("error", "Agente no encontrado con ID: " + id))
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener agente: " + e.getMessage()))
                    .build();
        }
    }

    

    // ✅ LISTAR TODOS LOS AGENTES
    @GET
    public Response obtenerTodosAgentes() {
        try {
            List<Agente> agentes = agenteService.obtenerTodosAgentes();
            
            return Response.ok(
                Map.of(
                    "total", agentes.size(),
                    "agentes", agentes
                )
            ).build();
            
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener agentes: " + e.getMessage()))
                    .build();
        }
    }

    // ✅ ELIMINAR AGENTE
    @DELETE
    @Path("/{id}")
    public Response eliminarAgente(@PathParam("id") Long id) {
        try {
            boolean eliminado = agenteService.eliminarAgente(id);
            
            if (eliminado) {
                return Response.noContent().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Agente no encontrado con ID: " + id))
                        .build();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al eliminar agente: " + e.getMessage()))
                    .build();
        }
    }

    // ✅ ACTUALIZAR AGENTE
    @PUT
    @Path("/{id}")
    public Response actualizarAgente(@PathParam("id") Long id, Agente agente) {
        try {
            // Verificar que el ID de la URL coincide con el del cuerpo
            if (!id.equals(agente.getId())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El ID de la URL no coincide con el ID del agente"))
                        .build();
            }

            // Verificar que el agente existe
            if (agenteService.obtenerAgente(id).isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Agente no encontrado con ID: " + id))
                        .build();
            }

            Agente agenteActualizado = agenteService.crearAgente(agente);
            
            return Response.ok(
                Map.of(
                    "mensaje", "Agente actualizado correctamente",
                    "agente", agenteActualizado
                )
            ).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al actualizar agente: " + e.getMessage()))
                    .build();
        }
    }

    // ✅ ENDPOINT ADICIONAL: Buscar agentes activos
    @GET
    @Path("/activos")
    public Response obtenerAgentesActivos() {
        try {
            List<Agente> agentes = agenteService.obtenerTodosAgentes()
                    .stream()
                    .filter(Agente::isActivo)
                    .toList();
            
            return Response.ok(
                Map.of(
                    "total", agentes.size(),
                    "agentes", agentes
                )
            ).build();
            
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener agentes activos: " + e.getMessage()))
                    .build();
        }
    }

    // ✅ ENDPOINT ADICIONAL: Health check del adaptador
    @GET
    @Path("/health")
    @Produces(MediaType.TEXT_PLAIN)
    public String health() {
        return "AgenteRestAdapter is working!";
    }
}