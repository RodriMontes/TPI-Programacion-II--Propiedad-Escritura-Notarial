/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
import config.DatabaseConnection;
import entities.Destino;
import entities.Propiedad;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Laura Mendez - Rodrigo Montes Sare - Gabriel Montaña
 */
public class PropiedadDao implements GenericDao<Propiedad> {

    // SQL base
    private static final String INSERT_SQL = """
        INSERT INTO propiedad (
            pro_padron_catastral,
            pro_direccion,
            pro_superficie_m2,
            pro_destino,
            pro_antiguedad,
            pro_email
        ) VALUES (?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_SQL = """
        UPDATE propiedad
        SET pro_padron_catastral = ?,
            pro_direccion = ?,
            pro_superficie_m2 = ?,
            pro_destino = ?,
            pro_antiguedad = ?,
            pro_email = ?
        WHERE pro_id = ?
          AND pro_eliminado = FALSE
        """;

    private static final String DELETE_LOGICO_SQL = """
        UPDATE propiedad
        SET pro_eliminado = TRUE
        WHERE pro_id = ?
        """;

    private static final String SELECT_BY_ID_SQL = """
        SELECT *
        FROM propiedad
        WHERE pro_id = ?
          AND pro_eliminado = FALSE
        """;

    private static final String SELECT_ALL_SQL = """
        SELECT *
        FROM propiedad
        WHERE pro_eliminado = FALSE
        ORDER BY pro_id
        """;

    private static final String SELECT_BY_PADRON_SQL = """
        SELECT *
        FROM propiedad
        WHERE pro_padron_catastral = ?
          AND pro_eliminado = FALSE
        """;

    // =======================
    // Métodos públicos (sin Connection)
    // =======================

    @Override
    public void crear(Propiedad entidad) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            crear(conn, entidad);
        }
    }

    @Override
    public Propiedad leerPorId(Long id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_SQL)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPropiedad(rs);
                }
                return null;
            }
        }
    }

    @Override
    public List<Propiedad> leerTodos() throws SQLException {
        List<Propiedad> lista = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapRowToPropiedad(rs));
            }
        }

        return lista;
    }
    
    public List<Propiedad> leerTodos(Connection conn) throws SQLException {
        List<Propiedad> lista = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapRowToPropiedad(rs));
        }
        return lista;
    }

    @Override
    public void actualizar(Propiedad entidad) throws SQLException {
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

    // =======================
    // Métodos con Connection 
    // para transacciones
    // =======================

    @Override
    public void crear(Connection conn, Propiedad entidad) throws SQLException {
        if (entidad.getSuperficieM2() == null)
            throw new IllegalArgumentException("superficieM2 es obligatorio");
        if (entidad.getDestino() == null)
            throw new IllegalArgumentException("destino es obligatorio (RES/COM)");

        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, entidad.getPadronCatastral());
            ps.setString(2, entidad.getDireccion());

            ps.setBigDecimal(3, entidad.getSuperficieM2());
            ps.setString(4, entidad.getDestino().name());

            // Opcionales
            if (entidad.getAntiguedad() != null) ps.setInt(5, entidad.getAntiguedad());
            else ps.setNull(5, Types.INTEGER);

            String email = entidad.getEmail();
            if (email == null || email.isBlank()) ps.setNull(6, Types.VARCHAR); // normalizar "" -> NULL
            else ps.setString(6, email);

            int filas = ps.executeUpdate();
            if (filas == 0) throw new SQLException("No se insertó ninguna propiedad (filas=0)");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) entidad.setId(rs.getLong(1));
                else throw new SQLException("No se obtuvo la clave generada de propiedad");
            }
        }
    }


    @Override
    public void actualizar(Connection conn, Propiedad entidad) throws SQLException {
        if (entidad.getId() == null) {
            throw new IllegalArgumentException("La propiedad a actualizar debe tener id");
        }

        try (PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {

            ps.setString(1, entidad.getPadronCatastral());
            ps.setString(2, entidad.getDireccion());

            BigDecimal superficie = entidad.getSuperficieM2();
            if (superficie != null) {
                ps.setBigDecimal(3, superficie);
            } else {
                ps.setNull(3, Types.DECIMAL);
            }

            if (entidad.getDestino() != null) {
                ps.setString(4, entidad.getDestino().name());
            } else {
                ps.setNull(4, Types.VARCHAR);
            }

            if (entidad.getAntiguedad() != null) {
                ps.setInt(5, entidad.getAntiguedad());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            if (entidad.getEmail() != null) {
                ps.setString(6, entidad.getEmail());
            } else {
                ps.setNull(6, Types.VARCHAR);
            }

            ps.setLong(7, entidad.getId());

            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se actualizó ninguna propiedad. Puede estar eliminada o no existir.");
            }
        }
    }

    @Override
    public void eliminarLogico(Connection conn, Long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_LOGICO_SQL)) {
            ps.setLong(1, id);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new SQLException("No se pudo eliminar lógicamente la propiedad (id=" + id + ")");
            }
        }
    }

    // =======================
    // Métodos específicos de Propiedad
    // =======================

    /**
     * Busca una propiedad por padrón catastral (UNIQUE en la BD).
     */
    public Propiedad buscarPorPadronCatastral(String padron) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_PADRON_SQL)) {

            ps.setString(1, padron);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPropiedad(rs);
                }
                return null;
            }
        }
    }

    // =======================
    // Helper privado de mapeo
    // =======================

    private Propiedad mapRowToPropiedad(ResultSet rs) throws SQLException {
        Propiedad p = new Propiedad();

        p.setId(rs.getLong("pro_id"));
        p.setEliminado(rs.getBoolean("pro_eliminado"));
        p.setPadronCatastral(rs.getString("pro_padron_catastral"));
        p.setDireccion(rs.getString("pro_direccion"));
        p.setSuperficieM2(rs.getBigDecimal("pro_superficie_m2"));

        String destinoStr = rs.getString("pro_destino");
        if (destinoStr != null) {
            p.setDestino(Destino.valueOf(destinoStr)); // RES o COM
        }

        int antig = rs.getInt("pro_antiguedad");
        if (!rs.wasNull()) {
            p.setAntiguedad(antig);
        } else {
            p.setAntiguedad(null);
        }

        p.setEmail(rs.getString("pro_email"));

        p.setEscrituraNotarial(null);

        return p;
    }    
}
