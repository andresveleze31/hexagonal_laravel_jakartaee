package org.citas.jakarta.hexagonal.infraestructure.adapters.in.rest;

import org.citas.jakarta.hexagonal.domain.model.Cita;
import org.citas.jakarta.hexagonal.domain.ports.in.CitaServiceUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Path("/citas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CitaRestInAdapter {

    @Inject
    CitaServiceUseCase citaService;

    // ✅ AGENDAR NUEVA CITA
    @POST
    public Response agendarCita(Cita cita) {
        try {
            System.out.println("📅 Datos recibidos para nueva cita:");
            System.out.println("Agente ID: " + cita.getAgenteId());
            System.out.println("Cliente: " + cita.getClienteNombre());
            System.out.println("Email: " + cita.getClienteEmail());
            System.out.println("Fecha: " + cita.getFechaHora());
            System.out.println("Motivo: " + cita.getMotivo());
            
            // Validaciones básicas
            if (cita.getAgenteId() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El ID del agente es requerido"))
                        .build();
            }
            
            if (cita.getClienteNombre() == null || cita.getClienteNombre().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El nombre del cliente es requerido"))
                        .build();
            }
            
            if (cita.getClienteEmail() == null || cita.getClienteEmail().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El email del cliente es requerido"))
                        .build();
            }
            
            if (cita.getFechaHora() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "La fecha y hora son requeridas"))
                        .build();
            }
            
            // Verificar que la fecha no sea en el pasado
            if (cita.getFechaHora().isBefore(LocalDateTime.now())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "No se pueden agendar citas en fechas pasadas"))
                        .build();
            }

            Cita nuevaCita = citaService.agendarCita(cita);
            
            return Response.status(Response.Status.CREATED)
                    .entity(Map.of(
                        "mensaje", "Cita agendada correctamente",
                        "cita", nuevaCita
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

    // ✅ OBTENER CITA POR ID
    @GET
    @Path("/{id}")
    public Response obtenerCita(@PathParam("id") Long id) {
        try {
            return citaService.obtenerCita(id)
                    .map(cita -> Response.ok(
                        Map.of("cita", cita)
                    ).build())
                    .orElse(Response.status(Response.Status.NOT_FOUND)
                            .entity(Map.of("error", "Cita no encontrada con ID: " + id))
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener cita: " + e.getMessage()))
                    .build();
        }
    }

    // ✅ OBTENER CITAS POR AGENTE
    @GET
    @Path("/agente/{agenteId}")
    public Response obtenerCitasPorAgente(@PathParam("agenteId") Long agenteId) {
        try {
            List<Cita> citas = citaService.obtenerCitasPorAgente(agenteId);
            
            return Response.ok(
                Map.of(
                    "agenteId", agenteId,
                    "total", citas.size(),
                    "citas", citas
                )
            ).build();
            
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener citas del agente: " + e.getMessage()))
                    .build();
        }
    }

    // ✅ OBTENER CITAS POR FECHA
    @GET
    @Path("/fecha/{fecha}")
    public Response obtenerCitasPorFecha(@PathParam("fecha") String fechaStr) {
        try {
            LocalDateTime fecha = LocalDateTime.parse(fechaStr + "T00:00:00");
            List<Cita> citas = citaService.obtenerCitasPorFecha(fecha);
            
            return Response.ok(
                Map.of(
                    "fecha", fechaStr,
                    "total", citas.size(),
                    "citas", citas
                )
            ).build();
            
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Formato de fecha inválido. Use: YYYY-MM-DD"))
                    .build();
        }
    }

    @GET
    public Response obtenerTodasCitas() {
        try {
            List<Cita> citas = citaService.obtenerCitas();
            
            return Response.ok(
                Map.of(
                    "total", citas.size(),
                    "citas", citas
                )
            ).build();
            
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener citas: " + e.getMessage()))
                    .build();
        }
    }

    // ✅ LISTAR TODAS LAS CITAS
    // @GET
    // public Response obtenerTodasCitas() {
    //     try {
    //         // En una aplicación real, esto debería tener paginación
    //         // Por ahora usamos obtenerCitasPorAgente con null para obtener todas
    //         List<Cita> citas = citaService.obtenerCitasPorAgente(null);
            
    //         return Response.ok(
    //             Map.of(
    //                 "total", citas.size(),
    //                 "citas", citas
    //             )
    //         ).build();
            
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
    //                 .entity(Map.of("error", "Error al obtener citas: " + e.getMessage()))
    //                 .build();
    //     }
    // }

    // ✅ ACTUALIZAR CITA
    @PUT
    @Path("/{id}")
    public Response actualizarCita(@PathParam("id") Long id, Cita cita) {
        try {
            // Verificar que el ID de la URL coincide con el del cuerpo
            if (!id.equals(cita.getId())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "El ID de la URL no coincide con el ID de la cita"))
                        .build();
            }

            // Verificar que la cita existe
            if (citaService.obtenerCita(id).isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Cita no encontrada con ID: " + id))
                        .build();
            }

            // Validar fecha
            if (cita.getFechaHora().isBefore(LocalDateTime.now())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "No se pueden agendar citas en fechas pasadas"))
                        .build();
            }

            Cita citaActualizada = citaService.agendarCita(cita);
            
            return Response.ok(
                Map.of(
                    "mensaje", "Cita actualizada correctamente",
                    "cita", citaActualizada
                )
            ).build();
            
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al actualizar cita: " + e.getMessage()))
                    .build();
        }
    }

    // // ✅ ELIMINAR CITA
    // @DELETE
    // @Path("/{id}")
    // public Response eliminarCita(@PathParam("id") Long id) {
    //     try {
    //         boolean eliminada = citaService.cancelarCita(id);
            
    //         if (eliminada) {
    //             return Response.ok(
    //                 Map.of("mensaje", "Cita eliminada correctamente")
    //             ).build();
    //         } else {
    //             return Response.status(Response.Status.NOT_FOUND)
    //                     .entity(Map.of("error", "Cita no encontrada con ID: " + id))
    //                     .build();
    //         }
            
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
    //                 .entity(Map.of("error", "Error al eliminar cita: " + e.getMessage()))
    //                 .build();
    //     }
    // }

    // ✅ ELIMINAR CITA
