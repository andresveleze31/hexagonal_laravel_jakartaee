package org.citas.jakarta.hexagonal.infraestructure.adapters.out.mariadb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.citas.jakarta.hexagonal.domain.model.Cita;
import org.citas.jakarta.hexagonal.domain.ports.out.CitaRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CitaMariaDbAdapter implements CitaRepository {

    private Connection connection;
    private static final String URL = "jdbc:mariadb://localhost:3306/citasdb";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";

    @Inject
    AgenteMariaDbAdapter agenteAdapter;

    @PostConstruct
    public void init() {
        try {
            // Registrar el driver de MariaDB
            Class.forName("org.mariadb.jdbc.Driver");
            
            // Crear conexión
            Properties connectionProps = new Properties();
            connectionProps.setProperty("user", USER);
            connectionProps.setProperty("password", PASSWORD);
            connectionProps.setProperty("useSSL", "false");
            connectionProps.setProperty("serverTimezone", "UTC");
            
            this.connection = DriverManager.getConnection(URL, connectionProps);
            System.out.println("✅ Conexión a MariaDB establecida para Citas");
            
            // Crear tabla si no existe
            crearTablaSiNoExiste();
            
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con la base de datos", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✅ Conexión a MariaDB para Citas cerrada");
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión de Citas: " + e.getMessage());
            }
        }
    }

    private void crearTablaSiNoExiste() {
        String sql = """
            CREATE TABLE IF NOT EXISTS citas (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                agente_id BIGINT NOT NULL,
                cliente_nombre VARCHAR(100) NOT NULL,
                cliente_email VARCHAR(150) NOT NULL,
                fecha_hora TIMESTAMP NOT NULL,
                motivo VARCHAR(500),
                fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                fecha_actualizacion TIMESTAMP NULL,
                FOREIGN KEY (agente_id) REFERENCES agentes(id) ON DELETE CASCADE,
                INDEX idx_agente_fecha (agente_id, fecha_hora),
                INDEX idx_fecha (fecha_hora)
            )
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
            System.out.println("✅ Tabla 'citas' verificada/creada");
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear tabla citas", e);
        }
    }

    @Override
    public Cita guardar(Cita cita) {
        if (cita.getId() == null) {
            return insertarCita(cita);
        } else {
            return actualizarCita(cita);
        }
    }

    private Cita insertarCita(Cita cita) {
        String sql = """
            INSERT INTO citas (agente_id, cliente_nombre, cliente_email, fecha_hora, motivo, fecha_creacion) 
            VALUES (?, ?, ?, ?, ?, NOW())
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, cita.getAgenteId());
            stmt.setString(2, cita.getClienteNombre());
            stmt.setString(3, cita.getClienteEmail());
            stmt.setTimestamp(4, Timestamp.valueOf(cita.getFechaHora()));
            stmt.setString(5, cita.getMotivo());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La inserción falló, no se generó ID");
            }
            
            // Obtener el ID generado
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    return new Cita(id, cita.getAgenteId(), cita.getClienteNombre(), 
                                  cita.getClienteEmail(), cita.getFechaHora(), cita.getMotivo());
                } else {
                    throw new SQLException("La inserción falló, no se obtuvo ID");
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar cita en la base de datos", e);
        }
    }

    private Cita actualizarCita(Cita cita) {
        String sql = """
            UPDATE citas 
            SET cliente_nombre = ?, cliente_email = ?, fecha_hora = ?, motivo = ?, fecha_actualizacion = NOW() 
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, cita.getClienteNombre());
            stmt.setString(2, cita.getClienteEmail());
            stmt.setTimestamp(3, Timestamp.valueOf(cita.getFechaHora()));
            stmt.setString(4, cita.getMotivo());
            stmt.setLong(5, cita.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new RuntimeException("Cita no encontrada con id: " + cita.getId());
            }
            
            return cita;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar cita en la base de datos", e);
        }
    }

    @Override
    public List<Cita> findAll() {
        String sql = "SELECT id, agente_id, cliente_nombre, cliente_email, fecha_hora, motivo FROM citas";
        List<Cita> citas = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                citas.add(mapResultSetToCita(rs));
            }
            
            return citas;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todas las citas", e);
        }
    }

    @Override
    public Optional<Cita> findById(Long id) {
        String sql = "SELECT id, agente_id, cliente_nombre, cliente_email, fecha_hora, motivo FROM citas WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCita(rs));
                } else {
                    return Optional.empty();
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cita por ID", e);
        }
    }

    @Override
    public List<Cita> findByAgenteId(Long agenteId) {
        String sql = "SELECT id, agente_id, cliente_nombre, cliente_email, fecha_hora, motivo FROM citas WHERE agente_id = ?";
        List<Cita> citas = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setLong(1, agenteId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    citas.add(mapResultSetToCita(rs));
                }
            }
            
            return citas;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar citas por agente", e);
        }
    }

    @Override
    public List<Cita> findByFecha(LocalDateTime fecha) {
        LocalDateTime startOfDay = fecha.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = fecha.toLocalDate().atTime(23, 59, 59);
        
        String sql = "SELECT id, agente_id, cliente_nombre, cliente_email, fecha_hora, motivo FROM citas WHERE fecha_hora BETWEEN ? AND ?";
        List<Cita> citas = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(startOfDay));
            stmt.setTimestamp(2, Timestamp.valueOf(endOfDay));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    citas.add(mapResultSetToCita(rs));
                }
            }
            
            return citas;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar citas por fecha", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM citas WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar cita", e);
        }
    }

    // Métodos adicionales específicos del adaptador JDBC
    public List<Cita> findByAgenteIdAndFecha(Long agenteId, LocalDateTime fecha) {
        LocalDateTime startOfDay = fecha.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = fecha.toLocalDate().atTime(23, 59, 59);
        
        String sql = "SELECT id, agente_id, cliente_nombre, cliente_email, fecha_hora, motivo FROM citas WHERE agente_id = ? AND fecha_hora BETWEEN ? AND ?";
        List<Cita> citas = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setLong(1, agenteId);
            stmt.setTimestamp(2, Timestamp.valueOf(startOfDay));
            stmt.setTimestamp(3, Timestamp.valueOf(endOfDay));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    citas.add(mapResultSetToCita(rs));
                }
            }
            
            return citas;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar citas por agente y fecha", e);
        }
    }

    public List<Cita> findByClienteEmail(String clienteEmail) {
        String sql = "SELECT id, agente_id, cliente_nombre, cliente_email, fecha_hora, motivo FROM citas WHERE cliente_email = ?";
        List<Cita> citas = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, clienteEmail);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    citas.add(mapResultSetToCita(rs));
                }
            }
            
            return citas;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar citas por email del cliente", e);
        }
    }

    public boolean existsByAgenteIdAndFechaHora(Long agenteId, LocalDateTime fechaHora) {
        String sql = "SELECT COUNT(*) FROM citas WHERE agente_id = ? AND fecha_hora = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setLong(1, agenteId);
            stmt.setTimestamp(2, Timestamp.valueOf(fechaHora));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar existencia de cita", e);
        }
    }

    public List<Cita> findCitasProximas(LocalDateTime desde, int dias) {
        LocalDateTime hasta = desde.plusDays(dias);
        
        String sql = "SELECT id, agente_id, cliente_nombre, cliente_email, fecha_hora, motivo FROM citas WHERE fecha_hora BETWEEN ? AND ? ORDER BY fecha_hora ASC";
        List<Cita> citas = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(desde));
            stmt.setTimestamp(2, Timestamp.valueOf(hasta));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    citas.add(mapResultSetToCita(rs));
                }
            }
            
            return citas;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar citas próximas", e);
        }
    }

    private Cita mapResultSetToCita(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long agenteId = rs.getLong("agente_id");
        String clienteNombre = rs.getString("cliente_nombre");
        String clienteEmail = rs.getString("cliente_email");
        LocalDateTime fechaHora = rs.getTimestamp("fecha_hora").toLocalDateTime();
        String motivo = rs.getString("motivo");
        
        return new Cita(id, agenteId, clienteNombre, clienteEmail, fechaHora, motivo);
    }

    // Método para verificar conexión (útil para health checks)
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    // Método para obtener estadísticas básicas
    public Map<String, Integer> obtenerEstadisticas() {
        Map<String, Integer> estadisticas = new HashMap<>();
        
        // Total de citas
        String sqlTotal = "SELECT COUNT(*) as total FROM citas";
        try (PreparedStatement stmt = connection.prepareStatement(sqlTotal);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                estadisticas.put("total", rs.getInt("total"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener estadísticas de citas", e);
        }
        
        // Citas por agente
        String sqlPorAgente = "SELECT agente_id, COUNT(*) as total FROM citas GROUP BY agente_id";
        try (PreparedStatement stmt = connection.prepareStatement(sqlPorAgente);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Long agenteId = rs.getLong("agente_id");
                int total = rs.getInt("total");
                estadisticas.put("agente_" + agenteId, total);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener estadísticas por agente", e);
        }
        
        return estadisticas;
    }

    // Método para obtener citas con información del agente (JOIN)
    public List<Map<String, Object>> findCitasConAgente() {
        String sql = """
            SELECT c.id, c.cliente_nombre, c.cliente_email, c.fecha_hora, c.motivo, 
                   a.nombre as agente_nombre, a.especialidad as agente_especialidad
            FROM citas c
            INNER JOIN agentes a ON c.agente_id = a.id
            ORDER BY c.fecha_hora DESC
            """;
        
        List<Map<String, Object>> resultados = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> resultado = new HashMap<>();
                resultado.put("id", rs.getLong("id"));
                resultado.put("clienteNombre", rs.getString("cliente_nombre"));
                resultado.put("clienteEmail", rs.getString("cliente_email"));
                resultado.put("fechaHora", rs.getTimestamp("fecha_hora").toLocalDateTime());
                resultado.put("motivo", rs.getString("motivo"));
                resultado.put("agenteNombre", rs.getString("agente_nombre"));
                resultado.put("agenteEspecialidad", rs.getString("agente_especialidad"));
                
                resultados.add(resultado);
            }
            
            return resultados;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar citas con información del agente", e);
        }
    }
}