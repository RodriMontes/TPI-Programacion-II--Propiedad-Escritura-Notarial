/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
import config.DatabaseConnection;
import entities.EscrituraNotarial;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Laura Mendez - Rodrigo Montes Sare - Gabriel Montaña
 */

public class EscrituraNotarialDao implements GenericDao<EscrituraNotarial> {

    // ======================
    // SQL base
    // ======================

    private static final String INSERT_SQL = """
        INSERT INTO escritura_notarial (
            esn_nro_escritura,
            esn_fecha,
            esn_notaria,
            esn_tomo,
            esn_folio,
            esn_observaciones,
            esn_propiedad_id
        ) VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE escritura_notarial
        SET esn_nro_escritura = ?,
            esn_fecha = ?,
            esn_notaria = ?,
            esn_tomo = ?,
            esn_folio = ?,
            esn_observaciones = ?,
            esn_propiedad_id = ?
        WHERE esn_id = ?
          AND esn_eliminado = FALSE
        """;

    private static final String DELETE_LOGICO_SQL = """
        UPDATE escritura_notarial
        SET esn_eliminado = TRUE,
        esn_propiedad_id = NULL
        WHERE esn_id = ?
        """;

    private static final String SELECT_BY_ID_SQL = """
        SELECT *
        FROM escritura_notarial
        WHERE esn_id = ?
          AND esn_eliminado = FALSE
        """;

    private static final String SELECT_ALL_SQL = """
        SELECT *
        FROM escritura_notarial
        WHERE esn_eliminado = FALSE
        ORDER BY esn_id
        """;

    private static final String SELECT_BY_NRO_SQL = """
        SELECT *
        FROM escritura_notarial
        WHERE esn_nro_escritura = ?
          AND esn_eliminado = FALSE
        """;

    private static final String SELECT_BY_PROPIEDAD_SQL = """
        SELECT *
        FROM escritura_notarial
        WHERE esn_propiedad_id = ?
          AND esn_eliminado = FALSE
        """;

    // ======================
    // Métodos sin Connection
    // ======================

    @Override
    public void crear(EscrituraNotarial entidad) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            crear(conn, entidad);
        }
    }

    @Override
    public EscrituraNotarial leerPorId(Long id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEscritura(rs);
                }
                return null;
            }
        }
    }

    @Override
    public List<EscrituraNotarial> leerTodos() throws SQLException {
        List<EscrituraNotarial> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRowToEscritura(rs));
            }
        }

        return lista;
    }

    @Override
    public void actualizar(EscrituraNotarial entidad) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            actualizar(conn, entidad);
        }
    }

    @Override
    public void eliminarLogico(Long id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            eliminarLogico(conn, id);
        }
    }

    // ======================
    // Métodos con Connection
    // para usar en transacciones
    // ======================

    @Override
    public void crear(Connection conn, EscrituraNotarial entidad) throws SQLException {
        if (entidad.getPropiedadId() == null) {
            throw new IllegalArgumentException("La escritura debe tener propiedadId (FK) antes de crearse");
        }

        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, entidad.getNroEscritura());

            LocalDate fecha = entidad.getFecha();
            if (fecha != null) {
                ps.setDate(2, Date.valueOf(fecha));
            } else {
                ps.setNull(2, Types.DATE);
            }

            ps.setString(3, entidad.getNotaria());
            ps.setString(4, entidad.getTomo());
            ps.setString(5, entidad.getFolio());
            ps.setString(6, entidad.getObservaciones());

            ps.setLong(7, entidad.getPropiedadId());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se insertó ninguna escritura (filas=0)");
            }

            // ID generado
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    entidad.setId(rs.getLong(1));
                }
            }
        }
    }

    @Override
    public void actualizar(Connection conn, EscrituraNotarial entidad) throws SQLException {
        if (entidad.getId() == null) {
            throw new IllegalArgumentException("La escritura a actualizar debe tener id");
        }

        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, entidad.getNroEscritura());

            LocalDate fecha = entidad.getFecha();
            if (fecha != null) {
                ps.setDate(2, Date.valueOf(fecha));
            } else {
                ps.setNull(2, Types.DATE);
            }

            ps.setString(3, entidad.getNotaria());
            ps.setString(4, entidad.getTomo());
            ps.setString(5, entidad.getFolio());
            ps.setString(6, entidad.getObservaciones());

            if (entidad.getPropiedadId() != null) {
                ps.setLong(7, entidad.getPropiedadId());
            } else {
                ps.setNull(7, Types.BIGINT);
            }

            ps.setLong(8, entidad.getId());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se actualizó ninguna escritura. Puede estar eliminada o no existir.");
            }
        }
    }

    @Override
    public void eliminarLogico(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_LOGICO_SQL)) {
            ps.setLong(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se pudo eliminar lógicamente la escritura (id=" + id + ")");
            }
        }
    }

    // ======================
    // Métodos específicos
    // ======================

    public EscrituraNotarial buscarPorNroEscritura(String nro) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_NRO_SQL)) {

            ps.setString(1, nro);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEscritura(rs);
                }
                return null;
            }
        }
    }

    public EscrituraNotarial buscarPorPropiedadId(Long propiedadId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_PROPIEDAD_SQL)) {

            ps.setLong(1, propiedadId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEscritura(rs);
                }
                return null;
            }
        }
    }
    
    public EscrituraNotarial buscarPorPropiedadId(Connection conn, Long propiedadId) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_PROPIEDAD_SQL)) {
        ps.setLong(1, propiedadId);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? mapRowToEscritura(rs) : null;
        }
    }
}

    // ======================
    // Helper de mapeo
    // ======================

    private EscrituraNotarial mapRowToEscritura(ResultSet rs) throws SQLException {
        EscrituraNotarial e = new EscrituraNotarial();

        e.setId(rs.getLong("esn_id"));
        e.setEliminado(rs.getBoolean("esn_eliminado"));
        e.setNroEscritura(rs.getString("esn_nro_escritura"));

        Date fechaSql = rs.getDate("esn_fecha");
        if (fechaSql != null) {
            e.setFecha(fechaSql.toLocalDate());
        }

        e.setNotaria(rs.getString("esn_notaria"));
        e.setTomo(rs.getString("esn_tomo"));
        e.setFolio(rs.getString("esn_folio"));
        e.setObservaciones(rs.getString("esn_observaciones"));
        e.setPropiedadId(rs.getLong("esn_propiedad_id"));

        return e;
    }  
}
