package org.citas.jakarta.hexagonal.infraestructure.adapters.out.mariadb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.citas.jakarta.hexagonal.domain.model.Agente;
import org.citas.jakarta.hexagonal.domain.ports.out.AgenteRepository;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AgenteMariaDbAdapter implements AgenteRepository {

    private Connection connection;
    private static final String URL = "jdbc:mariadb://localhost:3306/citasdb";
    private static final String USER = "root";
    private static final String PASSWORD = "admin";

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
            System.out.println("✅ Conexión a MariaDB establecida");
            
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
                System.out.println("✅ Conexión a MariaDB cerrada");
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }

    private void crearTablaSiNoExiste() {
        String sql = """
            CREATE TABLE IF NOT EXISTS agentes (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                nombre VARCHAR(100) NOT NULL,
                especialidad VARCHAR(100),
                email VARCHAR(150) NOT NULL UNIQUE,
                activo BOOLEAN DEFAULT TRUE,
                fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                fecha_actualizacion TIMESTAMP NULL
            )
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.execute();
            System.out.println("✅ Tabla 'agentes' verificada/creada");
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear tabla agentes", e);
        }
    }

    @Override
    public Agente guardar(Agente agente) {
        if (agente.getId() == null) {
            return insertarAgente(agente);
        } else {
            return actualizarAgente(agente);
        }
    }

    private Agente insertarAgente(Agente agente) {
        String sql = """
            INSERT INTO agentes (nombre, especialidad, email, activo, fecha_creacion) 
            VALUES (?, ?, ?, ?, NOW())
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, agente.getNombre());
            stmt.setString(2, agente.getEspecialidad());
            stmt.setString(3, agente.getEmail());
            stmt.setBoolean(4, agente.isActivo());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La inserción falló, no se generó ID");
            }
            
            // Obtener el ID generado
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong(1);
                    return new Agente(id, agente.getNombre(), agente.getEspecialidad(), agente.getEmail());
                } else {
                    throw new SQLException("La inserción falló, no se obtuvo ID");
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar agente en la base de datos", e);
        }
    }

    private Agente actualizarAgente(Agente agente) {
        String sql = """
            UPDATE agentes 
            SET nombre = ?, especialidad = ?, email = ?, activo = ?
            WHERE id = ?
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, agente.getNombre());
            stmt.setString(2, agente.getEspecialidad());
            stmt.setString(3, agente.getEmail());
            stmt.setBoolean(4, agente.isActivo());
            stmt.setLong(5, agente.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new RuntimeException("Agente no encontrado con id: " + agente.getId());
            }
            
            return agente;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar agente en la base de datos", e);
        }
    }

    @Override
    public Optional<Agente> findById(Long id) {
        String sql = "SELECT id, nombre, especialidad, email, activo FROM agentes WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAgente(rs));
                } else {
                    return Optional.empty();
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar agente por ID", e);
        }
    }

    @Override
    public List<Agente> findAll() {
        String sql = "SELECT id, nombre, especialidad, email, activo FROM agentes";
        List<Agente> agentes = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                agentes.add(mapResultSetToAgente(rs));
            }
            
            return agentes;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todos los agentes", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM agentes WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();
            
            return affectedRows > 0;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar agente", e);
        }
    }

    // Métodos adicionales específicos del adaptador JDBC
    public Optional<Agente> findByEmail(String email) {
        String sql = "SELECT id, nombre, especialidad, email, activo FROM agentes WHERE email = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAgente(rs));
                } else {
                    return Optional.empty();
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar agente por email", e);
        }
    }

    public List<Agente> findByActivo(boolean activo) {
        String sql = "SELECT id, nombre, especialidad, email, activo FROM agentes WHERE activo = ?";
        List<Agente> agentes = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setBoolean(1, activo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    agentes.add(mapResultSetToAgente(rs));
                }
            }
            
            return agentes;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar agentes por estado activo", e);
        }
    }

    public boolean existsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM agentes WHERE email = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar existencia de email", e);
        }
    }

    private Agente mapResultSetToAgente(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String nombre = rs.getString("nombre");
        String especialidad = rs.getString("especialidad");
        String email = rs.getString("email");
        boolean activo = rs.getBoolean("activo");
        
        Agente agente = new Agente(id, nombre, especialidad, email);
        
        return agente;
    }

    // Método para verificar conexión (útil para health checks)
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }
}