@DELETE
@Path("/{id}")
public Response eliminarCita(@PathParam("id") Long id) {
    try {
        boolean eliminada = citaService.eliminarCita(id);
        
        if (eliminada) {
            return Response.ok(
                Map.of("mensaje", "Cita eliminada correctamente")
            ).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Cita no encontrada con ID: " + id))
                    .build();
        }
        
    } catch (Exception e) {
        e.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of("error", "Error al eliminar cita: " + e.getMessage()))
                .build();
    }
}


    // ✅ ENDPOINT ADICIONAL: Verificar disponibilidad
    @GET
    @Path("/disponibilidad")
    public Response verificarDisponibilidad(
            @QueryParam("agenteId") Long agenteId,
            @QueryParam("fechaHora") String fechaHoraStr) {
        try {
            if (agenteId == null || fechaHoraStr == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Se requieren los parámetros agenteId y fechaHora"))
                        .build();
            }
            
            LocalDateTime fechaHora = LocalDateTime.parse(fechaHoraStr);
            
            // Buscar citas existentes en ese horario
            List<Cita> citasExistentes = citaService.obtenerCitasPorAgente(agenteId)
                    .stream()
                    .filter(c -> c.getFechaHora().equals(fechaHora))
                    .toList();
            
            boolean disponible = citasExistentes.isEmpty();
            
            return Response.ok(
                Map.of(
                    "agenteId", agenteId,
                    "fechaHora", fechaHoraStr,
                    "disponible", disponible,
                    "mensaje", disponible ? "Horario disponible" : "Horario ocupado"
                )
            ).build();
            
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Formato de fecha/hora inválido. Use: YYYY-MM-DDTHH:MM:SS"))
                    .build();
        }
    }

    // ✅ ENDPOINT ADICIONAL: Citas próximas
    @GET
    @Path("/proximas")
    public Response obtenerCitasProximas(@QueryParam("dias") @DefaultValue("7") int dias) {
        try {
            LocalDateTime desde = LocalDateTime.now();
            // En una implementación real, necesitarías un método específico en el servicio
            List<Cita> todasCitas = citaService.obtenerCitasPorAgente(null);
            
            List<Cita> citasProximas = todasCitas.stream()
                    .filter(cita -> !cita.getFechaHora().isBefore(desde) && 
                                   cita.getFechaHora().isBefore(desde.plusDays(dias)))
                    .toList();
            
            return Response.ok(
                Map.of(
                    "desde", desde.toString(),
                    "dias", dias,
                    "total", citasProximas.size(),
                    "citas", citasProximas
                )
            ).build();
            
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener citas próximas: " + e.getMessage()))
                    .build();
        }
    }

    // ✅ ENDPOINT ADICIONAL: Health check
    @GET
    @Path("/health")
    @Produces(MediaType.TEXT_PLAIN)
    public String health() {
        return "CitaRestInAdapter is working!";
    }

    // ✅ ENDPOINT ADICIONAL: Estadísticas
    @GET
    @Path("/estadisticas")
    public Response obtenerEstadisticas() {
        try {
            List<Cita> todasCitas = citaService.obtenerCitasPorAgente(null);
            
            long totalCitas = todasCitas.size();
            long citasFuturas = todasCitas.stream()
                    .filter(cita -> cita.getFechaHora().isAfter(LocalDateTime.now()))
                    .count();
            long citasPasadas = todasCitas.stream()
                    .filter(cita -> cita.getFechaHora().isBefore(LocalDateTime.now()))
                    .count();
            
            return Response.ok(
                Map.of(
                    "totalCitas", totalCitas,
                    "citasFuturas", citasFuturas,
                    "citasPasadas", citasPasadas,
                    "proximaCita", todasCitas.stream()
                            .filter(c -> c.getFechaHora().isAfter(LocalDateTime.now()))
                            .findFirst()
                            .map(Cita::getFechaHora)
                            .orElse(null)
                )
            ).build();
            
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error al obtener estadísticas: " + e.getMessage()))
                    .build();
        }
    }
}