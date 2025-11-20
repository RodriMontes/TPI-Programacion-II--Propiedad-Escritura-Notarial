/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package services;

import exceptions.AppException;
import java.util.List;

/**
 *
 * @author Laura Mendez - Rodrigo Montes Sare - Gabriel Montaña
 */
public interface GenericService<T> {

    void insertar(T entidad) throws AppException;

    void actualizar(T entidad) throws AppException;

    void eliminar(Long id) throws AppException;

    T getById(Long id) throws AppException;

    List<T> getAll() throws AppException;
}
