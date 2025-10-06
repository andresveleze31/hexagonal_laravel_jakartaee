package org.citas.jakarta.hexagonal.infraestructure.adapters.out.oracledb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.citas.jakarta.hexagonal.domain.model.Cita;
import org.citas.jakarta.hexagonal.domain.model.Agente;
import org.citas.jakarta.hexagonal.domain.ports.out.CitaRepository;
import org.citas.jakarta.hexagonal.infraestructure.adapters.out.mariadb.AgenteMariaDbAdapter;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CitaOracleXEAdapter implements CitaRepository {

    private Connection connection;
    private static final String URL = "jdbc:oracle:thin:@localhost:1522:XE";
    private static final String USER = "citasdb";
    private static final String PASSWORD = "123456";

    @Inject
    AgenteMariaDbAdapter agenteAdapter;

    @PostConstruct
    public void init() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Properties props = new Properties();
            props.setProperty("user", USER);
            props.setProperty("password", PASSWORD);
            props.setProperty("oracle.net.CONNECT_TIMEOUT", "10000");

            this.connection = DriverManager.getConnection(URL, props);
            System.out.println("✅ Conexión a Oracle XE establecida para Citas");

            crearTablaSiNoExiste();
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con Oracle XE", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✅ Conexión a Oracle XE para Citas cerrada");
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión de Citas: " + e.getMessage());
            }
        }
    }

    private void crearTablaSiNoExiste() {
        String checkTableSql = "SELECT COUNT(*) FROM user_tables WHERE table_name = 'CITAS'";
        String createTableSql = """
            CREATE TABLE citas (
                id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                agente_id NUMBER NOT NULL,
                cliente_nombre VARCHAR2(100) NOT NULL,
                cliente_email VARCHAR2(150) NOT NULL,
                fecha_hora TIMESTAMP NOT NULL,
                motivo VARCHAR2(500),
                fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                fecha_actualizacion TIMESTAMP NULL
            )
            """;
        String createIndex1Sql = "CREATE INDEX idx_citas_agente_fecha ON citas(agente_id, fecha_hora)";
        String createIndex2Sql = "CREATE INDEX idx_citas_fecha ON citas(fecha_hora)";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkTableSql);
             ResultSet rs = checkStmt.executeQuery()) {

            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement createStmt = connection.prepareStatement(createTableSql)) {
                    createStmt.execute();
                    System.out.println("✅ Tabla 'citas' creada en Oracle XE");
                }
                try (PreparedStatement idx1Stmt = connection.prepareStatement(createIndex1Sql)) {
                    idx1Stmt.execute();
                }
                try (PreparedStatement idx2Stmt = connection.prepareStatement(createIndex2Sql)) {
                    idx2Stmt.execute();
                }
                System.out.println("✅ Índices creados para tabla 'citas'");
            } else {
                System.out.println("✅ Tabla 'citas' ya existe en Oracle XE");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al crear/verificar tabla citas en Oracle XE", e);
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
            INSERT INTO citas (agente_id, cliente_nombre, cliente_email, fecha_hora, motivo)
            VALUES (?, ?, ?, ?, ?)
            """;

        String getGeneratedIdSql = "SELECT id FROM citas WHERE ROWID = (SELECT MAX(ROWID) FROM citas)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, cita.getAgenteId());
            stmt.setString(2, cita.getClienteNombre());
            stmt.setString(3, cita.getClienteEmail());
            stmt.setTimestamp(4, Timestamp.valueOf(cita.getFechaHora()));
            stmt.setString(5, cita.getMotivo());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("La inserción falló");

            try (PreparedStatement idStmt = connection.prepareStatement(getGeneratedIdSql);
                 ResultSet generatedKeys = idStmt.executeQuery()) {

                if (generatedKeys.next()) {
                    Long id = generatedKeys.getLong("id");
                    return new Cita(id, cita.getAgenteId(), cita.getClienteNombre(),
                            cita.getClienteEmail(), cita.getFechaHora(), cita.getMotivo());
                } else {
                    throw new SQLException("No se obtuvo ID generado");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error al insertar cita en Oracle XE", e);
        }
    }

    private Cita actualizarCita(Cita cita) {
        String sql = """
            UPDATE citas
            SET cliente_nombre = ?, cliente_email = ?, fecha_hora = ?, motivo = ?, fecha_actualizacion = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, cita.getClienteNombre());
            stmt.setString(2, cita.getClienteEmail());
            stmt.setTimestamp(3, Timestamp.valueOf(cita.getFechaHora()));
            stmt.setString(4, cita.getMotivo());
            stmt.setLong(5, cita.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new RuntimeException("Cita no encontrada con id: " + cita.getId());

            return cita;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar cita en Oracle XE", e);
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
            throw new RuntimeException("Error al obtener todas las citas de Oracle XE", e);
        }
    }

    @Override
    public Optional<Cita> findById(Long id) {
        String sql = "SELECT id, agente_id, cliente_nombre, cliente_email, fecha_hora, motivo FROM citas WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapResultSetToCita(rs));
                else return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar cita por ID en Oracle XE", e);
        }
    }

    @Override
    public List<Cita> findByAgenteId(Long agenteId) {
        String sql = "SELECT id, agente_id, cliente_nombre, cliente_email, fecha_hora, motivo FROM citas WHERE agente_id = ?";
        List<Cita> citas = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, agenteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) citas.add(mapResultSetToCita(rs));
            }
            return citas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar citas por agente en Oracle XE", e);
        }
    }

    @Override
    public List<Cita> findByFecha(LocalDateTime fecha) {
        LocalDateTime start = fecha.toLocalDate().atStartOfDay();
        LocalDateTime end = fecha.toLocalDate().atTime(23, 59, 59);
        String sql = "SELECT id, agente_id, cliente_nombre, cliente_email, fecha_hora, motivo FROM citas WHERE fecha_hora BETWEEN ? AND ?";
        List<Cita> citas = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(start));
            stmt.setTimestamp(2, Timestamp.valueOf(end));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) citas.add(mapResultSetToCita(rs));
            }
            return citas;
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar citas por fecha en Oracle XE", e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        String sql = "DELETE FROM citas WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar cita en Oracle XE", e);
        }
    }

    // Métodos adicionales
    public boolean existsByAgenteIdAndFechaHora(Long agenteId, LocalDateTime fechaHora) {
        String sql = "SELECT COUNT(*) FROM citas WHERE agente_id = ? AND fecha_hora = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, agenteId);
            stmt.setTimestamp(2, Timestamp.valueOf(fechaHora));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar existencia de cita en Oracle XE", e);
        }
    }

    public List<Map<String, Object>> findCitasConAgente() {
        List<Cita> citas = findAll();
        List<Map<String, Object>> resultados = new ArrayList<>();

        for (Cita c : citas) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("clienteNombre", c.getClienteNombre());
            map.put("clienteEmail", c.getClienteEmail());
            map.put("fechaHora", c.getFechaHora());
            map.put("motivo", c.getMotivo());

            Agente agente = agenteAdapter.findById(c.getAgenteId()).orElse(null);
            if (agente != null) {
                map.put("agenteNombre", agente.getNombre());
                map.put("agenteEspecialidad", agente.getEspecialidad());
            } else {
                map.put("agenteNombre", "Desconocido");
                map.put("agenteEspecialidad", "Desconocido");
            }

            resultados.add(map);
        }

        return resultados;
    }

    private Cita mapResultSetToCita(ResultSet rs) throws SQLException {
        return new Cita(
                rs.getLong("id"),
                rs.getLong("agente_id"),
                rs.getString("cliente_nombre"),
                rs.getString("cliente_email"),
                rs.getTimestamp("fecha_hora").toLocalDateTime(),
                rs.getString("motivo")
        );
    }
}
