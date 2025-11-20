/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
import config.DatabaseConnection;
import entities.Propiedad;
import entities.EscrituraNotarial;

import java.sql.*;
import java.util.*;

/**
 *
 * @author Laura Mendez - Rodrigo Montes Sare - Gabriel Montaña
 */
public class VistasDao {
    // 1) Listar propiedades completas (vista)
    public List<Propiedad> listarPropiedadesCompletas() throws SQLException {
        String sql = """
            SELECT pro_id, pro_padron_catastral, pro_direccion, pro_superficie_m2,
                   pro_destino, pro_antiguedad,
                   esn_id, esn_nro_escritura, esn_fecha, esn_notaria, esn_tomo, esn_folio, esn_observaciones,
                   tiene_escritura, categoria_antiguedad, categoria_superficie
            FROM v_propiedades_completas
            ORDER BY pro_id
        """;
        List<Propiedad> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Propiedad p = new Propiedad();
                p.setId(rs.getLong("pro_id"));
                p.setPadronCatastral(rs.getString("pro_padron_catastral"));
                p.setDireccion(rs.getString("pro_direccion"));
                p.setSuperficieM2(rs.getBigDecimal("pro_superficie_m2"));
                p.setDestino(entities.Destino.valueOf(rs.getString("pro_destino")));
                int ant = rs.getInt("pro_antiguedad");
                p.setAntiguedad(rs.wasNull() ? null : ant);

                Long esnId = (Long) rs.getObject("esn_id");
                if (esnId != null) {
                    EscrituraNotarial e = new EscrituraNotarial();
                    e.setId(esnId);
                    e.setNroEscritura(rs.getString("esn_nro_escritura"));
                    var f = rs.getDate("esn_fecha");
                    e.setFecha(f != null ? f.toLocalDate() : null);
                    e.setNotaria(rs.getString("esn_notaria"));
                    e.setTomo(rs.getString("esn_tomo"));
                    e.setFolio(rs.getString("esn_folio"));
                    e.setObservaciones(rs.getString("esn_observaciones"));
                    e.setPropiedadId(p.getId());
                    p.setEscrituraNotarial(e);
                }
                out.add(p);
            }
        }
        return out;
    }

    // 2) Búsqueda avanzada en la vista (por texto + filtros opcionales)
    public List<Propiedad> buscarEnVistaAvanzada(String texto, String rangoSup, String rangoAnt, Integer anio, Integer mes) throws SQLException {
        StringBuilder sb = new StringBuilder("""
            SELECT pro_id, pro_padron_catastral, pro_direccion, pro_superficie_m2,
                   pro_destino, pro_antiguedad, esn_nro_escritura, esn_fecha, esn_notaria
            FROM v_busqueda_avanzada
            WHERE 1=1
        """);
        List<Object> params = new ArrayList<>();

        if (texto != null && !texto.isBlank()) {
            sb.append(" AND texto_busqueda LIKE ? ");
            params.add("%" + texto.toLowerCase() + "%");
        }
        if (rangoSup != null) {
            sb.append(" AND rango_superficie = ? ");
            params.add(rangoSup);
        }
        if (rangoAnt != null) {
            sb.append(" AND rango_antiguedad = ? ");
            params.add(rangoAnt);
        }
        if (anio != null) {
            sb.append(" AND anio_escritura = ? ");
            params.add(anio);
        }
        if (mes != null) {
            sb.append(" AND mes_escritura = ? ");
            params.add(mes);
        }
        sb.append(" ORDER BY pro_id ");

        List<Propiedad> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Propiedad p = new Propiedad();
                    p.setId(rs.getLong("pro_id"));
                    p.setPadronCatastral(rs.getString("pro_padron_catastral"));
                    p.setDireccion(rs.getString("pro_direccion"));
                    p.setSuperficieM2(rs.getBigDecimal("pro_superficie_m2"));
                    p.setDestino(entities.Destino.valueOf(rs.getString("pro_destino")));
                    int ant = rs.getInt("pro_antiguedad");
                    p.setAntiguedad(rs.wasNull() ? null : ant);

                    String nro = rs.getString("esn_nro_escritura");
                    if (nro != null) {
                        EscrituraNotarial e = new EscrituraNotarial();
                        e.setNroEscritura(nro);
                        var f = rs.getDate("esn_fecha");
                        e.setFecha(f != null ? f.toLocalDate() : null);
                        e.setNotaria(rs.getString("esn_notaria"));
                        e.setPropiedadId(p.getId());
                        p.setEscrituraNotarial(e);
                    }
                    out.add(p);
                }
            }
        }
        return out;
    }

    // 3) Accesos rápidos a otras vistas simples
    public List<Long> listarPropiedadesResidencialesIds() throws SQLException {
        String sql = "SELECT pro_id FROM v_propiedades_residenciales ORDER BY pro_id";
        List<Long> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(rs.getLong(1));
        }
        return out;
    }

    public List<Long> listarEscriturasUltimoAnioIds() throws SQLException {
        String sql = "SELECT esn_id FROM v_escrituras_recientes ORDER BY esn_fecha DESC";
        List<Long> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(rs.getLong(1));
        }
        return out;
    }
}