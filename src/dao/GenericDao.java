/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.util.List;
import entities.BaseEntity;
import java.sql.Connection;
import java.util.List;
import java.sql.SQLException;

/**
 *
 * @author Laura Mendez - Rodrigo Montes Sare - Gabriel Montaña
 */
public interface GenericDao<T extends BaseEntity> {

    void crear(T entidad) throws SQLException;

    T leerPorId(Long id) throws SQLException;

    List<T> leerTodos() throws SQLException;

    void actualizar(T entidad) throws SQLException;

    void eliminarLogico(Long id) throws SQLException;

    // versiones transaccionales con Connection externa
    void crear(Connection conn, T entidad) throws SQLException;

    void actualizar(Connection conn, T entidad) throws SQLException;

    void eliminarLogico(Connection conn, Long id) throws SQLException;
}
